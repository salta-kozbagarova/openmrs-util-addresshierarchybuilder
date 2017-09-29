package kz.salikhanova.addresshierarchybuilder.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.exceptions.UnirestException;

public class AddressHierarchyBuilder {

	private static String USER_AGENT = "Mozilla/5.0";
	
	private static String EGOV_API = "http://data.egov.kz/api/v2/kato/data?pretty";
	
	private static String APP_DATA = System.getenv("APPDATA")+File.separator+"addresshierarchybuilder";
	
	private File file;
	
	private FileWriter fw = null;
	
	private BufferedWriter bw = null;
	
	private FileReader fr = null;
	
	private BufferedReader streetsReader = null;
	
	ArrayList<String> parentNames = null;
	
	JsonParser jsonParser = null;
	
	JsonElement jsonElement = null;
	
	JsonArray jsonArray = null;
	
	BufferedReader br = null;
	
	JsonObject addressObj = null;
	
	String urlParam;
	
	String completeUrl;
	
	String streetsFilename;
	
	URL url;
	
	HttpURLConnection con;
	
	public void start() throws IOException, UnirestException{
		File appData = new File(APP_DATA);
		if(!appData.exists()){
			appData.mkdirs();
		}
		file = new File(APP_DATA+File.separator+"addresses"+System.currentTimeMillis()+".csv");
		fw = new FileWriter(file.getAbsoluteFile(),true);
        bw = new BufferedWriter(fw);

        streetsFilename = StreetsUtil.prepare();
        fr = new FileReader(streetsFilename);
        streetsReader = new BufferedReader(fr);
        streetsReader.mark(1000000);
        getData(null, null, 2);
        System.out.println("Address Hierarchy Building is successfully completed!");
        System.out.println("Directory: "+file.getAbsoluteFile().toString());
        end();
	}
	
	public void end() throws IOException{
		if(bw != null){
			bw.close();
		}
		if(fw != null){
			fw.close();
		}
		if(streetsReader != null){
			streetsReader.close();
		}
		if(fr != null){
			fr.close();
		}
	}
	
	private void getData(ArrayList<String> names, Integer parentId, Integer level) throws IOException{
    	urlParam = "&source={%20\"size\":1000,%20\"query\":%20{%20\"bool\":{%20\"must\":[";
    	
    	if(level != null){
    		urlParam += "%20{\"match\":{\"Level\":%20"+level+"}}";
    	}
    	if(level != null && parentId != null){
    		urlParam += ",%20{\"match\":{\"Parent\":%20\""+parentId+"\"}}%20";
    	}
    	urlParam += "]%20}%20}%20}";
    	
        completeUrl = EGOV_API+urlParam;
        
        url = new URL(completeUrl);
		con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", USER_AGENT);
		
		int responseCode = con.getResponseCode();
		System.out.println("Sending 'GET' request to URL : " + completeUrl);
		System.out.println("Response Code : " + responseCode);

		br = new BufferedReader(new InputStreamReader(con.getInputStream()));
		jsonParser = new JsonParser();
		jsonElement = jsonParser.parse(br);
		jsonArray = jsonElement.getAsJsonArray();
		for (JsonElement elem : jsonArray) {
			addressObj = elem.getAsJsonObject();
			if(level == 4){
				if(!streetsReader.ready()){
					bw.write(names.get(0)+";"+names.get(1)+";"+addressObj.get("NameRus").toString().trim().replace("\"", "")+";");
					bw.newLine();
				}
				String line;
				while((line = streetsReader.readLine())!=null){
					bw.write(names.get(0)+";"+names.get(1)+";"+addressObj.get("NameRus").toString().trim().replace("\"", "")+";"+line);
					bw.newLine();
				}
				streetsReader.reset();
			} else{
				parentNames = new ArrayList<String>();
				if(names != null)
					parentNames.addAll(names);
				parentNames.add(addressObj.get("NameRus").toString().trim().replace("\"", ""));
				getData(parentNames, addressObj.get("Id").getAsInt(), level+1);
			}
		}
		if(jsonArray.size()==0 && level!=2){
			if(!streetsReader.ready()){
				bw.write(names.get(0));
				if(level==4){
					bw.write(";"+names.get(1));
				} else{
					bw.write(";");
				}
				bw.write(";"+";");
				bw.newLine();
			}
			String line;
			while((line = streetsReader.readLine())!=null){
				bw.write(names.get(0));
				if(level==4){
					bw.write(";"+names.get(1));
				} else{
					bw.write(";");
				}
				bw.write(";"+";"+line);
				bw.newLine();
			}
			streetsReader.reset();
		}
		
		if(br != null){
			br.close();
		}
    }
}
