package com.yida.framework.lucene5.util;

import java.io.IOException;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
/**
 * IndexSearcher工具类
 * @author Lanxiaowei
 *
 */
public class IndexSearcherManager {
	private IndexSearcher currentIndexSearcher;
    private boolean reopening;

    public IndexSearcherManager(IndexReader reader) throws IOException {
        this.currentIndexSearcher = new IndexSearcher(reader);
    }

    public synchronized IndexSearcher getIndexSearcher() {
        currentIndexSearcher.getIndexReader().incRef();
        return currentIndexSearcher;
    }

    public synchronized void releaseIndexSearcher(IndexSearcher indexSearcher) throws IOException {
        indexSearcher.getIndexReader().decRef();
    }

    private synchronized void swapIndexeSearcher(IndexSearcher newIndexSearcher) throws IOException {
        releaseIndexSearcher(currentIndexSearcher);
        currentIndexSearcher = newIndexSearcher;
    }

    public void close() throws IOException {
        swapIndexeSearcher(null);
    }

    private synchronized void startReopen() throws InterruptedException {
        while (reopening) {
            wait();
        }
        reopening = true;
    }

    private synchronized void doneReopen() {
        reopening = false;
        notifyAll();
    }

    public void tryToReopen() {
        try {
        	startReopen();
            IndexSearcher indexSearcher = getIndexSearcher();
            try {
                //IndexReader newIndexReader = currentIndexSearcher.getIndexReader().reopen();
            	IndexReader newIndexReader = DirectoryReader.openIfChanged((DirectoryReader)currentIndexSearcher.getIndexReader());
                if (newIndexReader != currentIndexSearcher.getIndexReader()) {
                    IndexSearcher newIndexSearcher = new IndexSearcher(newIndexReader);
                    swapIndexeSearcher(newIndexSearcher);
                }
            } catch (IOException e) {
				e.printStackTrace();
			} finally {
                try {
					releaseIndexSearcher(indexSearcher);
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        } catch (InterruptedException e1) {
			e1.printStackTrace();
		} finally {
            doneReopen();
        }
    }
}
