package com.yida.framework.lucene5.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 一些工具方法
 * 
 * @author Lanxiaowei
 * 
 */
public class Tools {
	public static int bytes2int(byte[] b) {
		int mask = 0xff;
		int temp = 0;
		int res = 0;
		for (int i = 0; i < 4; i++) {
			res <<= 8;
			temp = b[i] & mask;
			res |= temp;
		}
		return res;
	}

	public static byte[] int2bytes(int num) {
		byte[] b = new byte[4];
		for (int i = 0; i < 4; i++) {
			b[i] = (byte) (num >>> (24 - i * 8));
		}
		return b;
	}

	public static int nextInt(int min, int max) {
		return min + (int) (Math.random() * ((max - min) + 1));
	}

	/**
	 * 字节数组转化为输入流
	 * 
	 * @param bytes
	 * @return
	 */
	public static InputStream bytes2InputStream(byte[] bytes) {
		return new ByteArrayInputStream(bytes);
	}

	/**
	 * Java反序列化
	 * 
	 * @param is
	 * @return
	 */
	public static Object deSerialize(InputStream is) {

		try {
			ObjectInputStream oin = new ObjectInputStream(is);
			return oin.readObject();
		} catch (ClassNotFoundException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}

	public static List<String> getPermutationSentence(List<List<String>> termArrays, int start) {
		if (null == termArrays || termArrays.size() <= 0) {
			return Collections.emptyList();
		}

		int size = termArrays.size();
		if (start < 0 || start >= size) {
			return Collections.emptyList();
		}

		if (start == size - 1) {
			return termArrays.get(start);
		}

		List<String> strings = termArrays.get(start);

		List<String> permutationSentences = getPermutationSentence(termArrays,
				start + 1);

		if (null == strings || strings.size() <= 0) {
			return permutationSentences;
		}

		if (null == permutationSentences || permutationSentences.size() <= 0) {
			return strings;
		}

		List<String> result = new ArrayList<String>();
		for (String pre : strings) {
			for (String suffix : permutationSentences) {
				result.add(pre + suffix);
			}
		}
		return result;
	}
}
