package com.socialapp.services.util;

import java.util.HashMap;
import java.util.Map;

import com.socialapp.services.internal.callback.AQuery;
import com.socialapp.services.internal.callback.custom.PartnerappCallback;
import com.socialapp.services.internal.util.ExternalUrlConstants;


public class ServerUtils {

	private ServerUtils() {
	}

	public static void updateUserDataOnServer(String email, Integer memberID, Integer imageid, String gcmRegistrationID) {
		try {
			AQuery aq = new AQuery();
			Map<String, String> params = new HashMap<String, String>();
			if (email != null && !email.equals("")) {
				params.put("email", email.toString());
			}
			if (memberID != null && memberID > 0) {
				params.put("memberid", memberID.toString());
			}
			if (imageid != null && imageid > 0) {
				params.put("imageid", imageid.toString());
			}
			if (gcmRegistrationID != null && !gcmRegistrationID.equals("")) {
				params.put("gcmregid", gcmRegistrationID);
			}
			aq.ajax(ExternalUrlConstants.CREATE_OR_UPDATE_USER_URL, params,	PartnerappCallback.NULL_CALLBACK);
		} catch (RuntimeException e) {
			System.err.println(SocialappFeature.WEB.toString() + "update user data on server failed!");
			e.printStackTrace();
		}
	}

}
