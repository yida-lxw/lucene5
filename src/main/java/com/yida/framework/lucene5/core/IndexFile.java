package com.yida.framework.lucene5.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.ansj.lucene5.AnsjAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * 读取硬盘文件，创建索引
 * 
 * @author Lanxiaowei
 * 
 */
@SuppressWarnings({ "unchecked", "unused", "rawtypes" })
public class IndexFile {
	public static void main(String[] args) throws IOException {
		String dirPath = "D:/docPath";
		String indexPath = "D:/lucenedir";
		createIndex(dirPath, indexPath);
	}
	
	/**
	 * 创建索引
	 * @param dirPath       需要读取的文件所在文件目录
	 * @param indexPath     索引存放目录
	 * @throws IOException
	 */
	public static void createIndex(String dirPath, String indexPath) throws IOException {
		createIndex(dirPath, indexPath, false);
	}
	
	/**
	 * 创建索引
	 * @param dirPath         需要读取的文件所在文件目录
	 * @param indexPath       索引存放目录
	 * @param createOrAppend  始终重建索引/不存在则追加索引
	 * @throws IOException
	 */
	public static void createIndex(String dirPath, String indexPath,
			boolean createOrAppend) throws IOException {
		long start = System.currentTimeMillis();
		Directory dir = FSDirectory.open(Paths.get(indexPath, new String[0]));
		Path docDirPath = Paths.get(dirPath, new String[0]);
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);

		if (createOrAppend) {
			indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		} else {
			indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
		}
		IndexWriter writer = new IndexWriter(dir, indexWriterConfig);
		indexDocs(writer, docDirPath);
		writer.close();
		long end = System.currentTimeMillis();
		System.out.println("Time consumed:" + (end - start) + " ms");
	}

	/**
	 * 
	 * @param writer
	 *            索引写入器
	 * @param path
	 *            文件路径
	 * @throws IOException
	 */
	public static void indexDocs(final IndexWriter writer, Path path)
			throws IOException {
		// 如果是目录，查找目录下的文件
		if (Files.isDirectory(path, new LinkOption[0])) {
			System.out.println("directory");
			Files.walkFileTree(path, new SimpleFileVisitor() {
				@Override
				public FileVisitResult visitFile(Object file,
						BasicFileAttributes attrs) throws IOException {
					Path path = (Path)file;
					System.out.println(path.getFileName());
					indexDoc(writer, path, attrs.lastModifiedTime().toMillis());
					return FileVisitResult.CONTINUE;
				}
			});
		} else {
			indexDoc(writer, path,
					Files.getLastModifiedTime(path, new LinkOption[0])
							.toMillis());
		}
	}

	/**
	 * 读取文件创建索引
	 * 
	 * @param writer
	 *            索引写入器
	 * @param file
	 *            文件路径
	 * @param lastModified
	 *            文件最后一次修改时间
	 * @throws IOException
	 */
	public static void indexDoc(IndexWriter writer, Path file, long lastModified)
			throws IOException {
		InputStream stream = Files.newInputStream(file, new OpenOption[0]);
		Document doc = new Document();

		Field pathField = new StringField("path", file.toString(),
				Field.Store.YES);
		doc.add(pathField);

		doc.add(new LongField("modified", lastModified, Field.Store.YES));
		doc.add(new TextField("contents",intputStream2String(stream),Field.Store.YES));
		//doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));

		if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
			System.out.println("adding " + file);
			writer.addDocument(doc);
			writer.addDocuments(null);
		} else {
			System.out.println("updating " + file);
			writer.updateDocument(new Term("path", file.toString()), doc);
		}
		writer.commit();
	}
	
	/**
	 * InputStream转换成String
	 * @param is    输入流对象
	 * @return
	 */
	private static String intputStream2String(InputStream is) {
		BufferedReader bufferReader = null;
		StringBuilder stringBuilder = new StringBuilder();
		String line;
		try {
			bufferReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
			while ((line = bufferReader.readLine()) != null) {
				stringBuilder.append(line + "\r\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bufferReader != null) {
				try {
					bufferReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return stringBuilder.toString();
	}
}
