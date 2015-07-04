package com.socialapp.services.dao.chat;

import java.util.Date;

import com.socialapp.services.dao.IValueObject;
import com.socialapp.services.util.SocialappServiceConstants;

public class MessageFrame implements IValueObject{

	private Date date;
	private MessageBox boxType;

	public MessageFrame(Date date, MessageBox boxType) {
		this.date = date;
		this.boxType = boxType;
	}

	public MessageBox getBoxType() {
		return boxType;
	}

	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public String toString() {
		return "["
				+ SocialappServiceConstants.DATE_TIME_SECONDS_FORMAT_WEBSITE_FORMAT
						.format(date) + ", " + boxType.name() + "]";
	}

}
