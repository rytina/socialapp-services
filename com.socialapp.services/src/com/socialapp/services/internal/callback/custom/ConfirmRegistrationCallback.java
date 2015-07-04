package com.socialapp.services.internal.callback.custom;


import com.socialapp.services.IResultProcessor;
import com.socialapp.services.internal.callback.AjaxStatus;

public class ConfirmRegistrationCallback extends ProcessableCallback<Boolean> {

	private String email;
	private String pass;
	private IResultProcessor<Boolean> resultProcessor;

	public ConfirmRegistrationCallback(String email, String pass, IResultProcessor<Boolean> resultProcessor) {
		super(resultProcessor);
		this.email = email;
		this.pass = pass;
		this.resultProcessor = resultProcessor;
	}

	@Override
	public void callback(String url, String object, AjaxStatus status) {
		if(object != null){
			finalize(true, new Object[]{});
		}else{
			finalize(false, status);
		}
	}

	@Override
	public boolean shouldLog() {
		return false;
	}

	@Override
	public String getLogTableName() {
		return null;
	}
}