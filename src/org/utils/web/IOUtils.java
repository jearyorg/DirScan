package org.utils.web;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class IOUtils extends org.apache.commons.io.IOUtils{

	/**
     * InputStream 转字符串
     *
     * @param in
     * @return
     */
    public static String inputStreamToString(InputStream in) {
        int a = -1;
        byte[] b = new byte[1024];
        StringBuilder sb = new StringBuilder();
        try {
            while ((a = in.read(b)) != -1) {
                sb.append(new String(b, 0, a));
            }
        } catch (IOException e) {
            sb.append(StringUtils.exceptionToString(e));
        }
        return sb.toString();
    }

    /**
     * 读取文件到字符串
     *
     * @param path
     * @return
     */
    public static String readFileToString(String path) {
        StringBuilder sb = new StringBuilder();
        try {
            int a = 0;
            byte[] b = new byte[2048];
            InputStream in = new FileInputStream(path);
            while ((a = in.read(b)) != -1) {
                sb.append(new String(b, 0, a));
            }
        } catch (IOException e) {
            sb.append(StringUtils.exceptionToString(e));
        }
        return sb.toString();
    }
	
}
