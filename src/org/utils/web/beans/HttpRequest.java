package org.utils.web.beans;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.utils.web.HttpUtils;
import org.utils.web.IOUtils;

public class HttpRequest {

	public enum Method {
		GET, POST, HEAD, TRACE, PUT, DELETE, OPTIONS, CONNECT
	}
	
	private String method;
	private URL url;
	private int timeout = 15000;
	private String charset = "UTF-8";
	private int maxBodySizeBytes = 5 * 1024 * 1024;
	private String userAgent = HttpString.ua_IE6;
	private String referer = HttpString.ref_baidu;
	private boolean followRedirects = true;
	private Map<String, String> requestHeader = new LinkedHashMap<String, String>();
	private Map<String, String> requestDataMap;
	private String requestData;
	private String requestBae64InputStream;
	
	public HttpRequest() {
		
	}

	public HttpRequest(URL url) {
		this.url = url;
	}

	public HttpRequest method(Method method) {
		this.method = method.name();
		return this;
	}

	public HttpRequest url(URL url) {
		this.url = url;
		return this;
	}

	public HttpRequest timeout(int timeout) {
		this.timeout = timeout;
		return this;
	}

	public HttpRequest charset(String charset) {
		this.charset = charset;
		return this;
	}

	public HttpRequest maxBodySizeBytes(int maxBodySizeBytes) {
		this.maxBodySizeBytes = maxBodySizeBytes;
		return this;
	}

	public HttpRequest userAgent(String userAgent) {
		this.userAgent = userAgent;
		return this;
	}

	public HttpRequest referer(String referer) {
		this.referer = referer;
		return this;
	}

	public HttpRequest followRedirects(boolean followRedirects) {
		this.followRedirects = followRedirects;
		return this;
	}

	public HttpRequest header(Map<String, String> requestHeader) {
		this.requestHeader.putAll(requestHeader);
		return this;
	}

	public HttpRequest data(Map<String, String> requestDataMap) {
		this.requestDataMap = requestDataMap;
		return this;
	}

	public HttpRequest data(String requestData) {
		this.requestData = requestData;
		return this;
	}

	public HttpRequest data(InputStream in) {
		try {
			this.requestBae64InputStream = Base64.encodeBase64String(IOUtils.toByteArray(in));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

	public String getMethod() {
		return method;
	}

	public URL getUrl() {
		return url;
	}

	public int getTimeout() {
		return timeout;
	}

	public String getCharset() {
		return charset;
	}

	public int getMaxBodySizeBytes() {
		return maxBodySizeBytes;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public String getReferer() {
		return referer;
	}

	public boolean isFollowRedirects() {
		return followRedirects;
	}

	public Map<String, String> getRequestHeader() {
		return requestHeader;
	}

	public Map<String, String> getRequestDataMap() {
		return requestDataMap;
	}

	public String getRequestData() {
		return requestData;
	}

	public String getRequestBae64InputStream() {
		return requestBae64InputStream;
	}

	/**               
	 * 发送HTTP GET 请求
	 * @return
	 */
	public HttpResponse get() {
		this.method(Method.GET);
		return HttpUtils.httpRequest(this);
	}

	/**
	 * 发送HTTP POST 请求
	 * @return
	 */
	public HttpResponse post() {
		this.method(Method.POST);
		return HttpUtils.httpRequest(this);
	}
	/**
	 * Head
	 * @return
	 */
	public HttpResponse head() {
		this.method(Method.HEAD);
		return HttpUtils.httpRequest(this);
	}
	
	public static String[] getHtmlNode(String url) {
//		File f=new File("/User/jeary/1.txt");
//		OutputStream out=new FileOutputStream(f);
//		try {
//			out.write("123".getBytes());
//			out.close();
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
		
		
		String[] node = null;
		try {
			HttpResponse response = new HttpRequest(new URL(url)).referer(HttpString.ref_baidu).userAgent(HttpString.ua_Baiduspider).get();
			Document document = response.parse();
			List<Node> l = document.childNodes();
			String s = "";
			for (int i = 0; i < l.size(); i++) {
				 s += l.get(i).toString();
			}
			node = s.split("\n");
			System.out.println("页面响应长度："+s.length());
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return node;
	}

}
