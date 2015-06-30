package com.socialapp.services.test;

import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.socialapp.services.internal.callback.AQuery;
import com.socialapp.services.internal.callback.AjaxStatus;
import com.socialapp.services.internal.callback.custom.ProcessableCallback;


public class SocialappServicesTest {

	protected String response;
	protected AjaxStatus status;
	
	@Before
	public void setup(){
		response = null;
	}
	
	@After
	public void teardown(){
		response = null;	
	}

	@Test
	public void testResponseIsNotNull() {
		
		
		String url = "http://www.google.com";
		AQuery aq = new AQuery();
        
        aq.ajax(url, new ProcessableCallback<String>(null) {

            public void callback(String url, String resp, AjaxStatus status) {
            	SocialappServicesTest.this.status = status;
            	SocialappServicesTest.this.response = resp;
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
		
        waitForCallback();
        assertNotNull("the response must not be null!", response);
	}

	private void waitForCallback() {
		for (int i = 0; i < 10; i++) { // wait at most 10 seconds
			if(this.status != null){
				return;
			}
			try{
				Thread.sleep(1000);
			}catch(InterruptedException ex){
				// ignore
			}
		}
	}

}
