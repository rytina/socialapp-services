package com.socialapp.services.internal.callback.custom;

import com.socialapp.services.IResultProcessor;
import com.socialapp.services.internal.callback.AjaxStatus;


public class LogMessageCallback extends ProcessableCallback<String>{

	public LogMessageCallback(IResultProcessor<String> proc) {
		super(proc);
	}

	@Override
	public void callback(String url, String object, AjaxStatus status) {
		super.callback(url, object, status);
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
