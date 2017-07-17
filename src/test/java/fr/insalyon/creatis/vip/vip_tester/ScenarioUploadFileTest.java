package fr.insalyon.creatis.vip.vip_tester;

import fr.insalyon.creatis.vip.client.data.model.UploadData;
import fr.insalyon.creatis.vip.client.processing.api.DefaultApi;
import fr.insalyon.creatis.vip.client.processing.model.Execution;
import fr.insalyon.creatis.vip.client.processing.model.Execution.StatusEnum;
import fr.insalyon.creatis.vip.client.processing.model.Pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import fr.insalyon.creatis.vip.client.processing.api.DefaultApi;

public class ScenarioUploadFileTest {

	private VipTesterHelper vth = new VipTesterHelper();
	private DefaultApi client = vth.getDefaultApi();
	private static Logger logger = LoggerFactory.getLogger(ScenarioKillAndRestartTest.class);
	private fr.insalyon.creatis.vip.client.data.api.DefaultApi clientData = vth.getdefaultApiData();
	
	@Test
	public void scenario7() throws Exception{
		
		// directory of upload and output files creation
		clientData.createPath("vip://vip.creatis.insa-lyon.fr/vip/Home/uploadTest/");
		boolean pathExists = clientData.doesPathExists("vip://vip.creatis.insa-lyon.fr/vip/Home/uploadTest/");
		logger.debug("path existance: {}", pathExists);
		
		// upload data
		UploadData data = new UploadData();
		data.setUri("vip://vip.creatis.insa-lyon.fr/vip/Home/uploadTest/listeF");
		data.setPathContent("/home/alikari/Documents/listeF");
		clientData.uploadFile(data);
		
		// check of pipeline's parameters
		Pipeline pip = client.getPipeline("GrepTest%2F1.1");
		logger.debug("grep pipeline: {}", pip);
		
		// creation of Execution object 
		Execution testExe = new Execution();
		testExe.setName("pomme");
		testExe.setPipelineIdentifier("GrepTest/1.1");
		Map<String,Object> testMap = new HashMap<String, Object>();
		testMap.put("text", "prune");
		testMap.put("file", "/vip/Home/uploadTest/listeF");
		testMap.put("output", "coconut");
		testMap.put("results-directory", "/vip/Home/uploadTest");
		testExe.setInputValues(testMap);
		
		client.initAndStartExecution(testExe);
	}
}
