package org.utils.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Map;
//import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.utils.web.beans.HttpRequest;
import org.utils.web.beans.HttpResponse;

public class HttpUtils {

//	private final static Logger logger = Logger.getLogger("cat");

	private static final Pattern HTML_CHARSET_PATTERN = Pattern
			.compile("(?i)<meta.*\\bcharset\\s*=\\s*(?:\"|')?([^\\s,;\"']*)");

	/**
	 * 获取HTTP请求的文件类型,截取URI后缀部分
	 * 
	 * @param url
	 * @return
	 */
	public static String getFileType(URL url) {
		String path = "".equals(url.getPath()) ? "/" : url.getPath();
		String file = path.substring(path.lastIndexOf("/"));
		return file.substring(file.lastIndexOf(".") + 1, file.length());
	}

	/**
	 * 循环遍历Map所有元素拼成HTTP QueryString
	 * 
	 * @param args
	 * @param encoding
	 * @return
	 */
	public static String toURLParameterString(Map<String, String> args,
			String encoding) {
		StringBuilder sb = new StringBuilder();
		if (args != null) {
			int i = 0;
			for (String a : args.keySet()) {
				try {
					if (i > 0) {
						sb.append("&");
					}
					i++;
					sb.append(a).append("=")
							.append(URLEncoder.encode(args.get(a), encoding));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}

	/**
	 * 从HTML meta 标签里 提取网页编码(charset)
	 * 
	 * @param html
	 * @return
	 */
	public static String getCharsetFromHTMLBody(String html) {
		String encoding = null;
		if (html != null) {
			Matcher m = HTML_CHARSET_PATTERN.matcher(html);
			if (m.find()) {
				String charset = m.group(1).trim();
				charset = charset.replace("charset=", "");
				if (charset.length() == 0)
					return null;
				try {
					if (Charset.isSupported(charset)) {
						encoding = charset.toUpperCase(Locale.ENGLISH);
					}
				} catch (Exception e) {
					return "UTF-8";
				}
			}
		}
		return encoding;
	}

	/**
	 * 解析HTML编码 这个方法只做了从HTML代码里提取出meta标签里面的charset和编码自动识别
	 * 如果需要实现更复杂的编码识别可以参考org.jsoup.helper.DataUtil解析字节流编码
	 * 
	 * @param bodyByte
	 * @return
	 */
	public static String parseHTMLCharset(byte[] bodyByte) {
		String encoding = null;
		try {
			encoding = HttpUtils.getCharsetFromHTMLBody(new String(bodyByte));
			if (encoding == null) {
				int code = new BytesEncodingDetect().detectEncoding(bodyByte);
				encoding = BytesEncodingDetect.htmlname[code];
			}
		} catch (Exception e) {
			//System.out.println("parseHTMLCharset error:"+e.getMessage());
		}
		return encoding != null ? encoding : "UTF-8";
	}

	/**
	 * 设置HTTP请求基本属性 设置HTTP读写超时时间、是否跳转跟随、User-Agent、referer
	 * 
	 * @param httpURLConnection
	 * @param request
	 * @throws IOException
	 */
	public static void setRequestProperties(
			HttpURLConnection httpURLConnection, HttpRequest request)
			throws IOException {
		httpURLConnection.setConnectTimeout(request.getTimeout());
		httpURLConnection.setReadTimeout(request.getTimeout());
		HttpURLConnection.setFollowRedirects(request.isFollowRedirects());
		if (StringUtils.isNotEmpty(request.getMethod())) {
			if (!"GET".equals(request.getMethod())) {
				httpURLConnection.setDoInput(true);
				httpURLConnection.setDoOutput(true);
			}
			httpURLConnection.setRequestMethod(request.getMethod()
					.toUpperCase());
		}
		httpURLConnection.setRequestProperty("User-Agent",
				request.getUserAgent());
		httpURLConnection.setRequestProperty("Referer", request.getReferer());
	}

	/**
	 * 设置Http请求数据
	 * 
	 * @param httpURLConnection
	 * @param request
	 * @param data
	 * @throws IOException
	 */
	public static void setRequestData(HttpURLConnection httpURLConnection,
			HttpRequest request, String data) throws IOException {
		// 设置HTTP请求头
		Map<String, String> headers = request.getRequestHeader();
		for (String key : headers.keySet()) {
			httpURLConnection.setRequestProperty(key, headers.get(key));
		}

		// 设置HTTP非GET请求参数
		if (StringUtils.isNotEmpty(request.getMethod())
				&& !"GET".equals(request.getMethod())) {
			if (StringUtils.isNotEmpty(request.getRequestDataMap())
					|| StringUtils.isNotEmpty(request.getRequestData())
					|| StringUtils.isNotEmpty(request
							.getRequestBae64InputStream())) {
				OutputStream out = httpURLConnection.getOutputStream();
				// 如果有设置HTTP请求的InputStream则写InputStream忽略RequestDataMap
				if (StringUtils
						.isNotEmpty(request.getRequestBae64InputStream())) {
					out.write(Base64.decodeBase64(request
							.getRequestBae64InputStream()));
				} else {
					out.write(data.getBytes());
				}
				out.flush();
				out.close();
			}
		}
	}

	/**
	 * HTTP请求发送成功后设置HttpResponse
	 * 
	 * @param httpURLConnection
	 * @param response
	 * @throws IOException
	 */
	public static void setResponse(HttpURLConnection httpURLConnection,
			HttpResponse response) throws IOException {
		response.setStatusCode(httpURLConnection.getResponseCode());
		response.setStatusMessage(httpURLConnection.getResponseMessage());
		response.setContentType(httpURLConnection.getContentType());
		response.setHeader(httpURLConnection.getHeaderFields());
		response.setLastModified(httpURLConnection.getLastModified());
	}

	/**
	 * 发送HTTP请求,发送HTTP请求前先解析一次DNS记录,如果解析正常继续请求
	 * 
	 * @param request
	 * @return
	 */
	public static HttpResponse httpRequest(HttpRequest request) {
		HttpURLConnection httpURLConnection = null;
		InputStream in = null;
		HttpResponse response = new HttpResponse(request.getUrl());
		try {
			//logger.info("请求:" + request.getUrl());
			response.setRequestTime(System.currentTimeMillis());// 请求开始时间
			try {
				response.dnsParse();// DNS解析

				String protocol = request.getUrl().getProtocol();
				if (!protocol.equals("http") && !protocol.equals("https")) {
					throw new MalformedURLException("只支持 http & https 请求协议.");
				} else if ("https".equalsIgnoreCase(protocol)) {
					SslUtils.ignoreSsl();
				}

				String data = request.getRequestDataMap() != null ? HttpUtils
						.toURLParameterString(request.getRequestDataMap(),
								request.getCharset()) : request
						.getRequestData();

				URL url = null;
				if (!StringUtils.isNotEmpty(request.getMethod())
						|| "GET".equalsIgnoreCase(request.getMethod())
						&& StringUtils.isNotEmpty(data)) {
					url = new URL(request.getUrl()
							+ (StringUtils.isNotEmpty(request.getUrl()
									.getQuery()) ? "&" : "?") + data);
				} else {
					url = request.getUrl();
				}

				httpURLConnection = (HttpURLConnection) url.openConnection();
				setRequestProperties(httpURLConnection, request);
				setRequestData(httpURLConnection, request, data);
				httpURLConnection.connect();
				setResponse(httpURLConnection, response);

				// 获取HTTP请求响应内容
				try {
					in = httpURLConnection.getInputStream();
				} catch (IOException e) {
					in = httpURLConnection.getErrorStream();
				}
				if (in != null && in.available() > 0) {
					response.setBase64Data(Base64.encodeBase64String(IOUtils
							.toByteArray(in)));
				}
			} catch (UnknownHostException e) {
				response.setExceptionName(e.toString());
			}
		} catch (IOException e) {
			response.setExceptionName(e.toString());
		} finally {
			if (in != null)
				IOUtils.closeQuietly(in);
			if (httpURLConnection != null)
				httpURLConnection.disconnect();
			response.setResponseTime(System.currentTimeMillis());// 请求结束时间
		}
		return response;
	}

	public static void main(String[] args) {
		try {
			System.out.println("test");
			// Map<String,String> data = new LinkedHashMap<String, String>();
			// data.put("id", "admin");
			// request.referer("http://baidu.com");
			String html = new HttpRequest(new URL("http://www.symzj.gov.cn")).timeout(25000).get().body();
			// System.out.println(JSON.toJSONString(response));
			// Document d=response.parse();
			// System.out.println(d.body().text());
			System.out.println(html);
//			String s=WebUtils.getHtmlContext("http://www.symzj.gov.cn");
//			System.out.println(s);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}