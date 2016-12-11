package com.socialapp.services.util;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Pattern;


public class SocialappServiceConstants {
	
	public enum MemberColumn{
		image,				// 0.
		name,				// 1.
		city,				// 3.
		//dist,				// 4.
		text,				// 4.
		hobbies,			// 5.
		desires,			// 6.
		interests,			// 7.
		_id					// 8.
	}
	
	public static final String SOCIAL_APP_VERSION = "19.0";
	
	public static final String PARAMETER_SEARCH_LOCATION = "PARAMETER_SEARCH_LOCATION";
	
	public static final String PARAMETER_SEARCH_LOCATION_FIELD = "PARAMETER_SEARCH_LOCATION_FIELD";
	
	public static final String PARAMETER_SEARCH_GENDER = "PARAMETER_SEARCH_GENDER";
	
	public static final String PARAMETER_SEARCH_INTEREST = "PARAMETER_SEARCH_INTEREST";

	public static final String PARAMETER_SEARCH_PHOTOID = "PARAMETER_SEARCH_PHOTOID";

	public static final String PARAMETER_MEMBER_IMAGE = "PARAMETER_MEMBER_IMAGE";

	public static final String PARAMETER_MEMBER_NAME = "PARAMETER_MEMBER_NAME";

	public static final String PARAMETER_MEMBER_CITY = "PARAMETER_MEMBER_CITY";
	
	public static final String PARAMETER_MEMBER_ZIP = "PARAMETER_MEMBER_ZIP";

	public static final String PARAMETER_MEMBER_DIST = "PARAMETER_MEMBER_DIST";

	public static final String PARAMETER_MEMBER_ABOUT = "PARAMETER_MEMBER_ABOUT";

	public static final String PARAMETER_MEMBER_HOBBIES = "PARAMETER_MEMBER_HOBBIES";

	public static final String PARAMETER_MEMBER_DESIRES = "PARAMETER_MEMBER_DESIRES";

	public static final String PARAMETER_MEMBER_INTERESTS = "PARAMETER_MEMBER_INTERESTS";
	
	public static final String PARAMETER_MEMBER_UID = "PARAMETER_MEMBER_UID";
	
	public static final String PARAMETER_MEMBER_CHATMESSAGES = "PARAMETER_MEMBER_CHATMESSAGES";

	public static final String REGISTRATION_SUCCESS_TEXT = "Du erh&auml;ltst jetzt eine E-Mail zur Best&auml;tigung Deiner kostenlosen Registrierung";

	public static final String WELCOME = "Willkommen!";


	public static final String PHPSESSID = "PHPSESSID";
	public static final String WRONG_PASS = "Falsches Passwort";
	public static final String WRONG_EMAIL = "Falsche Email-Adresse";
	public static final String STATUS_OK = "OK";
	
	public static final int TIMEOUT_SHORT = 10000;
	public static final int TIMEOUT_NORMAL = TIMEOUT_SHORT * 2;
	public static final int TIMEOUT_LONG = TIMEOUT_NORMAL * 2;
	
	public static final String CREDENTIALS__FILE = "SESSION__FILE";

	public static final String PREVIOUS_SEARCH_SESSION_FILE = "PREVIOUS_SEARCH_SESSION_FILE";

	public static final String UUID_FILE = "UUID_FILE";
	
	public static final String SETTINGS_FILE = "PartnerappUiSettings";
	
	public static Pattern ZIP_PATTERN = Pattern.compile("\\d{5}");
	
	public static Pattern ACTIVEPAGE_PATTERN = Pattern.compile(" (\\d)+ ?<");
	
	public static Pattern QUOTED_TEXT_PATTERN = Pattern.compile("\n>+[^\n]*");
	
	public static int LOAD_MORE_LIST_INCREMENT = 10;

	public static int IMAGE_EAGER_LOAD_AMOUNT = 20;

	public static Object DEFAULT_BITMAP;	
	
	public static final String CHAT_PORT = "4048";
	
	public static final String IP_ADDRESS = "192.168.43.19";
	
	public static final String CHAT_BROKER_IP = "188.138.101.116";

//	public static final String CHAT_BROKER_IP = "192.168.178.37";
	
	public static final Integer CHAT_BROKER_PORT = 52743;
	
	public static final int CHAT_BROKER_KEEPALIVE_SECONDS = 600;

	public static final String EMPTYSTRING = "";
	
	public static final String CERTIFICATE_DEBUG = "78d4792d5c8c7ad8a7d0ef6337498cbf90cc28ff";
	
	public static final String CERTIFICATE_PLAYSTORE = "";
	
	public static final String NO_MESSAGES_IN_INBOX = "Keine Nachrichten im Posteingang!";
	
private static final String DATE_TIME_FORMAT_WEBSITE = "dd.MM.yyyy HH:mm";
	
	public static final SimpleDateFormat DATE_TIME_FORMAT_WEBSITE_FORMAT = new SimpleDateFormat(SocialappServiceConstants.DATE_TIME_FORMAT_WEBSITE,Locale.GERMANY);

	private static final String DATE_TIME_SECONDS_FORMAT_WEBSITE = "dd.MM.yyyy HH:mm:ss";
	
	public static final SimpleDateFormat DATE_TIME_SECONDS_FORMAT_WEBSITE_FORMAT = new SimpleDateFormat(SocialappServiceConstants.DATE_TIME_SECONDS_FORMAT_WEBSITE,Locale.GERMANY);
	
	private static final String DATE_TIME_FORMAT_MYSQL = "yyyy-MM-dd HH:mm:ss";
	
	public static final SimpleDateFormat DATE_TIME_FORMAT_MYSQL_FORMAT = new SimpleDateFormat(SocialappServiceConstants.DATE_TIME_FORMAT_MYSQL,Locale.GERMANY);

	public static final int NUMBER_OF_MESSAGES_TO_SHOW = 6;
	
	public static final String GSM_PROJECT_NUMBER = "478717961140";
	
	public static final String MESSAGE_METADATA_SEPARATOR = ']' + Tuple.TUPLE_SEPARATOR;

	public static final boolean IS_ACTIONBAR_AT_TOP_BY_DEFAULT = false;

	public static final String NO_MEMBERS_FOUND = "Keine passenden Mitglieder gefunden!";

}
