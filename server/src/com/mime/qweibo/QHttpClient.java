package com.mime.qweibo;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartBase;
import org.apache.commons.httpclient.methods.multipart.StringPart;

import com.mime.qweibo.utils.QHttpUtil;

public class QHttpClient {

	private static final int CONNECTION_TIMEOUT = 20000;

	public QHttpClient() {

	}

	/**
	 * Using GET method.
	 * 
	 * @param url
	 *            The remote URL.
	 * @param queryString
	 *            The query string containing parameters
	 * @return Response string.
	 * @throws Exception
	 */
	public String httpGet(String url, String queryString) throws Exception {
		String responseData = null;

		if (queryString != null && !queryString.equals("")) {
			url += "?" + queryString;
		}

		HttpClient httpClient = new HttpClient();
		GetMethod httpGet = new GetMethod(url);
		httpGet.getParams().setParameter("http.socket.timeout",
				new Integer(CONNECTION_TIMEOUT));

		try {
			int statusCode = httpClient.executeMethod(httpGet);
			if (statusCode != HttpStatus.SC_OK) {
				System.err.println("HttpGet Method failed: "
						+ httpGet.getStatusLine());
			}
			// Read the response body.
			responseData = httpGet.getResponseBodyAsString();
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			httpGet.releaseConnection();
			httpClient = null;
		}

		return responseData;
	}

	/**
	 * Using POST method.
	 * 
	 * @param url
	 *            The remote URL.
	 * @param queryString
	 *            The query string containing parameters
	 * @return Response string.
	 * @throws Exception
	 */
	public String httpPost(String url, String queryString) throws Exception {
		String responseData = null;
		HttpClient httpClient = new HttpClient();
		PostMethod httpPost = new PostMethod(url);
		httpPost.addParameter("Content-Type",
				"application/x-www-form-urlencoded");
		httpPost.getParams().setParameter("http.socket.timeout",
				new Integer(CONNECTION_TIMEOUT));
		if (queryString != null && !queryString.equals("")) {
			httpPost.setRequestEntity(new ByteArrayRequestEntity(queryString
					.getBytes()));
		}

		try {
			int statusCode = httpClient.executeMethod(httpPost);
			if (statusCode != HttpStatus.SC_OK) {
				System.err.println("HttpPost Method failed: "
						+ httpPost.getStatusLine());
			}
			responseData = httpPost.getResponseBodyAsString();
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			httpPost.releaseConnection();
			httpClient = null;
		}

		return responseData;
	}

	/**
	 * Using POST method with multiParts.
	 * 
	 * @param url
	 *            The remote URL.
	 * @param queryString
	 *            The query string containing parameters
	 * @param files
	 *            The list of image files
	 * @return Response string.
	 * @throws Exception
	 */
	public String httpPostWithFile(String url, String queryString,
			List<QParameter> files) throws Exception {

		String responseData = null;
		url += '?' + queryString;
		HttpClient httpClient = new HttpClient();
		PostMethod httpPost = new PostMethod(url);
		try {
			List<QParameter> listParams = QHttpUtil
					.getQueryParameters(queryString);
			int length = listParams.size() + (files == null ? 0 : files.size());
			Part[] parts = new Part[length];
			int i = 0;
			for (QParameter param : listParams) {
				parts[i++] = new StringPart(param.mName,
						QHttpUtil.formParamDecode(param.mValue), "UTF-8");
			}
			for (QParameter param : files) {
				File file = new File(param.mValue);
				parts[i++] = new FilePart(param.mName, file.getName(), file,
						QHttpUtil.getContentType(file), "UTF-8");
			}

			httpPost.setRequestEntity(new MultipartRequestEntity(parts,
					httpPost.getParams()));

			int statusCode = httpClient.executeMethod(httpPost);
			if (statusCode != HttpStatus.SC_OK) {
				System.err.println("HttpPost Method failed: "
						+ httpPost.getStatusLine());
			}
			responseData = httpPost.getResponseBodyAsString();
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			httpPost.releaseConnection();
			httpClient = null;
		}

		return responseData;
	}
	
	// yuchberry modified copy from weibo4j
	//
	private static class ByteArrayPart extends PartBase {
		private byte[] mData;
		private String mName;
		public ByteArrayPart(byte[] data, String name, String type) throws IOException {
			super(name, type, "UTF-8", "binary");
			mName = name;
			mData = data;
		}
		protected void sendData(OutputStream out) throws IOException {
			out.write(mData);
		}
		protected long lengthOfData() throws IOException {
			return mData.length;
		}
	    protected void sendDispositionHeader(OutputStream out) throws IOException {
	    	super.sendDispositionHeader(out);
	    	StringBuilder buf = new StringBuilder();
	    	buf.append("; filename=\"").append(mName).append("\"");
	    	out.write(buf.toString().getBytes());
	    }
	}
	
	public String httpPostWithFile(String url, String queryString,
			byte[] filesBuffer,String fileType) throws Exception {

		String responseData = null;
		url += '?' + queryString;
		HttpClient httpClient = new HttpClient();
		PostMethod httpPost = new PostMethod(url);
		try {
			List<QParameter> listParams = QHttpUtil
					.getQueryParameters(queryString);
			int length = listParams.size() + (filesBuffer == null ? 0 : 1);
			Part[] parts = new Part[length];
			int i = 0;
			for (QParameter param : listParams) {
				parts[i++] = new StringPart(param.mName,
						QHttpUtil.formParamDecode(param.mValue), "UTF-8");
			}
			
			parts[i] = new ByteArrayPart(filesBuffer,"pic",fileType);
			
			httpPost.setRequestEntity(new MultipartRequestEntity(parts,
					httpPost.getParams()));

			int statusCode = httpClient.executeMethod(httpPost);
			if (statusCode != HttpStatus.SC_OK) {
				System.err.println("HttpPost Method failed: "
						+ httpPost.getStatusLine());
			}
			responseData = httpPost.getResponseBodyAsString();
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			httpPost.releaseConnection();
			httpClient = null;
		}

		return responseData;
	}

}
