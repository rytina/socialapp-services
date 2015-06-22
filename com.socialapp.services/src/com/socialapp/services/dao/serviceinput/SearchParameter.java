package com.socialapp.services.dao.serviceinput;

public class SearchParameter {
	
	private final int location;
	private final String gender;
	private final int interest;
	
	public SearchParameter(int location, String gender, int interest) {
		super();
		this.location = location;
		this.gender = gender;
		this.interest = interest;
	}

	public int getLocation() {
		return location;
	}

	public String getGender() {
		return gender;
	}

	public int getInterest() {
		return interest;
	}

}
