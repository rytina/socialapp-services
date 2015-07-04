package com.socialapp.services.dao;


import static com.socialapp.services.internal.util.UrlConstants.APP_DOMAIN;
import static com.socialapp.services.util.SocialappServiceConstants.NUMBER_OF_MESSAGES_TO_SHOW;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jsoup.nodes.Element;

import com.socialapp.services.dao.chat.MessageBox;
import com.socialapp.services.dao.chat.MessageFrame;
import com.socialapp.services.internal.parser.PartnerappParser;
import com.socialapp.services.util.SocialappFeature;
import com.socialapp.services.util.SocialappServiceUtils;
import com.socialapp.services.util.Tuple;

public class Member {

	private String _id;
	private String image;
	private String name;
	private String city;
	private String zip;
	private String commoninterests;
	private String text;
	private String hobbies;
	private String desires;
	private String interests;
	private boolean hasImage;
	private String imageURL;
	private volatile Date lastInteractionStamp;
	
	// I used here a linked list for a better performance because the items are inserted at the sorted index using LinkedList.add(int index, E element); 
	private LinkedList<Tuple<MessageFrame,Object>> htmlMessageEntries = new LinkedList<Tuple<MessageFrame,Object>>();
	private PartnerappParser parser;

	public Member() {
		this.parser = new PartnerappParser(null);
	}
	

	public String getImageID() {
		return image;
	}
	
	public String getImageURL(){
		return imageURL;
	}
	
	public String getImageURL(int resolution){
		imageURL = getImageURL(resolution, image);
		return imageURL;
	}
	
	public static String getImageURL(int resolution, String imageID){
		String url = null;
		if(imageID != null){
			url = APP_DOMAIN + "Bilder/"+imageID+"-"+resolution+"-"+resolution+".jpg";
		}
		return url;
	}
	
	public String get(String columnName) throws IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException {
		return (String) this.getClass().getDeclaredField(columnName).get(this);
	}
	
	

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getId() {
		return _id;
	}

	public void setId(String _id) {
		this._id = _id;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCommoninterests() {
		return commoninterests;
	}

	public void setCommoninterests(String commoninterests) {
		this.commoninterests = commoninterests;
	}

	public String getAbout() {
		return text;
	}

	public void setAbout(String about) {
		this.text = about;
	}

	public String getHobbies() {
		return hobbies;
	}

	public void setHobbies(String hobbies) {
		this.hobbies = hobbies;
	}

	public String getDesires() {
		return desires;
	}

	public void setDesires(String desires) {
		this.desires = desires;
	}

	public String getInterests() {
		return interests;
	}

	public void setInterests(String interests) {
		this.interests = interests;
	}


	@Override
	public String toString() {
		return "Member ["
				+ (_id != null ? "_id=" + _id + ", " : "")
				+ (image != null ? "image=" + image + ", " : "")
				+ (name != null ? "name=" + name + ", " : "")
				+ (city != null ? "city=" + city + ", " : "")
				+ (zip != null ? "zip=" + zip + ", " : "")
//				+ (dist != null ? "dist=" + dist + ", " : "")
				+ (commoninterests != null ? "commoninterests="
						+ commoninterests + ", " : "")
				+ (text != null ? "text=" + text + ", " : "")
				+ (hobbies != null ? "hobbies=" + hobbies + ", " : "")
				+ (desires != null ? "desires=" + desires + ", " : "")
				+ (interests != null ? "interests=" + interests + ", " : "") + "]";
	}

	public void setHasImage(boolean b) {
		hasImage = b;
	}
	
	public boolean hasImage() {
		return hasImage;
	}
	
	public Integer[] getInterestsIdArray(){
		List<Integer> result = new ArrayList<Integer>();
		Integer[] resultArray = null;
		String[] interestsStrArray = interests.split(",");
		if(interestsStrArray.length > 0){
			Map<String, Integer> idMap = SocialappServiceUtils.getInterestNameToIdMap();
			for (int i = 0; i < interestsStrArray.length; i++) {
				Integer interest = idMap.get(interestsStrArray[i].trim());
				result.add(interest);
			}
		}
		if(!result.isEmpty()){
			resultArray = result.toArray(new Integer[interestsStrArray.length]);
		}
		return resultArray;
	}

	public void addHtmlMessageEntry(Tuple<MessageBox,Element> html) {
		Date date = parser.readChatMessageDateFromHtml(html.getValue());
		Tuple<MessageFrame, Object> entry = new Tuple<MessageFrame, Object>(new MessageFrame(date, html.getKey()), html.getValue());
		int sortedIndex = 0;
		for (Tuple<MessageFrame, Object> tuple : this.htmlMessageEntries) {
			if(tuple.getKey().getDate().compareTo(date) == 1){
				sortedIndex++;
			}else{
				break;
			}
		}
		this.htmlMessageEntries.add(sortedIndex, entry);
	}
	

	public Tuple<MessageFrame,Object> getLastChatMessage() {
		resolveChatMessages(parser, this.htmlMessageEntries);
		return this.htmlMessageEntries.getLast();
	}
	
	public OnlineStatus getOnlineStatus() {
		OnlineStatus onlineStatus = OnlineStatus.OFFLINE;
		if(getLastInteractionStamp() != null){
			long duration  = Calendar.getInstance().getTime().getTime() - getLastInteractionStamp().getTime();
			long diffInMinutes = TimeUnit.MILLISECONDS.toSeconds(duration) / 60;
			if(diffInMinutes < 10){
				onlineStatus = OnlineStatus.ONLINE;
			}
		}
		return onlineStatus;
	}

	private void resolveChatMessages(final PartnerappParser parser, List<Tuple<MessageFrame, Object>> messages) {
			try{
				final int chatMessageSize = messages.size();
				Thread resolveLastChatMessageThread = new Thread(new Runnable() {
					
					public void run() {
						int beginToCount = chatMessageSize < NUMBER_OF_MESSAGES_TO_SHOW 
								? 0 
								: chatMessageSize - NUMBER_OF_MESSAGES_TO_SHOW;
						for (int  i=chatMessageSize-1; i >= beginToCount; i--) {	// only rsolve the latest 6 messages
							final Tuple<MessageFrame, Object> message = htmlMessageEntries.get(i);
							parser.resolveChatMessage(message);
						}
					}
				}, "ResolveChatMessageThread");
				resolveLastChatMessageThread.start();
				
			}catch(Throwable e){
				e.printStackTrace();
				System.err.println(SocialappFeature.CHAT.name() +" error while resolving chat message: ");
			}			
	}

	public Collection<Tuple<MessageFrame,Object>> getHtmlMessageEntries() {
		return Collections.unmodifiableCollection(htmlMessageEntries);
	}

	public ArrayList<String> getChatMessages() {
		ArrayList<String> chatMessageList = new ArrayList<String>();
		int chatMessageSize = htmlMessageEntries.size();
		int beginToCount = chatMessageSize < NUMBER_OF_MESSAGES_TO_SHOW ? 0 : chatMessageSize - NUMBER_OF_MESSAGES_TO_SHOW;
		for (int  i=beginToCount; i < chatMessageSize ; i++) {	// only return the latest 6 messages
			chatMessageList.add(htmlMessageEntries.get(i).toString());
		}
		return chatMessageList;
	}

	public boolean setLastInteractionStamp(Date lastInteractionStamp) {
		boolean changed = false;
		if(this.lastInteractionStamp == null || !this.lastInteractionStamp.equals(lastInteractionStamp)){
			changed = true;
		}
		this.lastInteractionStamp = lastInteractionStamp;
		return changed;
	}

	public Date getLastInteractionStamp() {
		return this.lastInteractionStamp;
	}
}
