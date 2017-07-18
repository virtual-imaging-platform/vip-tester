package fr.insalyon.creatis.vip.vip_tester;

import fr.insalyon.creatis.vip.client.data.ApiException;
import fr.insalyon.creatis.vip.client.processing.api.DefaultApi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class VipCheckerHelper {
	private VipTesterHelper vth = new VipTesterHelper();
	private DefaultApi client = vth.getDefaultApi();
	private fr.insalyon.creatis.vip.client.data.api.DefaultApi clientData = vth.getdefaultApiData();

	
	public boolean verifyDirectoryExist(String uri) throws ApiException{
		String finalUri = vth.getUriPrefix()+uri;
		boolean isExist = clientData.doesPathExists(finalUri);
		assertThat("directory: "+uri+" not exist",isExist, is(true));
		return isExist;
		
	}
}
