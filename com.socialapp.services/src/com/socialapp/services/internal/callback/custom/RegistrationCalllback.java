package com.socialapp.services.internal.callback.custom;

import static com.socialapp.services.internal.util.UrlConstants.CONFIRM_REGISTRATION_URL;
import static com.socialapp.services.util.PartnerappServiceUtils.md5;

import com.socialapp.services.IResultProcessor;
import com.socialapp.services.internal.callback.AQuery;
import com.socialapp.services.internal.callback.AjaxStatus;
import com.socialapp.services.util.PartnerAppConstants;

public class RegistrationCalllback extends ProcessableCallback<Boolean> {

	private String pass;
	private String email;
	private IResultProcessor<Boolean> resultProcessor;

	public RegistrationCalllback(String email, String pass, IResultProcessor<Boolean> resultProcessor) {
		super(null);	// the processor is forwarded to the confirmation call back. Here it is null
		this.resultProcessor = resultProcessor;
	}

	@Override
	public void callback(String url, String html,
			AjaxStatus status) {
		super.callback(url, html, status);
		if (html.contains(PartnerAppConstants.REGISTRATION_SUCCESS_TEXT)) {
			
			ConfirmRegistrationCallback confirmRegistrationCb = new ConfirmRegistrationCallback(email, pass, resultProcessor);
			AQuery aq = new AQuery();
			String confirmationURL = CONFIRM_REGISTRATION_URL+email+"&key="+ md5(pass);
			aq.ajax(confirmationURL, confirmRegistrationCb);
		} else {
			String alertText = html;
			try {
				int index = html
						.indexOf("alert(unescape(\"");
				alertText = html.substring(index);
				alertText = alertText.substring(16,
						alertText.indexOf("\")"));
			} catch (Throwable e) {
				// ignore
			}
			finalize(false,alertText);
		}

	}

	@Override
	public boolean shouldLog() {
		return true;
	}
	
	@Override
	public String getLogTableName() {
		return "registrations";
	}
}