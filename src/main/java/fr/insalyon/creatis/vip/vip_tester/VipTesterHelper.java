package fr.insalyon.creatis.vip.vip_tester;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import fr.insalyon.creatis.vip.client.processing.api.DefaultApi;
import fr.insalyon.creatis.vip.client.processing.ApiClient;
import fr.insalyon.creatis.vip.client.processing.model.Execution;

public class VipTesterHelper {
	
	private Properties prop = null;
	private String apikey = null;
	private DefaultApi defaultApiClient = null;
	private fr.insalyon.creatis.vip.client.data.api.DefaultApi defaultApiClientData = null;
	
	public VipTesterHelper(){
		prop = initProperties();
		this.apikey = System.getProperty("apikey");
		defaultApiClient = initClient(prop.getProperty("viptest.additiontest.url"), apikey);
		defaultApiClientData = initClientData(prop.getProperty("viptest.additiontest.url"), apikey);
	}
	
	public String getAdditionTestPipelineId(){
		return prop.getProperty("viptest.additiontest.pipelineidentifier");
	}
	
	public String getAdditionTestPipelineIdString(){
		return prop.getProperty("viptest.additiontest.pipelineidentifierstring");
	}
	
	public String getAdditionTestTimeCheck(){
		return prop.getProperty("viptest.additiontest.timecheck");
	}
	
	public DefaultApi getDefaultApi(){
		return defaultApiClient;
	}
	
	public fr.insalyon.creatis.vip.client.data.api.DefaultApi getdefaultApiData(){
		return defaultApiClientData;
	}
	
	private Properties initProperties(){
		Properties prop = new Properties();
		try{
			FileInputStream in = new FileInputStream("src/main/resources/testVipAdditiontest.properties");
			try{
				prop.load(in);
				in.close();
			}catch(IOException ioe){
				System.out.println(ioe.getMessage());
			}
		}catch(FileNotFoundException fnfe){
			System.out.println(fnfe.getMessage());
		}
		return prop;
	}
	
	private static DefaultApi initClient(String url, String apiKey){
		ApiClient testAPiclient = new ApiClient();
		testAPiclient.setBasePath(url);
		testAPiclient.setApiKey(apiKey);
		return new DefaultApi(testAPiclient);
	}
	
	private static fr.insalyon.creatis.vip.client.data.api.DefaultApi initClientData(String url, String apiKey){
		fr.insalyon.creatis.vip.client.data.ApiClient testAPiclient = new fr.insalyon.creatis.vip.client.data.ApiClient();
		testAPiclient.setBasePath(url);
		testAPiclient.setApiKey(apiKey);
		return new fr.insalyon.creatis.vip.client.data.api.DefaultApi(testAPiclient);
	}
	
	public Execution initExecution(String directory, String name, int n1, int n2){
		Execution testExe = new Execution();
		testExe.setName(name);
		testExe.setPipelineIdentifier("AdditionTest/0.9");
		Map<String,Object> testMap = new HashMap<String, Object>();
		testMap.put("number1", n1);
		testMap.put("number2", n2);
		testMap.put("results-directory", directory);
		testExe.setInputValues(testMap);
		return testExe;
	}
	
	public Execution modifExecution(String newName, long newTimeout){ // I can add pipelineId ??? 
		Execution body = new Execution();
		body.setName(newName);
		body.setTimeout(newTimeout);
		body.setPipelineIdentifier("AdditionTest/0.9");
		return body;
	}
	
	public String randomSelection(){
    	int i, j = 40 ;
    	char[] table = new char[83];
    	char aleaChar;
    	String aleaName = "";
    	
    	// fill the character table
    	for(i = 0; i<83;i++){
    		table[i] = (char)j;
    		j++;
    	}

    	for(i=0; i<15;i++){
	    	Random randomer = new Random();
	    	int indice = randomer.nextInt(table.length);
	    	aleaChar = table[indice];
	    	aleaName += aleaChar;
    	}
    	return aleaName;
	}
}
