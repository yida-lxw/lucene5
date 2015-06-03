package com.yida.framework.lucene5.util;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
/**
 * Lucene索引读写器/查询器单例获取工具类
 * @author Lanxiaowei
 *
 */
public class LuceneManager {
	private volatile static LuceneManager singleton;
	
	private volatile static IndexWriter writer;
	
	private volatile static IndexReader reader;
	
	private volatile static IndexSearcher searcher;
	
	private final Lock writerLock = new ReentrantLock();
	
	//private final Lock readerLock = new ReentrantLock();
	
	//private final Lock searcherLock = new ReentrantLock();
	

	private static ThreadLocal<IndexWriter> writerLocal = new ThreadLocal<IndexWriter>();

	private LuceneManager() {}

	public static LuceneManager getInstance() {
		if (null == singleton) {
			synchronized (LuceneManager.class) {
				if (null == singleton) {
					singleton = new LuceneManager();
				}
			}
		}
		return singleton;
	}

	/**
	 * 获取IndexWriter单例对象
	 * @param dir
	 * @param config
	 * @return
	 */
	public IndexWriter getIndexWriter(Directory dir, IndexWriterConfig config) {
		if(null == dir) {
			throw new IllegalArgumentException("Directory can not be null.");
		}
		if(null == config) {
			throw new IllegalArgumentException("IndexWriterConfig can not be null.");
		}
		try {
			writerLock.lock();
			writer = writerLocal.get();
			if(null != writer) {
				return writer;
			}
			if(null == writer){
				//如果索引目录被锁，则直接抛异常
				if(IndexWriter.isLocked(dir)) {
					throw new LockObtainFailedException("Directory of index had been locked.");
				}
				writer = new IndexWriter(dir, config);
				writerLocal.set(writer);
			}
		} catch (LockObtainFailedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			writerLock.unlock();
		}
		return writer;
	}
	
	/**
	 * 获取IndexWriter[可能为Null]
	 * @return
	 */
	public IndexWriter getIndexWriter() {
		return writer;
	}
	
	/**
	 * 获取IndexReader对象
	 * @param dir
	 * @param enableNRTReader  是否开启NRTReader
	 * @return
	 */
	public IndexReader getIndexReader(Directory dir,boolean enableNRTReader) {
		if(null == dir) {
			throw new IllegalArgumentException("Directory can not be null.");
		}
		try {
			if(null == reader){
				reader = DirectoryReader.open(dir);
			} else {
				if(enableNRTReader && reader instanceof DirectoryReader) {
					IndexReader oldReader = reader;
					//开启近实时Reader,能立即看到动态添加/删除的索引变化
					reader = DirectoryReader.openIfChanged((DirectoryReader)reader);
					//reader返回为null表明索引还没有变化
					if(null == reader) {
						reader = oldReader;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return reader;
	}
	
	/**
	 * 获取IndexReader对象(默认不启用NETReader)
	 * @param dir
	 * @return
	 */
	public IndexReader getIndexReader(Directory dir) {
		return getIndexReader(dir, false);
	}
	
	/**
	 * 获取IndexSearcher对象
	 * @param reader    IndexReader对象实例
	 * @param executor  如果你需要开启多线程查询，请提供ExecutorService对象参数
	 * @return
	 */
	public IndexSearcher getIndexSearcher(IndexReader reader,ExecutorService executor) {
		if(null == reader) {
			throw new IllegalArgumentException("The indexReader can not be null.");
		}
		if(null == searcher){
			searcher = new IndexSearcher(reader);
		}
		return searcher;
	}
	
	/**
	 * 获取IndexSearcher对象(不支持多线程查询)
	 * @param reader    IndexReader对象实例
	 * @return
	 */
	public IndexSearcher getIndexSearcher(IndexReader reader) {
		return getIndexSearcher(reader, null);
	}
	
	/**
	 * 关闭IndexWriter
	 * @param writer
	 */
	public void closeIndexWriter(IndexWriter writer) {
		if(null != writer) {
			try {
				writer.close();
				writer = null;
				writerLocal.remove();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
