package com.socialapp.services.dao;

public class Interest implements IValueObject{
	private final int id;
	
	private final String interestName;

	public Interest(int id, String interestName){
		this.id = id;
		this.interestName = interestName;
	}
	
	public String getInterestName() {
		return interestName;
	}
	
	public int getId() {
		return id;
	}
	
	@Override
	public String toString() {
		return interestName;
	}
	
}
