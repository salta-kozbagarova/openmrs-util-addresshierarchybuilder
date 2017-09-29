package kz.salikhanova.addresshierarchybuilder.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class StreetsUtil {

	private static String APP_DATA = System.getenv("APPDATA")+File.separator+"addresshierarchybuilder";
	
	public static String prepare() throws IOException, UnirestException{
		File appData = new File(APP_DATA);
		if(!appData.exists()){
			appData.mkdirs();
		}
		File file = new File(APP_DATA+File.separator+"streets"+System.currentTimeMillis()+".csv");
		FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
		BufferedWriter bw = new BufferedWriter(fw);
		String url = "http://www.blizko.kz/streets?page=";
		HttpResponse<String> response = null;
		String content = null;
		for (int i = 1; i < 9; i++) {
			response = Unirest.get(url+i).asString();
			content = response.getBody();
			int searchIndex, startIndex;
			searchIndex = content.indexOf("<a href=\"http://www.blizko.kz/street/ulitsa");
			while(searchIndex >= 0){
				startIndex = content.indexOf(">", searchIndex);
				if(startIndex<0){
					break;
				}
				startIndex++;
				int lastIndex = content.indexOf("</a>", startIndex);
				bw.write(content.substring(startIndex, lastIndex).trim());
				bw.newLine();
				//System.out.println(content.substring(startIndex, lastIndex).trim());
				content = content.substring(startIndex);
				searchIndex = content.indexOf("<a href=\"http://www.blizko.kz/street/ulitsa");
			}
		}
		if(bw != null){
			bw.close();
		}
		if(fw != null){
			fw.close();
		}
		return file.getAbsoluteFile().toString();
	}
}
