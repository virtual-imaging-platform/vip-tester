package fr.insalyon.creatis.vip.vip_tester;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.insalyon.creatis.vip.client.processing.api.DefaultApi;
import fr.insalyon.creatis.vip.client.data.ApiException;
import fr.insalyon.creatis.vip.client.data.model.Path;
import fr.insalyon.creatis.vip.client.processing.ApiClient;
import fr.insalyon.creatis.vip.client.processing.model.Execution;

public class VipTesterHelper {
	
	private Properties prop = null;
	private String apikey = null;
	private DefaultApi defaultApiClient = null;
	private fr.insalyon.creatis.vip.client.data.api.DefaultApi defaultApiClientData = null;
	private static Logger logger = LoggerFactory.getLogger(VipTesterHelper.class);

	
	public VipTesterHelper(){
		initProperties();
		this.apikey = System.getProperty("apikey");
		if (this.apikey == null) throw new RuntimeException("No API key found in jvm parameters");
		initClient(prop.getProperty("viptest.additiontest.url"), apikey);
		initClientData(prop.getProperty("viptest.additiontest.url"), apikey);
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
	
	public String getUriPrefix(){
		return prop.getProperty("viptest.uriprefix");
	}
	
	public DefaultApi getDefaultApi(){
		return defaultApiClient;
	}
	
	public fr.insalyon.creatis.vip.client.data.api.DefaultApi getdefaultApiData(){
		return defaultApiClientData;
	}
	
	private void initProperties() {
		prop = new Properties();
		String propertiesLocation = "/testVipAdditiontest.properties";
		try (
			InputStream ip = this.getClass().getResourceAsStream(propertiesLocation);
		) {
			prop.load(ip);
		} catch(IOException ioe){
			logger.error("Error loading properties  {}", propertiesLocation, ioe);
			throw new RuntimeException("No properties file found. Aborting.", ioe);
		}
	}
	
	private void initClient(String url, String apiKey){
		ApiClient testAPiclient = new ApiClient();
		testAPiclient.setBasePath(url);
		testAPiclient.setApiKey(apiKey);
		defaultApiClient =  new DefaultApi(testAPiclient);
	}
	
	private void initClientData(String url, String apiKey){
		fr.insalyon.creatis.vip.client.data.ApiClient testAPiclient = new fr.insalyon.creatis.vip.client.data.ApiClient();
		testAPiclient.setBasePath(url);
		testAPiclient.setApiKey(apiKey);
		defaultApiClientData = new fr.insalyon.creatis.vip.client.data.api.DefaultApi(testAPiclient);
	}
	
	public Execution initAdditionExecution(String directory, String name, int n1, int n2){
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
	
	public Execution modifExecution(String newName, long newTimeout){ // Should I add pipelineId ??? 
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
    	
    	for(i = 0; i<83;i++){
    		table[i] = (char)j;
    		j++;
    	}

    	for(i=0; i<10;i++){
	    	Random randomer = new Random();
	    	int indice = randomer.nextInt(table.length);
	    	aleaChar = table[indice];
	    	aleaName += aleaChar;
    	}
    	return aleaName;
	}
	
	public Path createDirectory(String relatifPath) throws ApiException{
		return defaultApiClientData.createPath(getUriPrefix()+relatifPath);
	}
}
