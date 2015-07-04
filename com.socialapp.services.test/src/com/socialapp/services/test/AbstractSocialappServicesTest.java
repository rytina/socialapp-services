package com.socialapp.services.test;

import org.junit.After;
import org.junit.Before;

import com.socialapp.services.internal.callback.AjaxStatus;

public class AbstractSocialappServicesTest {
	
	protected Object response;
	protected AjaxStatus status;
	
	@Before
	public void setup(){
		response = null;
	}
	
	@After
	public void teardown(){
		response = null;	
	}
	
	
	protected void waitForCallback() {
		for (int i = 0; i < 10; i++) { // wait at most 10 seconds
			if(this.status != null || this.response != null){
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
