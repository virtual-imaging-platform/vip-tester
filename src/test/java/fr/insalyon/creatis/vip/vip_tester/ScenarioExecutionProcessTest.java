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

import java.util.Base64;
import javax.xml.bind.DatatypeConverter;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import org.junit.Test;

public class ScenarioExecutionProcessTest {
	
	private VipTesterHelper vth = new VipTesterHelper();
	private DefaultApi client = vth.getDefaultApi();
	private fr.insalyon.creatis.vip.client.data.api.DefaultApi clientData = vth.getdefaultApiData();
	private static Logger logger = LoggerFactory.getLogger(ScenarioExecutionProcessTest.class);
	
		//tries to launch an execution an waits the end of it
		@Test
		public void scenario1Test() throws Exception{
			// Search if the pipeline exist
			searchPipelineId();
			
			//check parameters for a specified pipeline
			checkPipelineParameter();
			
			// create a new path where the future result will be stocked
			newPath();
			
			String executionId = null;
			String executionResult = null;
			try{
				//create and start an execution and check its status
				executionId = launchExecution();
			
				//check the execution status every 20s + timeout=10mn
				checkExecutionProcess(executionId);
			
				// download the content of the result file and delete newPath
				executionResult = download(executionId);			
			}catch(Exception e){
				
			}
			finally{
				logger.debug("in finally bloc");
				if(clientData.doesPathExists("vip://vip.creatis.insa-lyon.fr/vip/Home/newPath/")){
					clientData.deletePath("vip://vip.creatis.insa-lyon.fr/vip/Home/newPath/");
				}
			}
			logger.debug("result: {}", executionResult);
		}
		
		public void searchPipelineId() throws Exception{
			String pipelineId = null;
			Iterator<Pipeline> listPipelineResultIt = client.listPipelines(null).iterator();

			boolean isFound = false;
			while(!isFound && listPipelineResultIt.hasNext()){
				Pipeline p = listPipelineResultIt.next();
				pipelineId = p.getIdentifier();
				if(pipelineId.equals("AdditionTest/0.9")){
					isFound = true;
				}			
			}
			assertThat("AdditionTest/0.9 is not present", isFound, is(true));
		}
		
		//check pipeline parameters
		public void checkPipelineParameter() throws ApiException{
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
		
		// create a new path where the future result will be stocked 
		public void newPath() throws Exception{
			clientData.createPath("vip://vip.creatis.insa-lyon.fr/vip/Home/newPath/");
			assertThat("newPath was not created",clientData.doesPathExists("vip://vip.creatis.insa-lyon.fr/vip/Home/newPath/"), is(true));
		}
		
		// launch an execution and check its status
		public String launchExecution() throws Exception{
			Execution result = client.initAndStartExecution(vth.initExecution("/vip/Home/newPath","testScenario1", 0, 1));
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
			String contentUri = "vip://vip.creatis.insa-lyon.fr/vip/Home/newPath/"+split[6]+"/"+split[7];
			String ExecutionResult = clientData.downloadFile(contentUri);
			Thread.sleep(2000);
			clientData.deletePath("vip://vip.creatis.insa-lyon.fr/vip/Home/newPath/");
			logger.debug("is exit before wait: {}",clientData.doesPathExists("vip://vip.creatis.insa-lyon.fr/vip/Home/newPath"));
			Thread.sleep(3000);
			logger.debug("is exit after wait of 5s: {}",clientData.doesPathExists("vip://vip.creatis.insa-lyon.fr/vip/Home/newPath"));
			assertThat("newPath directory is not deleted", clientData.doesPathExists("vip://vip.creatis.insa-lyon.fr/vip/Home/newPath/"), is(false));
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
