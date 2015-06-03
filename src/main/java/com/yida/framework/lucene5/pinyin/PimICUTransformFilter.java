package com.yida.framework.lucene5.pinyin;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import com.ibm.icu.text.Replaceable;
import com.ibm.icu.text.Transliterator;
import com.ibm.icu.text.UTF16;
import com.ibm.icu.text.UnicodeSet;

public class PimICUTransformFilter extends TokenFilter {
	/**
	 * 首汉字全拼后续汉字简拼
	 * Transliterator.createFromRules(null, ":: Han-Latin/Names;[[:space:]][bpmfdtnlgkhjqxzcsryw] { [[:any:]-[:white_space:]] >;::NFD;[[:NonspacingMark:][:Space:]]>;",Transliterator.FORWARD)
	 */
    private final Transliterator[] transforms;
 
    // Reusable position object
    private final Transliterator.Position position = new Transliterator.Position();
 
    // term attribute, will be updated with transformed text.
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
 
    // Wraps a termAttribute around the replaceable interface.
    private final ReplaceableTermAttribute replaceableAttribute = new ReplaceableTermAttribute();
 
    private char[] curTermBuffer;
    private int curTermLength;
    private int currentTransform;
 
    /**
     * Create a new ICUTransformFilter that transforms text on the given stream.
     *
     * @param input
     *            {@link TokenStream} to filter.
     * @param transform
     *            Transliterator to transform the text.
     */
    @SuppressWarnings("deprecation")
    public PimICUTransformFilter(TokenStream input, Transliterator[] transforms) {
        super(input);
        this.transforms = transforms;
 
        /*
         * This is cheating, but speeds things up a lot. If we wanted to use
         * pkg-private APIs we could probably do better.
         */
        for (Transliterator transform : this.transforms) {
            if (transform.getFilter() == null
                    && transform instanceof com.ibm.icu.text.RuleBasedTransliterator) {
                final UnicodeSet sourceSet = transform.getSourceSet();
                if (sourceSet != null && !sourceSet.isEmpty())
                    transform.setFilter(sourceSet);
            }
        }
    }
 
    @Override
    public boolean incrementToken() throws IOException {
 
        while (true) {
            if (curTermBuffer == null) {
                if (!input.incrementToken()) {
                    return false;
                } else {
                    curTermBuffer = termAtt.buffer().clone();
                    curTermLength = termAtt.length();
                    currentTransform = 0;
                }
            }
            if (currentTransform < transforms.length) {
                termAtt.copyBuffer(curTermBuffer, 0, curTermLength);
                termAtt.setLength(curTermLength);
                replaceableAttribute.setText(termAtt);
 
                final int length = termAtt.length();
                position.start = 0;
                position.limit = length;
                position.contextStart = 0;
                position.contextLimit = length;
 
                transforms[currentTransform++].filteredTransliterate(
                        replaceableAttribute, position, false);
                return true;
            }
            curTermBuffer = null;
        }
    }
 
    /**
     * Wrap a {@link CharTermAttribute} with the Replaceable API.
     */
    final class ReplaceableTermAttribute implements Replaceable {
        private char buffer[];
        private int length;
        private CharTermAttribute token;
 
        void setText(final CharTermAttribute token) {
            this.token = token;
            this.buffer = token.buffer();
            this.length = token.length();
        }
 
        public int char32At(int pos) {
            return UTF16.charAt(buffer, 0, length, pos);
        }
 
        public char charAt(int pos) {
            return buffer[pos];
        }
 
        public void copy(int start, int limit, int dest) {
            char text[] = new char[limit - start];
            getChars(start, limit, text, 0);
            replace(dest, dest, text, 0, limit - start);
        }
 
        public void getChars(int srcStart, int srcLimit, char[] dst,
                int dstStart) {
            System.arraycopy(buffer, srcStart, dst, dstStart, srcLimit
                    - srcStart);
        }
 
        public boolean hasMetaData() {
            return false;
        }
 
        public int length() {
            return length;
        }
 
        public void replace(int start, int limit, String text) {
            final int charsLen = text.length();
            final int newLength = shiftForReplace(start, limit, charsLen);
            // insert the replacement text
            text.getChars(0, charsLen, buffer, start);
            token.setLength(length = newLength);
        }
 
        public void replace(int start, int limit, char[] text, int charsStart,
                int charsLen) {
            // shift text if necessary for the replacement
            final int newLength = shiftForReplace(start, limit, charsLen);
            // insert the replacement text
            System.arraycopy(text, charsStart, buffer, start, charsLen);
            token.setLength(length = newLength);
        }
 
        /** shift text (if necessary) for a replacement operation */
        private int shiftForReplace(int start, int limit, int charsLen) {
            final int replacementLength = limit - start;
            final int newLength = length - replacementLength + charsLen;
            // resize if necessary
            if (newLength > length)
                buffer = token.resizeBuffer(newLength);
            // if the substring being replaced is longer or shorter than the
            // replacement, need to shift things around
            if (replacementLength != charsLen && limit < length)
                System.arraycopy(buffer, limit, buffer, start + charsLen,
                        length - limit);
            return newLength;
        }
    }
}
