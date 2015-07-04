package com.socialapp.services.internal.callback.custom;

import java.text.ParseException;
import java.util.Date;
import java.util.regex.Matcher;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.socialapp.services.IResultProcessor;
import com.socialapp.services.dao.chat.MessageFrame;
import com.socialapp.services.internal.callback.AjaxStatus;
import com.socialapp.services.internal.parser.PartnerappDomConstants;
import com.socialapp.services.util.SocialappServiceConstants;
import com.socialapp.services.util.SocialappFeature;
import com.socialapp.services.util.Tuple;


public class GetChatMessageCallback extends ProcessableCallback<String> {

	final Tuple<MessageFrame, Object> chatMessage;

	public GetChatMessageCallback(IResultProcessor<String> proc, Tuple<MessageFrame, ?> chatMessage) {
		super(proc);
		this.chatMessage = (Tuple<MessageFrame, Object>) chatMessage;
	}

	@Override
	public void callback(String request, String response, AjaxStatus status) {
		try {
			Document document = Jsoup.parse(response);
			Element mail = document.getElementsByClass(
					PartnerappDomConstants.CLASS_MAILTEXT).get(0);
			Element mailTr = mail.parent().parent();
			Element dateTr = mailTr.previousElementSibling().previousElementSibling();
			String dateWithSeconds = dateTr.child(1).ownText();
			Date date = SocialappServiceConstants.DATE_TIME_SECONDS_FORMAT_WEBSITE_FORMAT.parse(dateWithSeconds);
			MessageFrame messageFrame = chatMessage.getKey();
			messageFrame.setDate(date);
			String resolved = mail.text();
			if (resolved.contains("\n>")) { // remove the quoted text
				Matcher matcher = SocialappServiceConstants.QUOTED_TEXT_PATTERN
						.matcher(resolved);
				resolved = matcher.replaceAll("");
			}
			chatMessage.setValue(resolved);
		} catch (RuntimeException ex) {
			System.err.println(SocialappFeature.CHAT.name() + " error while retriveing chat message");
			ex.printStackTrace();
		} catch (ParseException e) {
			System.err.println(SocialappFeature.CHAT.name() + " error while parsing date of chat message");
			e.printStackTrace();
		}
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
