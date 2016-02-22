package org.utils.web.beans;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.utils.web.HttpUtils;

public class HttpResponse {

	private URL url;
	private int statusCode;
	private String statusMessage;
	private String contentType;
	private String base64Data;
	protected transient String data;
	private String charset;
	private String exceptionName;
	private long requestTime;
	private long responseTime;
	private long lastModified;
	private String ip;
	private String hostName;
	private String canonicalHostName;
	private String domain;
	private Map<String, List<String>> header;

	public HttpResponse(URL url) {
		this.url = url;
		this.domain = url.getHost();
	}

	public URL getUrl() {
		return url;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getBase64Data() {
		return base64Data;
	}

	public void setBase64Data(String base64Data) {
		this.base64Data = base64Data;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getExceptionName() {
		return exceptionName;
	}

	public void setExceptionName(String exceptionName) {
		this.exceptionName = exceptionName;
	}

	public long getRequestTime() {
		return requestTime;
	}

	public void setRequestTime(long requestTime) {
		this.requestTime = requestTime;
	}

	public long getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(long responseTime) {
		this.responseTime = responseTime;
	}

	public long getLastModified() {
		return lastModified;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getCanonicalHostName() {
		return canonicalHostName;
	}

	public void setCanonicalHostName(String canonicalHostName) {
		this.canonicalHostName = canonicalHostName;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	public Map<String, List<String>> getHeader() {
		return header;
	}

	public void setHeader(Map<String, List<String>> header) {
		this.header = header;
	}

	public void dnsParse() throws UnknownHostException {
		InetAddress ia = InetAddress.getByName(this.domain);
		this.setIp(ia.getHostAddress());// ip地址
		this.setHostName(ia.getHostAddress());// 主机名
		this.setCanonicalHostName(ia.getCanonicalHostName());// 别名
	}

	/**
	 * 解析HTTP响应正文中的HTML内容为Document树
	 * 
	 * @param response
	 * @return
	 */
	public Document parse() {
		return Jsoup.parse(parseBody(), this.getUrl().toString());
	}

	/**
	 * 解析HTML为字符串 body 字符串转换可能会有乱码问题,为了避免直接使用解析后的HTML字符串默认隐藏了 HTML
	 * body,仅提供方便序列化为json的base64字符串作为输出
	 */
	protected String parseBody() {
		if (this.data == null) {
			byte[] body = Base64.decodeBase64(this.getBase64Data());
			String charset = HttpUtils.parseHTMLCharset(body);
			if (Charset.isSupported(charset)) {
				this.setCharset(charset);
			} else {
				this.setCharset("UTF-8");
			}
			try {
				if (body != null) {
					this.data = new String(body, this.getCharset());
				}
			} catch (Exception e) {
				System.out.println("body null!" + this.url + e.getMessage());
				// return "html is null!";
			}
		}
		return this.data != null ? this.data : "html is null!";
	}

	/**
	 * 获取HTTP原始的html编码后的字符串
	 * 
	 * @return
	 */
	public String body() {
		return parseBody();
	}

}
