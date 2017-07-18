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
		// Search if the pipeline exist
		checkAdditionPipelineIsPresent();
		
		//check parameters for a specified pipeline
		checkAdditionPipelineParameters();
		
		// create a new path where the future result will be stocked
		vth.createDirectory(VIP_HOME_ADDITION_TEST);
		vch.verifyDirectoryExist(VIP_HOME_ADDITION_TEST);

				
		try{
			//create and start an execution and check its status
			String executionId = launchExecution();
		
			//check the execution status every 20s + timeout=10mn
			checkExecutionProcess(executionId);
		
			// download the content of the result file and delete newPath
			String executionResult = download(executionId);	
			logger.info("result: {}", executionResult);		
		} finally{
			logger.debug("in finally bloc");
			if(vch.verifyDirectoryExist(VIP_HOME_ADDITION_TEST)){
				clientData.deletePath(uriPrefix+VIP_HOME_ADDITION_TEST);
			}
		}
	}
	
	public void checkAdditionPipelineIsPresent() throws Exception{
		List<Pipeline> pipelinesList = client.listPipelines(null);
		
		for(Pipeline pipeline : pipelinesList){
			if(pipeline.getIdentifier().equals(vth.getAdditionTestPipelineIdString())){
				return;
			}				
		}
		throw new RuntimeException("AdditonTest does not exist");
	}
	
	//check pipeline parameters
	public void checkAdditionPipelineParameters() throws ApiException{
		Pipeline pipelineResult = client.getPipeline(vth.getAdditionTestPipelineId());
		List<PipelineParameter> pipelineParam = pipelineResult.getParameters();
		assertThat("It must have 3 parameters", pipelineParam.size(), is(3));
		
		int cmptInt = 0;
		for(PipelineParameter pp : pipelineResult.getParameters()){
			if(!(pp.getName().equals("results-directory"))){
				if(pp.getType().equals(ParameterType.STRING)){
					cmptInt++;
				}
			}
		}
		assertThat("It must have 2 parameters of type string for the addition", cmptInt, is(2)) ;		
		return;
	}
		
	// launch an execution and check its status
	public String launchExecution() throws Exception{
		Execution result = client.initAndStartExecution(vth.initAdditionExecution(VIP_HOME_ADDITION_TEST,"testScenario1", 0, 1));
		assertThat("The status must be \"running\"", result.getStatus(), is(StatusEnum.RUNNING));
		return result.getIdentifier();
	}
	
	//check the execution status every 20s + timeout=10mn
	public void checkExecutionProcess(final String executionId) throws Exception{
		// TEST ExecutorService 1
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

		CallableTimeout callTimeout = new CallableTimeout();		
		Callable<Boolean> callable = new Callable<Boolean>(){
			public Boolean call() throws ApiException{
				StatusEnum result = client.getExecution(executionId).getStatus();
				return result.equals(StatusEnum.FINISHED);
			}
		};
		
		ScheduledFuture<Boolean> timeout = executor.schedule(callTimeout, 10,TimeUnit.MINUTES);
		boolean isFinished = false;
		while(!isFinished){
			 ScheduledFuture<Boolean>expected = executor.schedule(callable, 20, TimeUnit.SECONDS);
			 callTimeout.setExpected(expected);
			 logger.debug("Waiting to check if execution is over");
			 isFinished = expected.get();
		}
		timeout.cancel(true);
		executor.shutdownNow();	

		assertThat("Output file is missing", client.getExecution(executionId).getReturnedFiles().size(), is(not(0)));
		return;
	}
	
	// download the content of the result file and delete newPath
	public String download(String executionId) throws Exception{
		String returnedFile = client.getExecution(executionId).getReturnedFiles().get("output_file").get(0);
		String[] split = returnedFile.split("/");	
		String resultDirectory = uriPrefix+VIP_HOME_ADDITION_TEST+"/";
		String uri = resultDirectory+split[6]+"/"+split[7];
		String ExecutionResult = clientData.downloadFile(uri);
		return ExecutionResult;
	}
	
	public class CallableTimeout implements Callable<Boolean>{
		private ScheduledFuture<Boolean> expected = null;
		
		public Boolean call() throws InterruptedException, ExecutionException{
			return expected.cancel(true);
		}
		
		public void setExpected(ScheduledFuture<Boolean> expected) {
			this.expected = expected;
		}
	}
}
