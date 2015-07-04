package com.socialapp.services.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Test;

import com.socialapp.services.IResultProcessor;
import com.socialapp.services.internal.callback.AQuery;
import com.socialapp.services.internal.callback.AjaxStatus;
import com.socialapp.services.internal.callback.custom.ProcessableCallback;
import com.socialapp.services.internal.callback.custom.sharedstate.LoginState;
import com.socialapp.services.internal.callback.custom.sharedstate.LoginState.LoginResult;


public class SocialappServicesTest extends AbstractSocialappServicesTest{


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
	
	@Test
	public void testLoginWithWrongPassword() {
		
		LoginState.login(null, "eliasryt@gmail.com", "somewrongpassword", new File(""), new IResultProcessor<LoginResult>(){

			@Override
			public void process(LoginResult result, Object... params) {
            	SocialappServicesTest.this.response = result;				
			}
			
		});
		
        waitForCallback();
        assertNotNull("the response must not be null!", response);
        assertEquals(LoginResult.WRONG_PASS, response);
	}

}
