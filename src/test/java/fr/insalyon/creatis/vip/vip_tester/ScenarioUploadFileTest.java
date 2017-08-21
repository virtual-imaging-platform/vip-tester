package fr.insalyon.creatis.vip.vip_tester;

import fr.insalyon.creatis.vip.client.data.model.UploadData;
import fr.insalyon.creatis.vip.client.processing.ApiException;
import fr.insalyon.creatis.vip.client.processing.api.DefaultApi;
import fr.insalyon.creatis.vip.client.processing.model.Execution;
import fr.insalyon.creatis.vip.client.processing.model.ParameterType;
import fr.insalyon.creatis.vip.client.processing.model.Execution.StatusEnum;
import fr.insalyon.creatis.vip.client.processing.model.Pipeline;
import fr.insalyon.creatis.vip.client.processing.model.PipelineParameter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.CharStreams;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import fr.insalyon.creatis.vip.client.processing.api.DefaultApi;

import javax.xml.bind.DatatypeConverter;

public class ScenarioUploadFileTest {

	private VipTesterHelper vth = new VipTesterHelper();
	private DefaultApi client = vth.getDefaultApi();
	private static Logger logger = LoggerFactory.getLogger(ScenarioKillAndRestartTest.class);
	private fr.insalyon.creatis.vip.client.data.api.DefaultApi clientData = vth.getdefaultApiData();
	private VipCheckerHelper vch = new VipCheckerHelper();
	
	@Test
	public void scenario7() throws Exception{
		String relatifPath = "/vip/Home/uploadTest";
		String uri = vth.getUriPrefix()+relatifPath;		
		String pipelineId = vth.getGrepTestPipelineIdString();
		
		vch.checkPipelineIsPresent(pipelineId);
		
		vch.checkPipelineParameters(pipelineId);

		// create a new path where the future result will be stocked
		vth.createDirectory(relatifPath);
		vch.checkDirectoryExist(relatifPath);
		
		try{
			
		UploadData encodedData = vth.prepareInputFile(relatifPath);
		clientData.uploadFile(encodedData);
			
			// check of pipeline's parameters
			Pipeline pip = client.getPipeline(vth.getGrepTestPipelineId());
			logger.debug("grep pipeline: {}", pip);
			
			Execution execut = vth.launchExecution(pipelineId, "testUpload", relatifPath,"Prune", relatifPath+"/listeFruit");
			vch.checkExecutionRunningState(execut);
			String executionId = execut.getIdentifier();
						
			vch.checkExecutionProcess(executionId);
			
			String result = vth.download(pipelineId, executionId, relatifPath);
			logger.info("result of the execution: {}", result);
		}finally{
			logger.debug("in finally bloc");
			if(vch.checkDirectoryExist(relatifPath)){
				clientData.deletePath(uri);
			}
		}
	}
}
