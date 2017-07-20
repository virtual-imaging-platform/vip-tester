package fr.insalyon.creatis.vip.vip_tester;

import fr.insalyon.creatis.vip.client.processing.api.DefaultApi;
import fr.insalyon.creatis.vip.client.processing.model.Execution;
import fr.insalyon.creatis.vip.client.processing.model.Execution.StatusEnum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ScenarioKillAndRestartTest {
	
	private VipTesterHelper vth = new VipTesterHelper();
	private VipCheckerHelper vch = new VipCheckerHelper();
	private DefaultApi client = vth.getDefaultApi();
	private static Logger logger = LoggerFactory.getLogger(ScenarioKillAndRestartTest.class);
	
	
	//tries kill a bugged execution end restart it
		@Test
		public void scenario3() throws Exception{
			String pipelineId = vth.getAdditionTestPipelineIdString();
			String relatifPath = "/vip/Home";
			//create and start the execution
			Execution execut1 = vth.launchExecution(pipelineId, "bugExecution", relatifPath, 3, 53);
			vch.checkExecutionRunningState(execut1);
			String executionId1 = execut1.getIdentifier();									
			
			//execution history ???
			List<Execution> list = client.listExecutions();
			
			//kill the bugged execution and check its status
			client.killExecution(executionId1);
			logger.debug("status must be killed, it is: {}", execut1.getStatus());
			vch.checkExecutionKilledState(execut1, executionId1);					
			
			//create and restart the execution check its status
			Execution execut2 = vth.launchExecution(pipelineId, "restartedExecution", relatifPath, 4, 54);
			vch.checkExecutionRunningState(execut2);
			String executionId2 = execut2.getIdentifier();
			vch.checkExecutionRunningState(execut2);

			// kill restart execution
			client.killExecution(executionId2);
			vch.checkExecutionKilledState(execut2, executionId2);
	}
}
