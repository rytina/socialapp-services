package com.socialapp.services.internal.callback.custom.sharedstate;

import static com.socialapp.services.internal.util.UrlConstants.APP_DOMAIN;
import static com.socialapp.services.internal.util.UrlConstants.LOGIN;
import static com.socialapp.services.util.PartnerAppConstants.PHPSESSID;

import java.io.File;
import java.io.FileWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.cookie.Cookie;

import com.socialapp.services.IGoogleCloudMessaging;
import com.socialapp.services.IResultProcessor;
import com.socialapp.services.dao.serviceinput.Credentials;
import com.socialapp.services.internal.callback.AQuery;
import com.socialapp.services.internal.callback.AjaxStatus;
import com.socialapp.services.internal.callback.custom.GetLoggedInIDCallback;
import com.socialapp.services.internal.callback.custom.GetLoggedInMemberInterestsCallback;
import com.socialapp.services.internal.callback.custom.GetLoggedInZipAndNameCallback;
import com.socialapp.services.internal.callback.custom.ProcessableCallback;
import com.socialapp.services.internal.callback.custom.sharedstate.LoginState.LoginResult;
import com.socialapp.services.internal.util.UrlConstants;
import com.socialapp.services.util.Assert;
import com.socialapp.services.util.PartnerAppConstants;
import com.socialapp.services.util.PartnerAppFeature;
import com.socialapp.services.util.PartnerappServiceUtils;
import com.socialapp.services.util.ServerUtils;
import com.socialapp.services.util.Tuple;

public class LoginState extends ProcessableCallback<LoginResult> {
	
	public enum LoginResult{
		NONE,SUCCESS,WRONG_PASS,WRONG_EMAIL
	}

	public static String phpsessid;
	public static volatile String email;
	public static volatile int memberID;
	private Credentials credentials;
	private File filesDir;
	
	private static volatile IGoogleCloudMessaging gcm;


	public LoginState(IGoogleCloudMessaging gcm, File filesDir, IResultProcessor<LoginResult> processor) {
		super(processor);
		this.filesDir = filesDir;
		this.gcm = gcm;
	}

	@Override
	public void callback(String url, String object, AjaxStatus status) {
		super.callback(url, object, status);
		if (object == null) {
			finalize(LoginResult.NONE, new Object[]{});
			return;
		} else if (object.contains(PartnerAppConstants.WRONG_PASS)) {
			finalize(LoginResult.WRONG_PASS, new Object[]{});
			return;
		}
		
		File sessFile = new File(filesDir,
				PartnerAppConstants.CREDENTIALS__FILE);

		if (object.contains(UrlConstants.LOGOUT)) { // logged in successfully
			persistCredentials(status, sessFile);
			finalize(LoginResult.SUCCESS, new Object[]{});
			updateUserDataOnServer();
		}else{	// wrong email
			finalize(LoginResult.WRONG_EMAIL, new Object[]{});
			return;
		}
	}

	public void updateUserDataOnServer() {	// oh man, thats ugly. To many chained callbacks!
		final AQuery aq = new AQuery();
		GetLoggedInZipAndNameCallback getZipCallback = new GetLoggedInZipAndNameCallback(new IResultProcessor<Tuple<String, String>>() {
			
			public void process(final Tuple<String, String> zipAndName, Object... params) {
				GetLoggedInMemberInterestsCallback getInterestsCallback = new GetLoggedInMemberInterestsCallback(new IResultProcessor<Integer[]>() {
					
					public void process(final Integer[] interests, Object... params) {
						GetLoggedInIDCallback getIdCallback = new GetLoggedInIDCallback(new IResultProcessor<String>() {

							public void process(String memberid, Object... params) {
								try{
									LoginState.memberID = Integer.parseInt(memberid);
								}catch(NumberFormatException ex){
									System.err.println(PartnerAppFeature.LOGIN.name() + " invalid format of memberID!");
									ex.printStackTrace();
								}
								doUpdateUserDataOnServer(email, memberid);
							}
							
						}, zipAndName.getValue(), interests, zipAndName.getKey());
						aq.ajax(getIdCallback.getUrl(), getIdCallback);
					}
					
				});
				aq.ajax(getInterestsCallback.getUrl(), getInterestsCallback);
			}
			
		});
		aq.ajax(getZipCallback.getUrl(), getZipCallback);
	}
	
	private static void doUpdateUserDataOnServer(final String email, final String memberid) {
		new Thread(new Runnable() {
			
			public void run() {
				String regid = null;
				try {
					regid = gcm.register(PartnerAppConstants.GSM_PROJECT_NUMBER);
				} catch (RuntimeException ex) {
					System.err.println(PartnerAppFeature.WEB.toString() + " error updateUserDataOnServer in LoginStat");
					ex.printStackTrace();

				}				
				if(StringUtils.isEmpty(memberid)){
					System.err.println(PartnerAppFeature.WEB.name() + " memberid is empty!");
					return;
				}
				int memberidAsInt = 0;
				try{
					memberidAsInt = Integer.parseInt(memberid);
				}catch(RuntimeException ex){
					System.err.println(PartnerAppFeature.WEB.name() + " memberid is not a number!");
					ex.printStackTrace();
				}
				ServerUtils.updateUserDataOnServer(email, memberidAsInt, null, regid);
			}
		}).start();
	}

	private void setDefaultInterest() {
		AQuery aq = new AQuery();
		ProcessableCallback<String> cb = ProcessableCallback.create();
		cb.cookie(PartnerAppConstants.PHPSESSID, LoginState.phpsessid);
		aq.ajax(UrlConstants.SET_DEFAULT_INTEREST_URL, cb);
	}

	private void persistCredentials(AjaxStatus status, File sessFile) {
		phpsessid = loginToken(status);
		email = credentials.getUser();
		FileWriter fWriter = null;
		try {
			fWriter = new FileWriter(sessFile);
			getCredentials();
			fWriter.write(credentials.getUser());
			fWriter.write("\n");
			fWriter.write(credentials.getPass());
			fWriter.flush();
			fWriter.close();
		} catch (Throwable e) {
			e.printStackTrace();
			try {
				fWriter.close();
			} catch (Throwable e1) {
			}
		}
	}

	private Credentials getCredentials() {
		return credentials;
	}

	private String loginToken(AjaxStatus status) {
		for (Cookie cookie : status.getCookies()) {
			if (cookie.getName().equals(PHPSESSID)) {
				return cookie.getValue();
			}
		}
		return null;
	}

	public static boolean isLoggedIn() {
		return phpsessid != null;
	}


	public static void login(IGoogleCloudMessaging gcm, String email,String pass, File filesDir, IResultProcessor<LoginResult> resultHandler) {
		checkState(email, pass);
		String url = APP_DOMAIN + LOGIN;
		Map<String, Object> params = Collections.emptyMap();
		LoginState cb = new LoginState(gcm,filesDir, resultHandler);
		if(gcm != null){
			gcm.create(cb);
		}
		if (email != null && pass != null) {
			params = cb.initParams(email, pass);
		}
		cb.timeout(PartnerAppConstants.TIMEOUT_SHORT);
		new AQuery().ajax(url, params, cb);
	}

	private static void checkState(String email, String pass) {
		if (!LoginState.isLoggedIn()) {
			Assert.isNotNull(email,
					"you need to specify a email if you want to login!");
			Assert.isNotNull(pass,
					"you need to specify a password if you want to login!");
		}
	}

	private Map<String, Object> initParams(String email, String pass) {
		credentials = new Credentials(email, pass);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(UrlConstants.POST_KEY_EMAIL, email);
		params.put(UrlConstants.POST_KEY_PASS, pass);
		params.put(UrlConstants.POST_KEY_HASH, PartnerappServiceUtils.md5(email));
		return params;
	}

	@Override
	public boolean shouldLog() {
		return false;
	}

	@Override
	public String getLogTableName() {
		return null;
	}
}
