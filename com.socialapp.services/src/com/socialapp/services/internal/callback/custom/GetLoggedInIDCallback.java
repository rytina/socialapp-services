package com.socialapp.services.internal.callback.custom;

import com.socialapp.services.IResultProcessor;
import com.socialapp.services.internal.callback.AjaxStatus;
import com.socialapp.services.internal.callback.custom.sharedstate.LoginState;
import com.socialapp.services.util.PartnerAppConstants;



public class GetLoggedInIDCallback extends AbstractGetMemberidCallback {
	
	public GetLoggedInIDCallback(IResultProcessor<String> proc, String name, Integer[] interestsIdArray, String zip){
		super(proc, name, interestsIdArray, zip);
		cookie(PartnerAppConstants.PHPSESSID, LoginState.phpsessid);
	}
	
	@Override
	public void callback(String url, String memberid, AjaxStatus status) {
		finalize(memberid, new Object[]{});
	}

	@Override
	public boolean shouldLog() {
		return false;
	}

	@Override
	public String getActivityTableName() {
		return null;
	}

}
