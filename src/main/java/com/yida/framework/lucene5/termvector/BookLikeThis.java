package com.yida.framework.lucene5.termvector;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.CharsRefBuilder;
/**
 * 查找类似书籍-测试
 * @author Lanxiaowei
 *
 */
public class BookLikeThis {
	public static void main(String[] args) throws IOException {
		String indexDir = "C:/lucenedir";
		Directory directory = FSDirectory.open(Paths.get(indexDir));
		IndexReader reader = DirectoryReader.open(directory);
		IndexSearcher searcher = new IndexSearcher(reader);
		// 最大的索引文档ID
		int numDocs = reader.maxDoc();

		BookLikeThis blt = new BookLikeThis();
		for (int i = 0; i < numDocs; i++) {
			System.out.println();
			Document doc = reader.document(i);
			System.out.println(doc.get("title"));

			Document[] docs = blt.docsLike(reader, searcher, i, 10);
			if (docs.length == 0) {
				System.out.println("  -> Sorry,None like this");
			}
			for (Document likeThisDoc : docs) {
				System.out.println("  -> " + likeThisDoc.get("title"));
			}
		}
		reader.close();
		directory.close();
	}

	public Document[] docsLike(IndexReader reader, IndexSearcher searcher,
			int id, int max) throws IOException {
		//根据文档id加载文档对象
		Document doc = reader.document(id);
		//获取所有的作者
		String[] authors = doc.getValues("author");
		BooleanQuery authorQuery = new BooleanQuery();
		//遍历所有的作者
		for (String author : authors) {
			//包含所有作者的书籍
			authorQuery.add(new TermQuery(new Term("author", author)),Occur.SHOULD);
		}
		//authorQuery权重乘以2
		authorQuery.setBoost(2.0f);

		//获取subject域的项向量
		Terms vector = reader.getTermVector(id, "subject");
		TermsEnum termsEnum = vector.iterator(null);
		CharsRefBuilder spare = new CharsRefBuilder();
		BytesRef text = null;
		BooleanQuery subjectQuery = new BooleanQuery();
		while ((text = termsEnum.next()) != null) {
			spare.copyUTF8Bytes(text);
			String term = spare.toString();
			//System.out.println("term:" + term);
			// if isNoiseWord
			TermQuery tq = new TermQuery(new Term("subject", term));
			//使用subject域中的项向量构建BooleanQuery
			subjectQuery.add(tq, Occur.SHOULD);
		}

		BooleanQuery likeThisQuery = new BooleanQuery();
		likeThisQuery.add(authorQuery, BooleanClause.Occur.SHOULD);
		likeThisQuery.add(subjectQuery, BooleanClause.Occur.SHOULD);

		//排除自身
		likeThisQuery.add(new TermQuery(new Term("isbn", doc.get("isbn"))),
				BooleanClause.Occur.MUST_NOT);

		TopDocs hits = searcher.search(likeThisQuery, 10);
		int size = max;
		if (max > hits.scoreDocs.length) {
			size = hits.scoreDocs.length;
		}

		Document[] docs = new Document[size];
		for (int i = 0; i < size; i++) {
			docs[i] = reader.document(hits.scoreDocs[i].doc);
		}
		return docs;
	}
}
