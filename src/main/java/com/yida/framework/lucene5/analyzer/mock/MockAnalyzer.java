package com.yida.framework.lucene5.analyzer.mock;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.automaton.CharacterRunAutomaton;

public class MockAnalyzer extends Analyzer {
	public static void main(String[] args) throws IOException {
		Map<String,String> synonyms = new HashMap<String, String>();
		synonyms.put("人民", "百姓");
		synonyms.put("世界", "world");
		synonyms.put("赶赴", "奔赴");
		synonyms.put("速度", "velocity");
		
		String text = "速度赶赴事故种花人民饶天亮可口可乐看男子怕女友被人抢走 将其从100斤喂到180斤后求婚。。醉了把世界名画变成2.5D动画，太美了。。We have a development schema/db and production schema/db. When developers are working on stuff they just make a copy of the \"build machines\" development database and restore it locally. This database is much smaller than the production db and is ideal for testing. Your production db should no be that much different than your development db schema wise (make smaller changes and release more often if it is the case.)";
		//AnalyzerUtils.displayTokens(new WhitespaceAnalyzer(), text);
		//AnalyzerUtils.displayTokens(new MockAnalyzer(), text);
	}
	private final CharacterRunAutomaton runAutomaton;
	  private final boolean lowerCase;
	  private final CharacterRunAutomaton filter;
	  private int positionIncrementGap;
	  private Integer offsetGap;
	  private boolean enableChecks = true;
	  private int maxTokenLength = MockTokenizer.DEFAULT_MAX_TOKEN_LENGTH;
	  //private boolean VERBOSE;

	  /**
	   * Creates a new MockAnalyzer.
	   * 
	   * @param runAutomaton DFA describing how tokenization should happen (e.g. [a-zA-Z]+)
	   * @param lowerCase true if the tokenizer should lowercase terms
	   * @param filter DFA describing how terms should be filtered (set of stopwords, etc)
	   */
	  public MockAnalyzer(CharacterRunAutomaton runAutomaton, boolean lowerCase, CharacterRunAutomaton filter) {
	    super(PER_FIELD_REUSE_STRATEGY);
	    this.runAutomaton = runAutomaton;
	    this.lowerCase = lowerCase;
	    this.filter = filter;
	  }

	  public MockAnalyzer(CharacterRunAutomaton runAutomaton, boolean lowerCase) {
	    this(runAutomaton, lowerCase, MockTokenFilter.EMPTY_STOPSET);
	  }

	  public MockAnalyzer() {
	    this(MockTokenizer.WHITESPACE, true);
	  }

	  @Override
	  public TokenStreamComponents createComponents(String fieldName) {
	    MockTokenizer tokenizer = new MockTokenizer(runAutomaton, lowerCase, maxTokenLength);
	    tokenizer.setEnableChecks(enableChecks);
	    MockTokenFilter filt = new MockTokenFilter(tokenizer, filter);
	    return new TokenStreamComponents(tokenizer, filt);
	    //return new TokenStreamComponents(tokenizer, maybePayload(filt, fieldName));
	  }
	  
	 /* private synchronized TokenFilter maybePayload(TokenFilter stream, String fieldName) {
	    Integer val = previousMappings.get(fieldName);
	    if (val == null) {
	      val = -1; // no payloads
	      if (LuceneTestCase.rarely(random)) {
	        switch(random.nextInt(3)) {
	          case 0: val = -1; // no payloads
	                  break;
	          case 1: val = Integer.MAX_VALUE; // variable length payload
	                  break;
	          case 2: val = random.nextInt(12); // fixed length payload
	                  break;
	        }
	      }
	      if (VERBOSE) {
	        if (val == Integer.MAX_VALUE) {
	          System.out.println("MockAnalyzer: field=" + fieldName + " gets variable length payloads");
	        } else if (val != -1) {
	          System.out.println("MockAnalyzer: field=" + fieldName + " gets fixed length=" + val + " payloads");
	        }
	      }
	      previousMappings.put(fieldName, val); // save it so we are consistent for this field
	    }
	    
	    if (val == -1)
	      return stream;
	    else if (val == Integer.MAX_VALUE)
	      return new MockVariableLengthPayloadFilter(random, stream);
	    else
	      return new MockFixedLengthPayloadFilter(random, stream, val);
	  }*/
	  
	  public void setPositionIncrementGap(int positionIncrementGap){
	    this.positionIncrementGap = positionIncrementGap;
	  }
	  
	  @Override
	  public int getPositionIncrementGap(String fieldName){
	    return positionIncrementGap;
	  }

	  /**
	   * Set a new offset gap which will then be added to the offset when several fields with the same name are indexed
	   * @param offsetGap The offset gap that should be used.
	   */
	  public void setOffsetGap(int offsetGap){
	    this.offsetGap = offsetGap;
	  }

	  /**
	   * Get the offset gap between tokens in fields if several fields with the same name were added.
	   * @param fieldName Currently not used, the same offset gap is returned for each field.
	   */
	  @Override
	  public int getOffsetGap(String fieldName){
	    return offsetGap == null ? super.getOffsetGap(fieldName) : offsetGap;
	  }
	  
	  /** 
	   * Toggle consumer workflow checking: if your test consumes tokenstreams normally you
	   * should leave this enabled.
	   */
	  public void setEnableChecks(boolean enableChecks) {
	    this.enableChecks = enableChecks;
	  }
	  
	  /** 
	   * Toggle maxTokenLength for MockTokenizer
	   */
	  public void setMaxTokenLength(int length) {
	    this.maxTokenLength = length;
	  }
}
