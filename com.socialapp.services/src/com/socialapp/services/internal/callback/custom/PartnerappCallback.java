package com.socialapp.services.internal.callback.custom;

import com.socialapp.services.internal.callback.AjaxStatus;


public interface PartnerappCallback {
	
	public static final ProcessableCallback<String> NULL_CALLBACK = new ProcessableCallback<String>(null){
		public void callback(String url, String object, AjaxStatus status) {
		}

		@Override
		public boolean shouldLog() {
			return false;
		}

		@Override
		public String getActivityTableName() {
			return null;
		};
	};

}
