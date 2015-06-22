package com.socialapp.services.internal.callback.custom;

import com.socialapp.services.IResultProcessor;
import com.socialapp.services.internal.util.ExternalUrlConstants;

public abstract class AbstractGetMemberidCallback extends
		ProcessableCallback<String> {

	private Integer[] interestsIdArray;
	private String zip;
	private String name;

	public AbstractGetMemberidCallback(IResultProcessor<String> proc, String name, Integer[] interestsIdArray, String zip) {
		super(proc);
		this.interestsIdArray = interestsIdArray;
		this.zip = zip;
		this.name = name;
	}

	protected String retriveMemberidURL(String name, Integer[] interestsIdArray, String zip) {
		String url = ExternalUrlConstants.GET_MEMBERID;
		for (int i = 0; i < interestsIdArray.length; i++) {
			if (interestsIdArray[i] != null) { // TODO ideally this is never the
												// case, but the app interests
												// store is not nsynch with the
												// origional store!
				url += "interest[" + i + "]=" + interestsIdArray[i] + "&";
			}
		}
		url += "zip=" + zip + "&";
		url += "name=" + name;
		return url;
	}

	@Override
	public String getUrl() {
		return retriveMemberidURL(name,interestsIdArray, zip);
	}

}
