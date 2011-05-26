package com.mime.qweibo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

import com.mime.qweibo.utils.QHttpUtil;

/**
 * Note: This is not really an async http client. You may try
 * Ning(https://github.com/AsyncHttpClient/async-http-client), Apache Geronimo
 * async-http-client, or anything else if you really need.
 * 
 */
public class QAsyncHttpClient {

	private static final int CONNECTION_TIMEOUT = 20000;

	/**
	 * Using asynchronously GET method.
	 * 
	 * @param url
	 *            The remote URL.
	 * @param queryString
	 *            The query string containing parameters
	 * @param callback
	 *            Callback handler.
	 * @param cookie
	 *            Cookie response to handler.
	 * @return Whether request has started.
	 */
	public boolean httpGet(String url, String queryString,
			QAsyncHandler callback, Object cookie) {
		if (url == null || url.equals("")) {
			return false;
		}
		if (queryString != null && !queryString.equals("")) {
			url += "?" + queryString;
		}
		GetMethod httpGet = new GetMethod(url);
		httpGet.getParams().setParameter("http.socket.timeout",
				new Integer(CONNECTION_TIMEOUT));
		mThreadPool.submit(new AsyncThread(httpGet, callback, cookie));
		return true;
	}

	/**
	 * Using asynchronously POST method.
	 * 
	 * @param url
	 *            The remote URL.
	 * @param queryString
	 *            The query string containing parameters
	 * @param callback
	 *            Callback handler.
	 * @param cookie
	 *            Cookie response to handler.
	 * @return Whether request has started.
	 */
	public boolean httpPost(String url, String queryString,
			QAsyncHandler callback, Object cookie) {
		if (url == null || url.equals("")) {
			return false;
		}
		PostMethod httpPost = new PostMethod(url);
		httpPost.addParameter("Content-Type",
				"application/x-www-form-urlencoded");
		httpPost.getParams().setParameter("http.socket.timeout",
				new Integer(CONNECTION_TIMEOUT));
		if (queryString != null && !queryString.equals("")) {
			httpPost.setRequestEntity(new ByteArrayRequestEntity(queryString
					.getBytes()));
		}
		mThreadPool.submit(new AsyncThread(httpPost, callback, cookie));
		return true;
	}

	/**
	 * Using asynchronously POST method with multiParts.
	 * 
	 * @param url
	 *            The remote URL.
	 * @param queryString
	 *            The query string containing parameters
	 * @param callback
	 *            Callback handler.
	 * @param cookie
	 *            Cookie response to handler.
	 * @return Whether request has started.
	 */
	public boolean httpPostWithFile(String url, String queryString,
			List<QParameter> files, QAsyncHandler callback, Object cookie) {
		if (url == null || url.equals("")) {
			return false;
		}
		url += '?' + queryString;
		PostMethod httpPost = new PostMethod(url);
		List<QParameter> listParams = QHttpUtil.getQueryParameters(queryString);
		int length = listParams.size() + (files == null ? 0 : files.size());
		Part[] parts = new Part[length];
		int i = 0;
		for (QParameter param : listParams) {
			parts[i++] = new StringPart(param.mName,
					QHttpUtil.formParamDecode(param.mValue), "UTF-8");
		}
		try {
			for (QParameter param : files) {
				File file = new File(param.mValue);
				parts[i++] = new FilePart(param.mName, file.getName(), file,
						QHttpUtil.getContentType(file), "UTF-8");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}

		httpPost.setRequestEntity(new MultipartRequestEntity(parts, httpPost
				.getParams()));
		mThreadPool.submit(new AsyncThread(httpPost, callback, cookie));
		return true;
	}

	private ExecutorService mThreadPool = Executors.newFixedThreadPool(20);

	/**
	 * Thread for asynchronous HTTP request.
	 * 
	 */
	class AsyncThread extends Thread {

		private HttpMethod mHttpMedthod;
		private QAsyncHandler mAsyncHandler;
		private Object mCookie;

		public AsyncThread(HttpMethod method, QAsyncHandler handler,
				Object cookie) {
			this.mHttpMedthod = method;
			this.mAsyncHandler = handler;
			this.mCookie = cookie;
		}

		@Override
		public void run() {
			String responseData = null;
			HttpClient httpClient = new HttpClient();
			int statusCode = -1;
			try {
				statusCode = httpClient.executeMethod(mHttpMedthod);
				if (statusCode != HttpStatus.SC_OK) {
					System.err.println("HttpMethod failed: "
							+ mHttpMedthod.getStatusLine());
				}
				responseData = mHttpMedthod.getResponseBodyAsString();
			} catch (HttpException e) {
				e.printStackTrace();
				if (mAsyncHandler != null) {
					mAsyncHandler.onThrowable(e, mCookie);
				}
				return;
			} catch (IOException e) {
				e.printStackTrace();
				if (mAsyncHandler != null) {
					mAsyncHandler.onThrowable(e, mCookie);
				}
				return;
			} finally {
				mHttpMedthod.releaseConnection();
				httpClient = null;
			}
			if (mAsyncHandler != null) {
				mAsyncHandler.onCompleted(statusCode, responseData, mCookie);
			}
		}

	}
}
