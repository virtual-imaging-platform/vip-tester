package fr.insalyon.creatis.vip.vip_tester;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.CharStreams;

import fr.insalyon.creatis.vip.client.processing.api.DefaultApi;
import fr.insalyon.creatis.vip.client.data.ApiException;
import fr.insalyon.creatis.vip.client.data.model.Path;
import fr.insalyon.creatis.vip.client.data.model.UploadData;
import fr.insalyon.creatis.vip.client.processing.ApiClient;
import fr.insalyon.creatis.vip.client.processing.model.Execution;
import fr.insalyon.creatis.vip.client.processing.model.Execution.StatusEnum;

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
	
	public String getGrepTestPipelineId(){
		return prop.getProperty("viptest.greptest.pipelineidentifier");
	}
	
	public String getGrepTestPipelineIdString(){
		return prop.getProperty("viptest.greptest.pipelineidentifierstring");
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
	
	public Execution initGrepExecution(String name, Object[] parameters){
		Execution testExe = new Execution();
		testExe.setName(name);
		testExe.setPipelineIdentifier(getGrepTestPipelineIdString());
		Map<String,Object> testMap = new HashMap<String, Object>();
		testMap.put("results-directory", (String)parameters[0]);
		testMap.put("text", (String)parameters[1]);
		testMap.put("file", (String)parameters[2]);
		testExe.setInputValues(testMap);
		return testExe;
	}
	
	public Execution initAdditionExecution(String name, Object[] parameters){
		Execution testExe = new Execution();
		testExe.setName(name);
		testExe.setPipelineIdentifier(getAdditionTestPipelineIdString());
		Map<String,Object> testMap = new HashMap<String, Object>();
		testMap.put("results-directory", (String)parameters[0]);
		testMap.put("number1", (Integer)parameters[1]);
		testMap.put("number2", (Integer)parameters[2]);	
		testExe.setInputValues(testMap);
		return testExe;
	}
	
	// launch an execution and check its status
	public Execution launchExecution(String pipelineId, String name, Object... parameters) throws Exception{
		Execution exe = null;
		switch(pipelineId){
			case "AdditionTest/0.9":
				exe = initAdditionExecution(name, parameters);
				break;
			case "GrepTest/1.2":
				exe = initGrepExecution(name, parameters);
				break;
			default:
				throw new RuntimeException(pipelineId+"is not a valid pipeline");
		}
		Execution result = defaultApiClient.initAndStartExecution(exe);
		return result;
	}
	
	// download the content of the result file and delete newPath
	public String download(String pipelineId, String executionId, String relatifPath) throws Exception{
		String returnedFile = null;
		switch(pipelineId){
		case "AdditionTest/0.9":
			returnedFile = defaultApiClient.getExecution(executionId).getReturnedFiles().get("output_file").get(0);
			break;
		case "GrepTest/1.2":
			returnedFile = defaultApiClient.getExecution(executionId).getReturnedFiles().get("output").get(0);
			break;
		default:
			throw new RuntimeException("download method is not implmented for: "+pipelineId);
		}
		logger.debug("returned file: {}",defaultApiClient.getExecution(executionId).getReturnedFiles().get("output"));
		String[] split = returnedFile.split("/");	
		String resultDirectory = getUriPrefix()+relatifPath+"/";
		String uri = resultDirectory+split[6]+"/"+split[7];
		String ExecutionResult = defaultApiClientData.downloadFile(uri);
		return ExecutionResult;
//		return "blable";
	}
	
	public UploadData prepareInputFile(String relatifPath){
		//A METTRE DANS VipTesterHelper
		//open test file with java
		UploadData data = null;
		String testFileLocation = "/listeF";
		try (
				InputStream ip = this.getClass().getResourceAsStream(testFileLocation);
			) {
		    String text = null;
		    try (final Reader reader = new InputStreamReader(ip)) {
		        text = CharStreams.toString(reader);
		    }
		  
		    logger.info("content file: {}", text);
		    String content = DatatypeConverter.printBase64Binary(text.getBytes());
		    
			data = initUploadData(relatifPath, content);

			} catch(IOException ioe){
				logger.error("Error loading input file  {}", testFileLocation, ioe);
				throw new RuntimeException("No input file found. Aborting.", ioe);
			}
		return data;
	}
	
	public UploadData initUploadData(String relatifPath, String content){
		UploadData data = new UploadData();
		data.setUri(getUriPrefix()+relatifPath+"/listeFruit");
		data.setPathContent(content);
		return data;
	}
}
