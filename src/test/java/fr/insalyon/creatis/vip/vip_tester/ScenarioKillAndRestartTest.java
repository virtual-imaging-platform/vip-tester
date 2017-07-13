package fr.insalyon.creatis.vip.vip_tester;

import fr.insalyon.creatis.vip.client.processing.api.DefaultApi;
import fr.insalyon.creatis.vip.client.processing.model.Execution;
import fr.insalyon.creatis.vip.client.processing.model.Execution.StatusEnum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ScenarioKillAndRestartTest {
	
	private VipTesterHelper vth = new VipTesterHelper();
	private DefaultApi client = vth.getDefaultApi();
	private static Logger logger = LoggerFactory.getLogger(ScenarioKillAndRestartTest.class);
	
	
	//tries kill a bugged execution end restart it
		@Test
		public void scenario3() throws Exception{		
			//create and start the execution
			Execution body = vth.initExecution("vip/Home", "newScenarioKo", 1, 2);
			Execution result = client.initAndStartExecution(body);
			String resId = result.getIdentifier();				
			assertThat("the execution is not launched", result.getStatus(), is(StatusEnum.RUNNING));
			
			//execution history
			client.listExecutions();

			//kill the bugged execution and check its status
			client.killExecution(resId);
			result = client.getExecution(resId);
			assertThat("the bugged execution is not killed", result.getStatus(), is(StatusEnum.KILLED));
					
			//create and restart the execution check its status
			body = vth.initExecution("vip/Home", "newScenario3", 1, 2);
			result = client.initAndStartExecution(body);
			assertThat("the execution has not been launched", result.getStatus(), is(StatusEnum.RUNNING));
			resId = result.getIdentifier();
			client.killExecution(resId);
			
			assertThat("The new execution is not launch", result.getStatus(), is(StatusEnum.RUNNING));
	}
}
