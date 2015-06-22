package com.socialapp.services.dao;

public class City implements IValueObject{
	
	private int zip;
	private String cityName;

	public City(int zip, String cityName){
		this.zip = zip;
		this.cityName = cityName;
	}
	
	public City() {
	}

	public int getZip() {
		return zip;
	}
	public void setZip(int zip) {
		this.zip = zip;
	}
	public String getCityName() {
		return cityName;
	}
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	
	

}
