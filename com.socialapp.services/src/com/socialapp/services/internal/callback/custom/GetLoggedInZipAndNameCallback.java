package com.socialapp.services.internal.callback.custom;

import com.socialapp.services.IResultProcessor;
import com.socialapp.services.internal.callback.AjaxStatus;
import com.socialapp.services.internal.callback.custom.sharedstate.LoginState;
import com.socialapp.services.internal.util.UrlConstants;
import com.socialapp.services.util.PartnerAppConstants;
import com.socialapp.services.util.Tuple;


public class GetLoggedInZipAndNameCallback extends ProcessableCallback<Tuple<String, String>>{

	private static final String ZIP = "name=\"zip\" value=\"";
	private static final String FORENAME = "name=\"forename\" value=\"";

	public GetLoggedInZipAndNameCallback(IResultProcessor<Tuple<String, String>> proc) {
		super(proc);
		cookie(PartnerAppConstants.PHPSESSID, LoginState.phpsessid);
	}
	
	@Override
	public void callback(String request, String response, AjaxStatus status) {
		String zip = null;
		int index = response.indexOf(ZIP) + ZIP.length();
		zip = response.substring(index, index+5);
		int end = zip.indexOf('"');
		if(end > 0){
			zip = zip.substring(0, end);
		}
		String name = null;
		index = response.indexOf(FORENAME) + FORENAME.length();
		name = response.substring(index, index+50);
		end = name.indexOf('"');
		if(end > 0){
			name = name.substring(0, end);
		}
		finalize(new Tuple<String, String>(zip, name), new Object[]{});
	}

	@Override
	public boolean shouldLog() {
		return false;
	}
	
	@Override
	public String getUrl() {
		return UrlConstants.APP_DOMAIN + UrlConstants.PROFILE;
	}

	@Override
	public String getActivityTableName() {
		return null;
	}

}
