package org.webwul.target;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class Target {
	public static String path="";
	public static Queue<String> get(){
		Queue<String> l=new LinkedBlockingQueue<String>();
		try {
			File file = new File(path);
			BufferedReader reader = null;
			String url = null;
			reader = new BufferedReader(new FileReader(file), 5 * 1024 * 1024);
			while ((url = reader.readLine()) != null) {
				l.offer(url);
			}
		} catch (Exception e) {
			System.out.println("error:" + e.getMessage());
			e.printStackTrace();
		}
		return l;
	}
	
	public static Queue<String> getUrl(String upath){
		Queue<String> l=new LinkedBlockingQueue<String>();
		try {
			File file = new File(upath);
			BufferedReader reader = null;
			String url = null;
			reader = new BufferedReader(new FileReader(file), 5 * 1024 * 1024);
			while ((url = reader.readLine()) != null) {
				l.offer(url);
			}
		} catch (Exception e) {
			System.out.println("error:" + e.getMessage());
			e.printStackTrace();
		}
		return l;
	}
	
	public static BufferedReader getBreader(String tpath) throws FileNotFoundException{
		return new BufferedReader(new InputStreamReader(new FileInputStream(tpath)));
	}

}
