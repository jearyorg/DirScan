package org.utils.web;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class StringUtils extends org.apache.commons.lang.StringUtils{

    /**
     * 对象值非空判断,判断输入对象是否是空值
     *
     * @param obj
     * @return
     */
    public static boolean isNotEmpty(Object obj) {
        if (obj == null) {
            return false;
        }
        return !"".equals(String.valueOf(obj).trim());
    }

    /**
     * 验证输入的串是否是数字
     *
     * @param str
     * @return
     */
    public static boolean isNum(String str) {
        return str.trim().replaceAll("[0-9]+", "").length() == 0;
    }

    /**
     * 数组切割，把一个数组用分隔符切分成字符串
     *
     * @param array
     * @param separator
     * @return
     */
    public static String join(Object[] array, String separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                sb.append(separator);
            }
            if (array[i] != null) {
                sb.append(array[i]);
            }
        }
        return sb.toString();
    }

    /**
     * List切割，把一个数组用分隔符切分成字符串
     *
     * @param ls
     * @param separator
     * @return
     */
    public static String join(List<?> ls, String separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ls.size(); i++) {
            if (i > 0) {
                sb.append(separator);
            }
            if (ls.get(i) != null) {
                sb.append(ls.get(i));
            }
        }
        return sb.toString();
    }

    /**
     * 简单的数组包含判断
     *
     * @param array
     * @param objectToFind
     * @return
     */
    public static boolean ArrayContains(Object[] array, Object objectToFind) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] != null && ("" + array[i]).indexOf("" + objectToFind) != -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取系统换行分割线
     *
     * @return
     */
    public static String getSeparator() {
        if ("\r".equals(System.getProperty("line.separator"))) {
            return "\\r";//linux
        } else if ("\r\n".equals(System.getProperty("line.separator"))) {
            return "\\r\\n";//windows
        } else {
            return "\\n";//mac
        }
    }

    /**
     * 获取uuid
     *
     * @return
     */
    public static synchronized String getUUID() {
        return UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
    }

    /**
     * unicode 转换成 utf-8
     *
     * @param str
     * @return
     */
    public static String unicodeToUtf8(String str) {
        if (str == null || str.length() < 1 || !Pattern.compile("\\\\u").matcher(str).find()) {
            return str;
        }
        char aChar;
        int len = str.length();
        StringBuffer outBuffer = new StringBuffer(len);
        for (int x = 0; x < len;) {
            aChar = str.charAt(x++);
            if (aChar == '\\') {
                aChar = str.charAt(x++);
                if (aChar == 'u') {
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = str.charAt(x++);
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Malformed   \\uxxxx   encoding.");
                        }
                    }
                    outBuffer.append((char) value);
                } else {
                    if (aChar == 't') {
                        aChar = '\t';
                    } else if (aChar == 'r') {
                        aChar = '\r';
                    } else if (aChar == 'n') {
                        aChar = '\n';
                    } else if (aChar == 'f') {
                        aChar = '\f';
                    }
                    outBuffer.append(aChar);
                }
            } else {
                outBuffer.append(aChar);
            }
        }
        return outBuffer.toString();
    }

    /**
     * 获取当前系统时间
     *
     * @return
     */
    public static String getCurrentTime() {
        return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
    }

    /**
     * 帐号正则验证
     *
     * @param account
     * @return
     */
    public static boolean accountValidate(String account) {
        return Pattern.compile("^[a-zA-Z0-9|-|_]{2,20}$").matcher(account).find();
    }

    /**
     * 邮箱正则验证
     *
     * @param mail
     * @return
     */
    public static boolean mailValidate(String mail) {
        return Pattern.compile("^([a-zA-Z0-9_\\.\\-])+\\@(([a-zA-Z0-9\\-])+\\.)+([a-zA-Z0-9]{2,4})+$").matcher(mail).find();
    }

    /**
     * 手机正则验证
     *
     * @param cellPhone
     * @return
     */
    public static boolean cellPhoneValidate(String cellPhone) {
        return Pattern.compile("^1[3|4|5|7|8][0-9]\\d{8}$").matcher(cellPhone).find();
    }
    
    /**
     * 字符串首字母大写
     * @param s
     * @return
     */
    public static String toUpperCaseFirstOne(String str) {
    	if(isNotEmpty(str)){
    		char f = str.charAt(0);
    		if (Character.isUpperCase(f)) {
    			return str;
    		} else {
    			return str.replaceFirst(String.valueOf(f), String.valueOf(f).toUpperCase());
    		}
    	}
		return str;
    }
    
    /**
     * Exception 转换成字符串
     *
     * @param e
     * @return
     */
    public static String exceptionToString(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw, true));
        return sw.toString();
    }

}
