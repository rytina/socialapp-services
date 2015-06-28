package com.socialapp.services.test;

import org.junit.Test;

import com.socialapp.services.internal.callback.AQuery;
import com.socialapp.services.internal.callback.AjaxStatus;
import com.socialapp.services.internal.callback.custom.ProcessableCallback;


public class SocialappServicesTest {

	@Test
	public void testAjaxCookieGet() throws InterruptedException{
		
		
		String url = "http://www.google.com";
		AQuery aq = new AQuery();
        
        aq.ajax(url, new ProcessableCallback<String>(null) {

            public void callback(String url, String json, AjaxStatus status) {
                
            	System.out
						.println("SocialappServicesTest.testAjaxCookieGet().new ProcessableCallback() {...}.callback()");
            }

			@Override
			public boolean shouldLog() {
				return false;
			}

			@Override
			public String getLogTableName() {
				return null;
			}
        });
		
        while(true){
        	Thread.sleep(1000);
        }
	}

}
