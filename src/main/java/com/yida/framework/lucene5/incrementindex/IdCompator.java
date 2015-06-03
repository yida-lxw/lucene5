package com.yida.framework.lucene5.incrementindex;

import java.util.Comparator;

public class IdCompator implements Comparator<Person>{
	public int compare(Person o1, Person o2) {
		if(o2.getId() > o1.getId()) {
			return 1;
		}
		if(o2.getId() < o1.getId()) {
			return -1;
		}
		return 0;
	}
}
