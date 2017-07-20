package fr.insalyon.creatis.vip.vip_tester;

import fr.insalyon.creatis.vip.client.processing.ApiException;
import fr.insalyon.creatis.vip.client.processing.api.DefaultApi;
import fr.insalyon.creatis.vip.client.processing.model.Execution;
import fr.insalyon.creatis.vip.client.processing.model.Execution.StatusEnum;
import fr.insalyon.creatis.vip.client.processing.model.ParameterType;
import fr.insalyon.creatis.vip.client.processing.model.Pipeline;
import fr.insalyon.creatis.vip.client.processing.model.PipelineParameter;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import org.junit.Test;

public class ScenarioExecutionProcessTest {
	
	private static final String VIP_HOME_ADDITION_TEST = "/vip/Home/additionTest";
	private VipTesterHelper vth = new VipTesterHelper();
	private DefaultApi client = vth.getDefaultApi();
	private fr.insalyon.creatis.vip.client.data.api.DefaultApi clientData = vth.getdefaultApiData();
	private static Logger logger = LoggerFactory.getLogger(ScenarioExecutionProcessTest.class);
	private VipCheckerHelper vch = new VipCheckerHelper();
	private String uriPrefix = vth.getUriPrefix();
	
	//tries to launch an execution and waits the end of it
	@Test
	public void launchAdditionExecutionTest() throws Exception{
		
		String pipelineId = vth.getAdditionTestPipelineIdString();
		String relatifPath = "/vip/Home/additionTest";
		// Search if the pipeline exist
		vch.checkPipelineIsPresent(pipelineId);
		
		//check parameters for a specified pipeline
		vch.checkPipelineParameters(pipelineId);
		
		// create a new path where the future result will be stocked
		vth.createDirectory(VIP_HOME_ADDITION_TEST);
		vch.checkDirectoryExist(VIP_HOME_ADDITION_TEST);

				
		try{
			//create and start an execution and check its status
//			String executionId = launchExecution();
			logger.debug("pipe: {}", pipelineId);
			Execution execut = vth.launchExecution(pipelineId, "testExecutionProcess", relatifPath, 2, 3);
			vch.checkExecutionRunningState(execut);
			String executionId = execut.getIdentifier();
			//check the execution status every 20s + timeout=10mn
			vch.checkExecutionProcess(executionId);
		
			// download the content of the result file and delete newPath
			String executionResult = vth.download(executionId, relatifPath);	
			logger.info("result: {}", executionResult);		
		} finally{
			logger.debug("in finally bloc");
			if(vch.checkDirectoryExist(VIP_HOME_ADDITION_TEST)){
				clientData.deletePath(uriPrefix+VIP_HOME_ADDITION_TEST);
			}
		}
	}
			
	// launch an execution and check its status
//	public String launchExecution() throws Exception{
//		Execution result = client.initAndStartExecution(vth.initAdditionExecution(VIP_HOME_ADDITION_TEST,"testScenario1", 0, 1));
//		assertThat("The status must be \"running\"", result.getStatus(), is(StatusEnum.RUNNING));
//		return result.getIdentifier();
//	}		
	
}
