package com.socialapp.services.internal.callback.custom;

import com.socialapp.services.IResultProcessor;
import com.socialapp.services.internal.callback.AjaxStatus;
import com.socialapp.services.util.SocialappServiceConstants;


public class SendMessageCallback extends ProcessableCallback<String>{


	public enum Phase{FIRST,SECOND}

	private Phase phase;
	
	public SendMessageCallback(Phase phase, IResultProcessor<String> proc) {
		super(proc);
		this.phase = phase;
	}
	
	@Override
	public void callback(String url, String object, AjaxStatus status) {
		super.callback(url, object, status);
		switch (phase) {
		case FIRST:
			finalize(object);
			break;
		case SECOND:
			finalize(SocialappServiceConstants.STATUS_OK);
			break;
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
