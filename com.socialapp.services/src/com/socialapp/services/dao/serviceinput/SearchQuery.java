package com.socialapp.services.dao.serviceinput;

public class SearchQuery {
	
	int location;
	int interest;
	String gender;
	
	public SearchQuery(int location, int interest, String gender) {
		super();
		this.location = location;
		this.interest = interest;
		this.gender = gender;
	}
	public SearchQuery() {
	}
	public int getLocation() {
		return location;
	}
	public void setLocation(int location) {
		this.location = location;
	}
	public int getInterest() {
		return interest;
	}
	public void setInterest(int interest) {
		this.interest = interest;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	

}
