package fr.insalyon.creatis.vip.vip_tester;

import fr.insalyon.creatis.vip.client.data.model.Path;
import fr.insalyon.creatis.vip.client.data.model.UploadData;
import fr.insalyon.creatis.vip.client.processing.api.DefaultApi;
import fr.insalyon.creatis.vip.client.processing.model.Execution;
import fr.insalyon.creatis.vip.client.processing.model.Execution.StatusEnum;
import fr.insalyon.creatis.vip.client.processing.model.Pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;



import fr.insalyon.creatis.vip.client.processing.api.DefaultApi;

public class ScenarioSearchOfFileTest {
	private VipTesterHelper vth = new VipTesterHelper();
	private DefaultApi client = vth.getDefaultApi();
	private static Logger logger = LoggerFactory.getLogger(ScenarioSearchOfFileTest.class);
	private fr.insalyon.creatis.vip.client.data.api.DefaultApi clientData = vth.getdefaultApiData();

	@Test
	public void scenario8() throws Exception{
		
		clientData.createPath("vip://vip.creatis.insa-lyon.fr/vip/Home/searchFileTest/");
		clientData.doesPathExists("vip://vip.creatis.insa-lyon.fr/vip/Home/searchFileTest/");
		
		int i = 0;
		Execution testExe = new Execution();
		while(i<3){
			
			testExe.setName("pomme");
			testExe.setPipelineIdentifier("GrepTest/1.1");
			Map<String,Object> testMap = new HashMap<String, Object>();
			testMap.put("text", "prune");
			testMap.put("file", "/vip/Home/uploadTest/listeF");
			testMap.put("output", "coconut"+i);
			testMap.put("results-directory", "/vip/Home/searchFileTest");
			testExe.setInputValues(testMap);
			
			client.initAndStartExecution(testExe);
			
			// wait for the end of the execution
			
			List<Path> liste = clientData.listDirectory("vip://vip.creatis.insa-lyon.fr/vip/Home/searchFileTest");
			assertThat("the asked directory is empty",liste.size(), is(not(0)));
		}
	}
}
