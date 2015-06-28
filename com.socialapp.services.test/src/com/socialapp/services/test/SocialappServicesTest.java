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
	
	@Before
	public void setup(){
		response = null;
	}
	
	@After
	public void teardown(){
		response = null;	
	}

	@Test
	public void testResponseIsNotNull() throws InterruptedException{
		
		
		String url = "http://www.google.com";
		AQuery aq = new AQuery();
        
        aq.ajax(url, new ProcessableCallback<String>(null) {

            public void callback(String url, String resp, AjaxStatus status) {
            	System.out
						.println("SocialappServicesTest.testResponseIsNotNull(): " + resp);
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
		
        Thread.sleep(1000);
        assertNotNull("the response must not be null!", response);
	}

}
