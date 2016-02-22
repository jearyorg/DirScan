package org.tools.dicweak;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.tools.report.ResultOutput;
import org.utils.web.beans.HttpRequest;
import org.utils.web.beans.HttpResponse;
import org.webwul.target.Target;


public class WeakFileScan extends Thread {

	private static BufferedReader br = null;
	static volatile int count = 0; // 记录任务进度
	String url = null;
	public void run() {
		try {
			while ((url = br.readLine()) != null) {
				String ranUrl = checkUrl(url + ranPage());
				boolean ranRequest = isExitsByHead(ranUrl);
				// 随机页面不存在则进行扫描步骤
				if (!ranRequest) {
					List<String> fileList = getWeakRules();
					List<String> successList = new ArrayList<String>();
					boolean falsehood = false;
					for (int i = 0; i < fileList.size(); i++) {
						if (i > 3) {
							if (successList.size() > 1) {
								break;
							} else {
								falsehood = true;
							}
						}
						String file = checkFile(fileList.get(i));
						String scanUrl = checkUrl(url+ hostNameRules(file, url));

						boolean isSuccess = isExitsByHead(scanUrl);

						if (isSuccess) {
							successList.add(scanUrl);
						}
						System.out.println("\033[0;32m[+] [" + count
								+ "] [Scan] [" + scanUrl + "]");
						count++;
					}
					if (falsehood) {
						for (String successUrl : successList) {
							String result = "[+] [" + count + "] [Success] "
									+ successUrl;
							System.out.println("\033[0;31m" + result);
							ResultOutput.successWrite(resultPath, successUrl);
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println("\033[0;33m:( :( :(  Error:" + e.getMessage()+"["+url+"]");
		}
	}

	/**
	 * 随机页面
	 * @return
	 */
	public static String ranPage() {
		String c = "";
		long Temp;
		for (int i = 0; i < 9; i++) {
			Temp = Math.round(Math.random() * (90 - 65) + 65);
			c += (char) (int) Temp + "";
		}
		return "/" + c + "/" + c + ".php";
	}

	/**
	 * 替换字典里的{hostname},{hostname-all}
	 */
	public static String hostNameRules(String fileName, String url) {
		String ha = "{hostname-all}";
		String h = "{hostname}";
		if (fileName.contains(ha)) {
			return fileName.replace(ha, url);
		}
		if (fileName.contains(h)) {
			String[] hostname = url.split("\\.");
			String dirFile = "";
			if (hostname.length > 2) {
				dirFile = fileName.replace(h, hostname[1]);
			} else {
				dirFile = fileName.replace(h, hostname[0]);
			}
			return dirFile;
		}
		return fileName;
	}

	/**
	 * 补全http协议
	 */
	public static String checkUrl(String url) {
		try {
			new URL(url);
		} catch (MalformedURLException e) {
			return "http://" + url;
		}
		return url;
	}

	/**
	 * 补全文件首部斜杠
	 */
	public static String checkFile(String fileName) {
		return fileName.indexOf("/") != 0 ? "/" + fileName : fileName;
	}

	/**
	 * Head请求判断是否存在
	 * @param url
	 * @return
	 * @throws MalformedURLException
	 */
	public static boolean isExitsByHead(String url)
			throws MalformedURLException {
		return new HttpRequest(new URL(url)).followRedirects(false).head().getStatusCode()==HttpURLConnection.HTTP_OK;

	}

	/**
	 * Get请求判断是否存在
	 * @param url
	 * @return
	 * @throws MalformedURLException
	 *             Waiting..
	 */
	public static boolean isExitsByGet(String url) throws MalformedURLException {
		// String urlFormat = checkUrl(url);
		HttpResponse response = new HttpRequest(new URL(url)).get();
		int code = response.getStatusCode();
		String html = response.body();
		boolean is = html.contains("safedog");
		if (is) {
			String result = "\033[0;33m" + url + " find dog!!!!!!";
			System.out.println(result);
			// ResultOutput.successWrite("./result_dog_weakdir.txt", result);
		}
		if (code == HttpURLConnection.HTTP_OK && !is) {
			return true;
		}
		return false;
	}

	/**
	 * 获取目录
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static ArrayList<String> getWeakRules() throws IOException {
		ArrayList<String> rules = new ArrayList<String>();
		rules.addAll(FileUtils.readLines(new File(path), "UTF-8"));
		return rules;
	}

	/**
	 * Main
	 * 
	 * @param args
	 */

	private static String path = ""; // 扫描规则
	private static String resultPath = ""; // 成功输出
	private static String targetFile = "";

	public static void main(String[] args) {

		if (args.length > 1) {
			path = args[1];
		} else {
			System.out
					.println("Usage:java -jar WeakFileScan.jar [target file] [rules file] [output result]");
			return;
		}
		if (args.length > 2) {
			resultPath = args[2];
		}

		targetFile = args[0];

		try {
			long ctime = System.currentTimeMillis();
			br = Target.getBreader(targetFile);
			int threads = 150;
			ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors
					.newFixedThreadPool(threads);
			for (int i = 0; i < threads; i++) {
				pool.execute(new WeakFileScan());
			}
			try {
				pool.shutdown();
				pool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			long etime = System.currentTimeMillis();
			System.out.println("耗时:" + (etime - ctime));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}