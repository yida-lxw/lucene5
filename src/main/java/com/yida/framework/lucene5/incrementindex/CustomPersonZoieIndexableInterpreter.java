package com.yida.framework.lucene5.incrementindex;

import org.apache.lucene.analysis.Analyzer;

import proj.zoie.api.indexing.AbstractZoieIndexableInterpreter;
import proj.zoie.api.indexing.ZoieIndexable;

/**
 * 自定义Person-->Document的数据转换器的生产者
 * @author Lanxiaowei
 *
 */
public class CustomPersonZoieIndexableInterpreter extends AbstractZoieIndexableInterpreter<Person>{
	private Analyzer analyzer;
	
	@Override
	public ZoieIndexable convertAndInterpret(Person person) {
		return new PersonZoieIndexable(person, analyzer);
	}
	
	public CustomPersonZoieIndexableInterpreter() {}
	
	public CustomPersonZoieIndexableInterpreter(Analyzer analyzer) {
		super();
		this.analyzer = analyzer;
	}

	public Analyzer getAnalyzer() {
		return analyzer;
	}
	public void setAnalyzer(Analyzer analyzer) {
		this.analyzer = analyzer;
	}
}
