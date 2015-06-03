package com.yida.framework.lucene5.sort;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.BinaryDocValuesField;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
/**
 * 创建测试索引
 * @author Lanxiaowei
 *
 */
public class CreateTestIndex {
	public static void main(String[] args) throws IOException {
		String dataDir = "C:/data";
		String indexDir = "C:/lucenedir";

		Directory dir = FSDirectory.open(Paths.get(indexDir));
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
		indexWriterConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
		IndexWriter writer = new IndexWriter(dir, indexWriterConfig);

		List<File> results = new ArrayList<File>();
		findFiles(results, new File(dataDir));
		System.out.println(results.size() + " books to index");

		for (File file : results) {
			Document doc = getDocument(dataDir, file);
			writer.addDocument(doc);
		}
		writer.close();
		dir.close();

	}

	/**
	 * 查找指定目录下的所有properties文件
	 * 
	 * @param result
	 * @param dir
	 */
	private static void findFiles(List<File> result, File dir) {
		for (File file : dir.listFiles()) {
			if (file.getName().endsWith(".properties")) {
				result.add(file);
			} else if (file.isDirectory()) {
				findFiles(result, file);
			}
		}
	}

	/**
	 * 读取properties文件生成Document
	 * 
	 * @param rootDir
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static Document getDocument(String rootDir, File file)
			throws IOException {
		Properties props = new Properties();
		props.load(new FileInputStream(file));

		Document doc = new Document();

		String category = file.getParent().substring(rootDir.length());
		category = category.replace(File.separatorChar, '/');

		String isbn = props.getProperty("isbn");
		String title = props.getProperty("title");
		String author = props.getProperty("author");
		String url = props.getProperty("url");
		String subject = props.getProperty("subject");

		String pubmonth = props.getProperty("pubmonth");

		System.out.println("title:" + title + "\n" + "author:" + author + "\n" + "subject:" + subject + "\n"
				+ "pubmonth:" + pubmonth + "\n" + "category:" + category + "\n---------");

		doc.add(new StringField("isbn", isbn, Field.Store.YES));
		doc.add(new StringField("category", category, Field.Store.YES));
		doc.add(new SortedDocValuesField("category", new BytesRef(category)));
		doc.add(new TextField("title", title, Field.Store.YES));
		doc.add(new Field("title2", title.toLowerCase(), Field.Store.YES,
				Field.Index.NOT_ANALYZED_NO_NORMS,
				Field.TermVector.WITH_POSITIONS_OFFSETS));
		//doc.add(new BinaryDocValuesField("title2", new BytesRef(title.getBytes())));
		
		doc.add(new SortedDocValuesField("title2", new BytesRef(title.getBytes())));
		String[] authors = author.split(",");
		for (String a : authors) {
			doc.add(new Field("author", a, Field.Store.YES,
					Field.Index.NOT_ANALYZED,
					Field.TermVector.WITH_POSITIONS_OFFSETS));
		}

		doc.add(new Field("url", url, Field.Store.YES,
				Field.Index.NOT_ANALYZED_NO_NORMS));
		doc.add(new Field("subject", subject, Field.Store.YES,
				Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));

		doc.add(new IntField("pubmonth", Integer.parseInt(pubmonth),
				Field.Store.YES));
		doc.add(new NumericDocValuesField("pubmonth", Integer.parseInt(pubmonth)));
		Date d = null;
		try {
			d = DateTools.stringToDate(pubmonth);
		} catch (ParseException pe) {
			throw new RuntimeException(pe);
		}
		int day = (int) (d.getTime() / (1000 * 3600 * 24));
		doc.add(new IntField("pubmonthAsDay",day, Field.Store.YES));
		doc.add(new NumericDocValuesField("pubmonthAsDay", day));
		for (String text : new String[] { title, subject, author, category }) {
			doc.add(new Field("contents", text, Field.Store.NO,
					Field.Index.ANALYZED,
					Field.TermVector.WITH_POSITIONS_OFFSETS));
		}
		return doc;
	}

}
