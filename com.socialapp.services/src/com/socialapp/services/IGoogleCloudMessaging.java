package com.socialapp.services;

import com.socialapp.services.internal.callback.custom.sharedstate.LoginState;

public interface IGoogleCloudMessaging {

	void create(LoginState cb);

	String register(String gsmProjectNumber);

}
