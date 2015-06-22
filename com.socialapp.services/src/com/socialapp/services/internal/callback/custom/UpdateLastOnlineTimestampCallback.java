package com.socialapp.services.internal.callback.custom;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.socialapp.services.IResultProcessor;
import com.socialapp.services.dao.Member;
import com.socialapp.services.internal.callback.AjaxStatus;
import com.socialapp.services.internal.util.ExternalUrlConstants;
import com.socialapp.services.util.PartnerAppConstants;
import com.socialapp.services.util.PartnerAppFeature;


public class UpdateLastOnlineTimestampCallback extends ProcessableCallback<Boolean> {

	private static final String WRONG_FORMAT_ERRORMSG = "the positionToTimestampEntry has the wrong format";
	private static final String MEMBER_POST_PARAM = "members[%s]";
	private List<Member> members;

	public UpdateLastOnlineTimestampCallback(List<Member> members, IResultProcessor<Boolean> processor){
		super(processor);
		this.members = members;
	}
	
	@Override
	public void callback(String url, String memberPositionToTimestampJson, AjaxStatus status) {
		String[] lines = memberPositionToTimestampJson.trim().split("\n");
		boolean changed = false;
		for (String positionToTimestampEntry : lines) {
			try{
				String[] splitted = positionToTimestampEntry.split("=");
				int memberIndex = Integer.parseInt(splitted[0]);
				String dateTimeString = splitted[1].trim();
				if(!dateTimeString.equals("0")){
					Date lastOnlineStamp = PartnerAppConstants.DATE_TIME_FORMAT_MYSQL_FORMAT.parse(dateTimeString);
					changed |= members.get(memberIndex).setLastInteractionStamp(lastOnlineStamp);
				}
			}catch(RuntimeException ex){
				System.err.println(PartnerAppFeature.WEB.name() + " " + WRONG_FORMAT_ERRORMSG);
				ex.printStackTrace();
			} catch (ParseException ex) {
				System.err.println(PartnerAppFeature.WEB.name() + " " + WRONG_FORMAT_ERRORMSG);
				ex.printStackTrace();
			}
		}
		finalize(changed, new Object[]{});
	}
	
	
	@Override
	public String getUrl() {
		String onlineTimestamp = ExternalUrlConstants.GET_ONLINE_TIMESTAMPS;
		return onlineTimestamp;
	}
	
	
	public Map<String, Object> getParams(){
		Map<String, Object> paramMap = new HashMap<String, Object>();
		for (int i=0; i < members.size(); i++) {
			paramMap.put(String.format(MEMBER_POST_PARAM, i), members.get(i).getId());
		}
		return paramMap;
	}

	@Override
	public boolean shouldLog() {
		return false;
	}

	@Override
	public String getActivityTableName() {
		return null;
	}
	
}
