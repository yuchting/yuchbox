package com.mime.qweibo;

public class QWeiboType {
	/**
	 * The format of response string.
	 *
	 */
	public enum ResultType {
		ResultType_Xml, ResultType_Json
	}
	/**
	 * The page flag to fetch messages.
	 *
	 */
	public enum PageFlag {
		PageFlag_First, PageFlag_Next, PageFlag_Last
	}
}
