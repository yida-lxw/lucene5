package com.yida.framework.lucene5.collector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
/**
 * 自定义Collector结果收集器
 * @author Lanxiaowei
 *
 */
public class GroupCollector implements Collector, LeafCollector {
	/**评分计算器*/
	private Scorer scorer;
	/**段文件的编号*/
    private int docBase;
    
    private String fieldName;
    private SortedDocValues sortedDocValues;
    
    private List<ScoreDoc> scoreDocs = new ArrayList<ScoreDoc>();
    
    public LeafCollector getLeafCollector(LeafReaderContext context)
			throws IOException {
    	this.sortedDocValues = context.reader().getSortedDocValues(fieldName);
    	return this;
	}
    
	public void setScorer(Scorer scorer) throws IOException {
		this.scorer = scorer;
	}

	public void collect(int doc) throws IOException {
        // scoreDoc:docId和评分
        this.scoreDocs.add(new ScoreDoc(this.docBase + doc, this.scorer.score()));
	}

	public GroupCollector(String fieldName) {
		super();
		this.fieldName = fieldName;
	}

	public int getDocBase() {
		return docBase;
	}

	public void setDocBase(int docBase) {
		this.docBase = docBase;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public SortedDocValues getSortedDocValues() {
		return sortedDocValues;
	}

	public void setSortedDocValues(SortedDocValues sortedDocValues) {
		this.sortedDocValues = sortedDocValues;
	}

	public List<ScoreDoc> getScoreDocs() {
		return scoreDocs;
	}

	public void setScoreDocs(List<ScoreDoc> scoreDocs) {
		this.scoreDocs = scoreDocs;
	}

	public Scorer getScorer() {
		return scorer;
	}
}
