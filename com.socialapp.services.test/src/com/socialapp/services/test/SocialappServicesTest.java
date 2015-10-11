package com.socialapp.services.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import com.socialapp.services.IResultProcessor;
import com.socialapp.services.internal.callback.AQuery;
import com.socialapp.services.internal.callback.AjaxStatus;
import com.socialapp.services.internal.callback.custom.ProcessableCallback;
import com.socialapp.services.internal.callback.custom.RegistrationCalllback;
import com.socialapp.services.internal.callback.custom.sharedstate.LoginState;
import com.socialapp.services.internal.callback.custom.sharedstate.LoginState.LoginResult;
import com.socialapp.services.internal.util.UrlConstants;
import com.socialapp.services.util.SocialappServiceUtils;

public class SocialappServicesTest extends AbstractSocialappServicesTest{


	@Test
	public void testResponseIsNotNull() {
		
		
		String url = "http://www.google.com";
		AQuery aq = new AQuery();
        
        aq.ajax(url, new ProcessableCallback<String>(null) {

            public void callback(String url, String resp, AjaxStatus status) {
            	SocialappServicesTest.this.status = status;
            	SocialappServicesTest.this.response = resp;
            }

			@Override
			public boolean shouldLog() {
				return false;
			}

			@Override
			public String getLogTableName() {
				return null;
			}
        });
		
        waitForCallback();
        assertNotNull("the response must not be null!", response);
	}
	
	@Test
	public void testLoginWithWrongPassword() {
		
		LoginState.login(null, "eliasryt@gmail.com", "somewrongpassword", new File(""), new IResultProcessor<LoginResult>(){

			@Override
			public void process(LoginResult result, Object... params) {
            	SocialappServicesTest.this.response = result;				
			}
			
		});
		
        waitForCallback();
        assertNotNull("the response must not be null!", response);
        assertEquals(LoginResult.WRONG_PASS, response);
	}
	
	@Test
	public void testLoginWithWrongEmail() {
		
		LoginState.login(null, "xxxxxx@gmail.com", "somewrongpassword", new File(""), new IResultProcessor<LoginResult>(){

			@Override
			public void process(LoginResult result, Object... params) {
            	SocialappServicesTest.this.response = result;				
			}
			
		});
		
        waitForCallback();
        assertNotNull("the response must not be null!", response);
        assertEquals(LoginResult.WRONG_EMAIL, response);
	}
	
	@Test
	public void testRegisterWithInvalidEmail() throws UnsupportedEncodingException{
		final String email = "ungültigeemail";
		final String pass = "1234";
		
		RegistrationCalllback registrationCallback = new RegistrationCalllback(
				email, pass, new IResultProcessor<Boolean>() {

					public void process(Boolean result,	Object... params) {
						if(params != null && params.length > 0){
							if(params[0] instanceof AjaxStatus){
								SocialappServicesTest.this.status = (AjaxStatus) params[0];
								SocialappServicesTest.this.response = result;
							}else if(params[0] instanceof String){
								SocialappServicesTest.this.response = params[0];
							}
						}
					}
					
					

				}){
			@Override
			public boolean shouldLog() {
				return false;
			}
		};
		
		Map<String, String> params = new HashMap<String, String>();

		String name = URLEncoder.encode("Müller","UTF-8");
		params.put("name", name);
		params.put("forename", URLEncoder.encode("Sören","UTF-8"));
		params.put("country", URLEncoder.encode("DE","UTF-8"));
		params.put("zip", "73575");
		params.put("city", URLEncoder.encode("Leinzell","UTF-8"));
		params.put("email", email);
		params.put("email_wdh", email);
		params.put("b_day", "17");
		params.put("b_month", "12");
		params.put("b_year", "1981");
		params.put("single", "yes");
		params.put("sex", "m");
		params.put("password", pass);
		params.put("password_wdh", pass);
		params.put("agb", "1");
		params.put("cp", SocialappServiceUtils.md5(name));
		registrationCallback.setParameter(params);
		registrationCallback.header("Content-Type",
				"application/x-www-form-urlencoded;charset=UTF-8");
		new AQuery().ajax(UrlConstants.APP_DOMAIN + UrlConstants.MITMACHEN, params, registrationCallback);
		
        waitForCallback();
        assertNotNull("the response must not be null!", response);
        assertEquals("Bitte gültige E-Mailadresse eingeben.", response);
	}
	
	@Test
	public void testRegisterWithInvalidPassword() throws UnsupportedEncodingException{
		final String email = "garfield992k@gmail.com";
		final String pass = "123";
		
		RegistrationCalllback registrationCallback = new RegistrationCalllback(
				email, pass, new IResultProcessor<Boolean>() {

					public void process(Boolean result,	Object... params) {
						if(params != null && params.length > 0){
							if(params[0] instanceof AjaxStatus){
								SocialappServicesTest.this.status = (AjaxStatus) params[0];
								SocialappServicesTest.this.response = result;
							}else if(params[0] instanceof String){
								SocialappServicesTest.this.response = params[0];
							}
						}
					}
					
					

				}){
			@Override
			public boolean shouldLog() {
				return false;
			}
		};
		
		Map<String, String> params = new HashMap<String, String>();

		String name = URLEncoder.encode("Müller","UTF-8");
		params.put("name", name);
		params.put("forename", URLEncoder.encode("Sören","UTF-8"));
		params.put("country", URLEncoder.encode("DE","UTF-8"));
		params.put("zip", "73575");
		params.put("city", URLEncoder.encode("Leinzell","UTF-8"));
		params.put("email", email);
		params.put("email_wdh", email);
		params.put("b_day", "17");
		params.put("b_month", "12");
		params.put("b_year", "1981");
		params.put("single", "yes");
		params.put("sex", "m");
		params.put("password", pass);
		params.put("password_wdh", pass);
		params.put("agb", "1");
		params.put("cp", SocialappServiceUtils.md5(name));
		registrationCallback.setParameter(params);
		registrationCallback.header("Content-Type",
				"application/x-www-form-urlencoded;charset=UTF-8");
		new AQuery().ajax(UrlConstants.APP_DOMAIN + UrlConstants.MITMACHEN, params, registrationCallback);
		
        waitForCallback();
        assertNotNull("the response must not be null!", response);
        assertEquals("Das Password darf nur aus folgenden Zeichen bestehen: a - Z, 0 - 9, -, _, . und muss mindestens 5 Zeichen lang sein.", response);
	}
	
	@Test
	public void testUploadImage() throws URISyntaxException {
	
		ProcessableCallback<String> callback = new ProcessableCallback<String>(null) {

            public void callback(String url, String resp, AjaxStatus status) {
            	SocialappServicesTest.this.status = status;
            	SocialappServicesTest.this.response = resp;
            }

			@Override
			public boolean shouldLog() {
				return false;
			}

			@Override
			public String getLogTableName() {
				return null;
			}
        };
		
		
		Map<String, String> params = new HashMap<String, String>();

		params.put("image", getReferenceImageEncodedWithBase64());
		callback.setParameter(params);
		callback.header("Content-Type",
				"application/x-www-form-urlencoded;charset=UTF-8");
		new AQuery().ajax("http://rytina.net/partnerapp/save_image.php", params, callback);
        waitForCallback();
        assertNotNull("the response must not be null!", response);
        assertEquals("OK\n", response);
	}
	
	public static String getReferenceImageEncodedWithBase64() throws URISyntaxException {
		String imageDataString = null;
        File file = new File(SocialappServicesTest.class.getResource("reference.jpg").toURI());
 
        try {            
            // Reading a Image file from file system
            InputStream imageInFile = new FileInputStream(file);
            byte imageData[] = new byte[(int) file.length()];
            imageInFile.read(imageData);
 
            // Converting Image byte array into Base64 String
            imageDataString = encodeImage(imageData);
 
            imageInFile.close();
        } catch (FileNotFoundException e) {
            System.out.println("Image not found" + e);
        } catch (IOException ioe) {
            System.out.println("Exception while reading the Image " + ioe);
        }
        return imageDataString;
    }
	
	 /**
     * Encodes the byte array into base64 string
     *
     * @param imageByteArray - byte array
     * @return String a {@link java.lang.String}
     */
    public static String encodeImage(byte[] imageByteArray) {
        return Base64.encodeBase64String(imageByteArray);
    }
 
    /**
     * Decodes the base64 string into byte array
     *
     * @param imageDataString - a {@link java.lang.String}
     * @return byte array
     */
    public static byte[] decodeImage(String imageDataString) {
        return Base64.decodeBase64(imageDataString);
    }

}
