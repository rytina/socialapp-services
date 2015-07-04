package com.socialapp.services.internal.callback.custom;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.socialapp.services.IResultProcessor;
import com.socialapp.services.internal.callback.AjaxStatus;
import com.socialapp.services.internal.callback.custom.sharedstate.LoginState;
import com.socialapp.services.internal.util.UrlConstants;
import com.socialapp.services.util.SocialappServiceConstants;


public class GetLoggedInMemberInterestsCallback extends ProcessableCallback<Integer[]>{

	public GetLoggedInMemberInterestsCallback(IResultProcessor<Integer[]> proc) {
		super(proc);
		cookie(SocialappServiceConstants.PHPSESSID, LoginState.phpsessid);
	}
	
	@Override
	public void callback(String request, String response, AjaxStatus status) {
		Document document = Jsoup.parse(response);
		Element interestsElement = document.getElementById("interests");
		Elements elements = interestsElement.getElementsByAttributeValue("checked","checked");
		Integer[] interests = new Integer[elements.size()];
		for (int i=0; i<elements.size(); i++) {
			interests[i] = Integer.parseInt(elements.get(i).attr("value"));
		}
		finalize(interests, new Object[]{});
	}

	@Override
	public boolean shouldLog() {
		return false;
	}
	
	@Override
	public String getUrl() {
		return UrlConstants.APP_DOMAIN + UrlConstants.INTERESTS;
	}

	@Override
	public String getLogTableName() {
		return null;
	}

}
