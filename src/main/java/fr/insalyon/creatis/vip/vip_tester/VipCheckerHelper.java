package fr.insalyon.creatis.vip.vip_tester;

import fr.insalyon.creatis.vip.client.data.ApiException;
import fr.insalyon.creatis.vip.client.processing.api.DefaultApi;
import fr.insalyon.creatis.vip.client.processing.model.ParameterType;
import fr.insalyon.creatis.vip.client.processing.model.Pipeline;
import fr.insalyon.creatis.vip.client.processing.model.PipelineParameter;
import fr.insalyon.creatis.vip.client.processing.model.Execution;
import fr.insalyon.creatis.vip.client.processing.model.Execution.StatusEnum;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VipCheckerHelper {
	private VipTesterHelper vth = new VipTesterHelper();
	private DefaultApi client = vth.getDefaultApi();
	private fr.insalyon.creatis.vip.client.data.api.DefaultApi clientData = vth.getdefaultApiData();
	private static Logger logger = LoggerFactory.getLogger(ScenarioExecutionProcessTest.class);

	
	public boolean checkDirectoryExist(String uri) throws ApiException{
		String finalUri = vth.getUriPrefix()+uri;
		boolean isExist = clientData.doesPathExists(finalUri);
		assertThat("directory: "+uri+" not exist",isExist, is(true));
		return isExist;
		
	}
	
	public void checkExecutionRunningState(Execution exe){
		assertThat("The status must be \"running\"", exe.getStatus(), is(StatusEnum.RUNNING));
	}
	
	public void checkExecutionKilledState(Execution exe, String exeId) throws Exception{
		Execution result = client.getExecution(exeId);
		assertThat("The status must be \"killed\"", result.getStatus(), is(StatusEnum.KILLED));
	}
	
	public void checkPipelineIsPresent(String pipelineId) throws Exception{
		List<Pipeline> pipelinesList = client.listPipelines(null);
		
		for(Pipeline pipeline : pipelinesList){
			if(pipeline.getIdentifier().equals(pipelineId)){
				return;
			}				
		}
		throw new RuntimeException(pipelineId+" does not exist");
	}
	
	//check pipeline parameters
	public void checkPipelineParameters(String pipelineId) throws Exception{
		switch(pipelineId){
			case "AdditionTest/0.9":
				checkAdditionPipelineParameters();
				break;
			case "GrepTest/1.1":
				checkGrepPipelineParameters();
				break;
			default:				
		}
	}
	
	//check AdditonTest pipeline parameters
	private void checkAdditionPipelineParameters() throws Exception{
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
	
	//check GrepTest pipeline parameters
	private void checkGrepPipelineParameters() throws Exception{
		Pipeline pipelineResult = client.getPipeline(vth.getGrepTestPipelineId());
		List<PipelineParameter> pipelineParam = pipelineResult.getParameters();
		assertThat("It must have 4 parameters", pipelineParam.size(), is(4));
		
		int cmptFile = 0, cmptString = 0;
		for(PipelineParameter pp : pipelineResult.getParameters()){
			if(pp.getType().equals(ParameterType.FILE)){
				if(pp.getName().equals("results-directory") || pp.getName().equals("file")){
					cmptFile++;
				}
			}
			if(pp.getType().equals(ParameterType.STRING)){
				if(pp.getName().equals("text") || pp.getName().equals("output")){
					cmptString++;
				}
			}				
		}
		boolean conditionSuccess = (cmptFile==2) && (cmptString==2); 
		assertThat("It must have 2 parameters of type string and 2 others of file type", conditionSuccess, is(true)) ;		
		return;
	}
	
	//check the execution status every 20s + timeout=10mn
	public void checkExecutionProcess(final String executionId) throws Exception{
		// TEST ExecutorService 1
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

		CallableTimeout callTimeout = new CallableTimeout();		
		Callable<Boolean> callable = new Callable<Boolean>(){
			public Boolean call() throws Exception{
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
