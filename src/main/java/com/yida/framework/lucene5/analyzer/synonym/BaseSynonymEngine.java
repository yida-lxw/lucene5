package com.yida.framework.lucene5.analyzer.synonym;

import java.io.IOException;
import java.util.HashMap;

public class BaseSynonymEngine implements SynonymEngine {
	private static HashMap<String, String[]> map = new HashMap<String, String[]>();
	
	{
		map.put("quick", new String[] {"fast","speedy"});
		map.put("jumps", new String[] {"leaps","hops"});
		map.put("over", new String[] {"above"});
		map.put("lazy", new String[] {"apathetic","slugish"});
		map.put("dog", new String[] {"canine","pooch"});
	}

	public String[] getSynonyms(String s) throws IOException {
		return map.get(s);
	}
}
