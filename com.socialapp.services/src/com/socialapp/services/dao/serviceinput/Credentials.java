package com.socialapp.services.dao.serviceinput;

import com.socialapp.services.dao.IValueObject;


public class Credentials implements IValueObject{
	
	private String user;
	private String pass;

	public Credentials(String user, String pass) {
		this.user = user;
		this.pass = pass;
	}
	
	public String getUser() {
		return user;
	}
	public String getPass() {
		return pass;
	}
	

}
