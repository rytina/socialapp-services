package com.socialapp.services.util;



public enum SocialappFeature implements org.togglz.core.Feature{
	UNKNOWN,
	CORE,
	LOGIN, 
	P2P_WITH_NET4J, 
	P2P_WITHOUT_NET4J,
	CHAT,
	WEB,
	ADS;
	
    public boolean isActive() {
    	boolean isActive = false;
    	switch (this) {
    	case CORE:
			isActive = true;
			break;
		case LOGIN:
			isActive = true;
			break;
		case P2P_WITH_NET4J:
			isActive = false;
			break;
		case P2P_WITHOUT_NET4J:
			isActive = true;
			break;
		case ADS:
			isActive = true;
			break;
		case CHAT:
			isActive = true;
			break;
		case WEB:
			isActive = true;
			break;
		default:
			break;
		}
    	return isActive;
    }
}
