package com.yida.framework.lucene5.sort.custom;

import java.io.IOException;

import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldComparatorSource;
/**
 * 域比较器自定义ValueSource
 * @author Lanxiaowei
 *
 */
public class DistanceComparatorSource extends FieldComparatorSource {
	private  int x;  
    private int y;  
     
    public DistanceComparatorSource(int x,int y){  
        this.x = x;  
        this.y = y;  
    }

	@Override
	public FieldComparator<?> newComparator(String fieldname, int numHits,
			int sortPos, boolean reversed) throws IOException {
		return new DistanceSourceLookupComparator(fieldname, numHits,x,y);
	}
}
