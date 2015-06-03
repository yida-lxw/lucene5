package com.yida.framework.lucene5.util;
/**
 * @ClassName: HighlighterParam
 * @Description: 高亮器参数对象
 * @author Lanxiaowei
 * @date 2014-3-30 下午12:22:08
 */
public class HighlighterParam {
	/**是否需要设置高亮*/
	private boolean highlight;
	/**需要设置高亮的属性名*/
	private String fieldName;
	/**高亮前缀*/
	private String prefix;
	/**高亮后缀*/
	private String stuffix;
	/**显示摘要最大长度*/
	private int fragmenterLength;
	public boolean isHighlight() {
		return highlight;
	}
	public void setHighlight(boolean highlight) {
		this.highlight = highlight;
	}
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public String getStuffix() {
		return stuffix;
	}
	public void setStuffix(String stuffix) {
		this.stuffix = stuffix;
	}
	public int getFragmenterLength() {
		return fragmenterLength;
	}
	public void setFragmenterLength(int fragmenterLength) {
		this.fragmenterLength = fragmenterLength;
	}
	public HighlighterParam(boolean highlight, String fieldName, String prefix, String stuffix, int fragmenterLength) {
		this.highlight = highlight;
		this.fieldName = fieldName;
		this.prefix = prefix;
		this.stuffix = stuffix;
		this.fragmenterLength = fragmenterLength;
	}
	
	public HighlighterParam(boolean highlight, String fieldName, int fragmenterLength) {
		this.highlight = highlight;
		this.fieldName = fieldName;
		this.fragmenterLength = fragmenterLength;
	}
	
	public HighlighterParam(boolean highlight, String fieldName, String prefix, String stuffix) {
		this.highlight = highlight;
		this.fieldName = fieldName;
		this.prefix = prefix;
		this.stuffix = stuffix;
	}
	public HighlighterParam() {
	}
}
