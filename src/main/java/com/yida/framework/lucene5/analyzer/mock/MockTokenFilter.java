package com.yida.framework.lucene5.analyzer.mock;

import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.automaton.Automata;
import org.apache.lucene.util.automaton.CharacterRunAutomaton;
import org.apache.lucene.util.automaton.Operations;

public class MockTokenFilter extends TokenFilter {
	/** Empty set of stopwords */
	  public static final CharacterRunAutomaton EMPTY_STOPSET =
	    new CharacterRunAutomaton(Automata.makeEmpty());
	  
	  /** Set of common english stopwords */
	  public static final CharacterRunAutomaton ENGLISH_STOPSET = 
	    new CharacterRunAutomaton(Operations.union(Arrays.asList(
	      Automata.makeString("a"), Automata.makeString("an"), Automata.makeString("and"), Automata.makeString("are"),
	      Automata.makeString("as"), Automata.makeString("at"), Automata.makeString("be"), Automata.makeString("but"), 
	      Automata.makeString("by"), Automata.makeString("for"), Automata.makeString("if"), Automata.makeString("in"), 
	      Automata.makeString("into"), Automata.makeString("is"), Automata.makeString("it"), Automata.makeString("no"),
	      Automata.makeString("not"), Automata.makeString("of"), Automata.makeString("on"), Automata.makeString("or"), 
	      Automata.makeString("such"), Automata.makeString("that"), Automata.makeString("the"), Automata.makeString("their"), 
	      Automata.makeString("then"), Automata.makeString("there"), Automata.makeString("these"), Automata.makeString("they"), 
	      Automata.makeString("this"), Automata.makeString("to"), Automata.makeString("was"), Automata.makeString("will"), 
	      Automata.makeString("with"))));
	  
	  private final CharacterRunAutomaton filter;

	  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	  private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
	  private int skippedPositions;

	  /**
	   * Create a new MockTokenFilter.
	   * 
	   * @param input TokenStream to filter
	   * @param filter DFA representing the terms that should be removed.
	   */
	  public MockTokenFilter(TokenStream input, CharacterRunAutomaton filter) {
	    super(input);
	    this.filter = filter;
	  }
	  
	  @Override
	  public boolean incrementToken() throws IOException {
	    // TODO: fix me when posInc=false, to work like FilteringTokenFilter in that case and not return
	    // initial token with posInc=0 ever
	    
	    // return the first non-stop word found
	    skippedPositions = 0;
	    while (input.incrementToken()) {
	      if (!filter.run(termAtt.buffer(), 0, termAtt.length())) {
	        posIncrAtt.setPositionIncrement(posIncrAtt.getPositionIncrement() + skippedPositions);
	        return true;
	      }
	      skippedPositions += posIncrAtt.getPositionIncrement();
	    }
	    // reached EOS -- return false
	    return false;
	  }

	  @Override
	  public void end() throws IOException {
	    super.end();
	    posIncrAtt.setPositionIncrement(posIncrAtt.getPositionIncrement() + skippedPositions);
	  }

	  @Override
	  public void reset() throws IOException {
	    super.reset();
	    skippedPositions = 0;
	  }
}
