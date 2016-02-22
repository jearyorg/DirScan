package org.tools.report;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class ResultOutput {
	
	//http://sourceforge.net/projects/jexcelapi/files/
	
	public static void successWrite(String path,String result){
		try {
			FileUtils.writeStringToFile(new File(path), result+ "\r\n", "UTF-8", true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	

}
