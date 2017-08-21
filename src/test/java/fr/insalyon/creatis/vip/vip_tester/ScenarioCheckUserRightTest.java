package fr.insalyon.creatis.vip.vip_tester;

import fr.insalyon.creatis.vip.vip_tester.VipTesterHelper;

import fr.insalyon.creatis.vip.client.processing.ApiException;
import fr.insalyon.creatis.vip.client.processing.api.DefaultApi;
import fr.insalyon.creatis.vip.client.processing.model.Execution;
import fr.insalyon.creatis.vip.client.processing.model.Execution.StatusEnum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

public class ScenarioCheckUserRightTest {
	private VipTesterHelper vth = new VipTesterHelper();
	private VipCheckerHelper vch = new VipCheckerHelper();
	private DefaultApi client = vth.getDefaultApi();
	private static Logger logger = LoggerFactory.getLogger(ScenarioCheckUserRightTest.class);
	
	@Test
	public void scenario6() throws Exception{
		String pipelineId = vth.getAdditionTestPipelineIdString();
		String relatifPath = "/vip/Home";
		//create and start an execution
		Execution execut1 = vth.launchExecution(pipelineId, "updateExe1", relatifPath, 1, 51);
		vch.checkExecutionRunningState(execut1);
		String executionId1 = execut1.getIdentifier();
				
		//create and start another execution
		String executionId2 = null;
		Execution execut2 = null;
		try{
			//create and start an execution
			execut2 = vth.launchExecution(pipelineId, "updateExe2", relatifPath, 2, 52);
			vch.checkExecutionRunningState(execut2);
			executionId2 = execut2.getIdentifier();
		}catch(ApiException ae){
			client.killExecution(executionId1);
			return;
		}
		client.killExecution(executionId1);
		vch.checkExecutionKilledState(execut2, executionId2);
		client.killExecution(executionId2);
		throw new RuntimeException(" with that type of right you can't launch 2 execution in the same time");
	}
}
