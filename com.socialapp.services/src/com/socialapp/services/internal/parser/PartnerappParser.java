package com.socialapp.services.internal.parser;

import static com.socialapp.services.internal.util.UrlConstants.APP_DOMAIN;
import static com.socialapp.services.internal.util.UrlConstants.MAILBOX;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.socialapp.services.IResultProcessor;
import com.socialapp.services.dao.City;
import com.socialapp.services.dao.Member;
import com.socialapp.services.dao.chat.MessageFrame;
import com.socialapp.services.internal.callback.AQuery;
import com.socialapp.services.internal.callback.custom.GetChatMessageCallback;
import com.socialapp.services.internal.callback.custom.sharedstate.LoginState;
import com.socialapp.services.internal.util.UrlConstants;
import com.socialapp.services.persistence.IDataSource;
import com.socialapp.services.util.PartnerAppConstants;
import com.socialapp.services.util.PartnerAppFeature;
import com.socialapp.services.util.Tuple;


public class PartnerappParser {

	private final IDataSource datasource;

	public PartnerappParser(IDataSource datasource) {
		this.datasource = datasource;
	}

	public List<Member> createMembersListFromEntries(List<Element> from,
			int interest, String gender) {
		List<Member> members = new ArrayList<Member>();
		for (Element entry : from) {
			String g;
			String imageID = getImageId(entry);
			if (interest == 0) {
				// skip members with no image when logged out
				if (!LoginState.isLoggedIn() && imageID == null) {
					continue;
				}
				g = gender(entry);
			} else {
				g = "";
			}
			if (gender.equals(g) || gender.equals("") || g.equals("")) {
				Member member = new Member();
				if (imageID != null) {
					member.setHasImage(true);
				}
				String memberId = getMemberId(entry, interest);
				if (memberId != null) {
					member.setId(memberId);
					String cityName = getCityName(entry, interest);
					City city = datasource.localQueryForCityByName(cityName,
							interest, false);
					if (city.getZip() == 0) {
						city.setZip(getZip(entry));
					}
					member.setZip(String.valueOf(city.getZip()));
					member.setName(getName(entry, interest));
					Elements personInfos = getPersonInfos(entry, interest);
					if (getAbout(personInfos, interest) != null) {
						member.setAbout(getAbout(personInfos, interest)
								.ownText());
					}
					member.setCity(cityName);
					member.setCommoninterests("0d");
					if (getDesires(personInfos, interest) != null) {
						member.setDesires(getDesires(personInfos, interest)
								.ownText());
					}

					if (getHobbies(personInfos, interest) != null) {
						member.setHobbies(getHobbies(personInfos, interest)
								.ownText());
					}
					member.setImage(imageID);
					if (getInterests(personInfos, interest) != null) {
						member.setInterests(getInterests(personInfos, interest)
								.ownText());
					}
					members.add(member);
				}
			}
		}
		return members;
	}

	private String gender(Element entry) {
		String gender = null;
		try {
			gender = entry.getElementsByTag("td").get(0).className();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return gender;
	}

	private int getZip(Element entry) {
		int result = 0;
		try {
			String location = entry.getElementsByTag("td").get(0).ownText();
			Matcher matcher = PartnerAppConstants.ZIP_PATTERN.matcher(location);
			if (matcher.find()) {
				location = matcher.group();
				result = Integer.parseInt(location);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return result;
	}

	public String getImageId(Element entry) {
		String result = null;
		try {
			Elements imgTag = entry.getElementsByTag("img");
			if (!imgTag.isEmpty()) {
				String imgUrl = imgTag.get(0).attr("src");
				if (imgUrl.contains("/")) {
					result = split(imgUrl, "/.-")[1];
				} else {
					result = split(imgUrl, ".-")[0];
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return result;
	}

	private Elements getPersonInfos(Element entry, int interest) {
		Elements result = null;
		if (LoginState.isLoggedIn()
				|| interest == 0) {

		} else {
			result = entry.getElementsByTag("span");
		}
		return result;
	}

	public String getName(Element entry) {
		String result = null;
		Element item = entry.children().get(1);
		result = item.getElementsByTag("a").get(0).ownText();
		return result;
	}

	private String getName(Element entry, int interest) {
		String result = null;
		try {
			if (LoginState.isLoggedIn()
					|| interest == 0) {
				result = entry.getElementsByClass("red").get(0).ownText();
			} else {
				result = split(getNameAndLocation(entry), " ")[0];
			}
		} catch (Throwable e) {
			e.printStackTrace();
			result = "FEHLER!";
		}
		return result;
	}

	private String getCityName(Element entry, int interest) {
		String result = null;
		try {
			if (LoginState.isLoggedIn()
					|| interest == 0) {
				result = getLocationWithDistance(entry, interest);
				result = split(result, "  ")[1];
			} else {
				String[] splittedBySpace = split(getNameAndLocation(entry), " ");
				result = splittedBySpace[3];
			}
		} catch (Throwable e) {
			e.printStackTrace();
			result = "FEHLER!";
		}
		return result;
	}

	private String getLocationWithDistance(Element entry, int interest) {
		String result = null;
		try {
			if (LoginState.isLoggedIn()
					|| interest == 0) {
				result = entry.getElementsByClass("red").get(0).parent()
						.ownText();
				if (result.contains("Mitglied bei ")) {
					result = substringAfter(result, "Mitglied bei ");
				}
			} else {
			}
		} catch (Throwable e) {
			e.printStackTrace();
			result = "FEHLER!";
		}
		return result;
	}

	private String getNameAndLocation(Element entry) {
		return entry.getElementsByTag("h3").get(0).ownText();
	}

	public String getMemberId(Element element) {
		String result = null;
		try {
			Element item = element.children().get(1);
			int index = item.toString().indexOf(UrlConstants.VIEW_PROFILE);
			result = item.toString().substring(
					index + UrlConstants.VIEW_PROFILE.length());
			result = result.substring(0, result.indexOf('"'));
		} catch (Throwable e) {
			// ignore
		}
		return result;
	}

	private String getMemberId(Element element, int interest) {
		String result = null;
		if (interest == 0) {
			Elements cl = element.getElementsByClass("addcontact");
			result = cl.attr("href");
		} else if (LoginState.isLoggedIn()) {
			Elements redClass = element.getElementsByClass("red");
			result = substringAfterLast(redClass.attr("href"), "=");
		}
		if (result == null || result.equals("")) {
			result = "";
		}
		return result;
	}

	private Element getInterests(Elements personInfos, int interest) {
		Element result = null;
		if (LoginState.isLoggedIn()
				|| interest == 0) {

		} else {
			result = getInfo(personInfos, "Interessen");
		}
		return result;
	}

	private Element getHobbies(Elements personInfos, int interest) {
		Element result = null;
		if (LoginState.isLoggedIn()
				|| interest == 0) {

		} else {
			result = getInfo(personInfos, "Hobbies");
		}
		return result;
	}

	private Element getDesires(Elements personInfos, int interest) {
		Element result = null;
		if (LoginState.isLoggedIn()
				|| interest == 0) {

		} else {
			result = getInfo(personInfos, "nsche"); // Wünsche
		}
		return result;
	}

	private Element getAbout(Elements personInfos, int interest) {
		Element result = null;
		if (LoginState.isLoggedIn()
				|| interest == 0) {

		} else {
			result = getInfo(personInfos, "ber mich"); // Über mich
		}
		return result;
	}

	private Element getInfo(Elements personInfos, String info) {
		int index = getIndex(info, personInfos);
		Element result = null;
		if (index > -1) {
			return personInfos.get(index).parent();
		}
		return result;
	}

	private int getIndex(String string, Elements personInfos) {
		for (int i = 0; i < personInfos.size(); i++) {
			if (personInfos.get(i).ownText().endsWith(string)) {
				return i;
			}
		}
		return -1;
	}

	public void resolveChatMessage(Tuple<MessageFrame, ?> chatMessage) {
		if (chatMessage.getValue() instanceof Element) {
			String uid = chatMessage.getValue().toString();
			String box;
			int index = uid.indexOf(PartnerappDomConstants.PREFIX_MESSAGE_INBOX_UID);
			if (index != -1) {
				uid = uid.substring(index+ PartnerappDomConstants.PREFIX_MESSAGE_INBOX_UID
								.length());
				uid = uid.substring(0, uid.indexOf("'"));
				box = UrlConstants.POST_VALUE_INBOX;
			} else {
				index = uid
						.indexOf(PartnerappDomConstants.PREFIX_MESSAGE_OUTBOX_UID);
				if (index == -1) {
					return;
				}
				uid = uid.substring(index
						+ PartnerappDomConstants.PREFIX_MESSAGE_OUTBOX_UID
								.length());
				uid = uid.substring(0, uid.indexOf("'"));
				box = UrlConstants.POST_VALUE_OUTBOX;
			}
			String url = APP_DOMAIN + MAILBOX;
			Map<String, Object> params = new HashMap<String, Object>();
			AQuery aq = new AQuery();
			GetChatMessageCallback callback = new GetChatMessageCallback(
					IResultProcessor.NULL_PROCESSOR, chatMessage);

			params = new HashMap<String, Object>();
			callback.cookie(PartnerAppConstants.PHPSESSID, LoginState.phpsessid);
			params.put(UrlConstants.POST_KEY_FOLDER, box);
			params.put(UrlConstants.POST_KEY_ACTION,
					UrlConstants.POST_VALUE_READ);
			params.put(UrlConstants.POST_KEY_UID, uid);
			callback.setParameter(params);
			aq.ajax(url, params, callback);
			callback.block();
		}
	}
	
	public Date readChatMessageDateFromHtml(Element entry){
		Date date = null;
		String dateString = entry.getElementsByTag("td").get(4).ownText();
		try {
			date = PartnerAppConstants.DATE_TIME_FORMAT_WEBSITE_FORMAT.parse(dateString);
		} catch (ParseException e) {
			System.err.println(PartnerAppFeature.CHAT.name()+ " couldn't parse date from string: " +dateString);
			e.printStackTrace();
		}
		return date;
	}
			

}
