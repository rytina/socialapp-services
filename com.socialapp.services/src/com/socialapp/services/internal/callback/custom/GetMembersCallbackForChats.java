package com.socialapp.services.internal.callback.custom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.socialapp.services.IResultProcessor;
import com.socialapp.services.dao.Member;
import com.socialapp.services.dao.chat.MessageBox;
import com.socialapp.services.internal.parser.PartnerappDomConstants;
import com.socialapp.services.internal.parser.PartnerappParser;
import com.socialapp.services.util.PartnerAppFeature;
import com.socialapp.services.util.Tuple;


public class GetMembersCallbackForChats extends AbstractGetMembersCallback {

	public volatile List<Member> chatPartnerFromOutbox;
	private MessageBox boxType;

	public GetMembersCallbackForChats(IResultProcessor<List<Member>> proc, IResultProcessor<Boolean> updateOnlineStatusProcessor, MessageBox boxType) {
		super(proc, updateOnlineStatusProcessor);
		this.boxType = boxType;
	}

	@Override
	public boolean shouldLog() {
		return false;
	}

	@Override
	protected List<Element> extractEntries(Document response) {
		List<Element> result = new ArrayList<Element>();
		Elements content = response
				.getElementsByClass(PartnerappDomConstants.CLASS_TABLE_MAILCONTENT);
		if (content.size() > 0) {
			result = content.get(0).getElementsByClass(
					PartnerappDomConstants.CLASS1_MAILENTRY);
			result.addAll(content.get(0).getElementsByClass(
					PartnerappDomConstants.CLASS2_MAILENTRY));
		}
		return result;
	}

	@Override
	protected List<Member> createMembersListFromEntries(List<Element> entries) {
		Map<String, Member> tmpMemory = new HashMap<String, Member>();
		List<Member> result;
		if (chatPartnerFromOutbox != null && !chatPartnerFromOutbox.isEmpty()) {
			result = chatPartnerFromOutbox;
			for (Member member : result) {
				tmpMemory.put(member.getId(), member);
			}
		} else {
			result = new ArrayList<Member>();
		}
		for (Element entry : entries) {
			try {
				createOrUpdateChatPartner(tmpMemory, result,
						new Tuple<MessageBox, Element>(boxType, entry));
			} catch (RuntimeException ex) {
				System.err.println(PartnerAppFeature.CHAT.name() + " exception during query of chat partner from outbox");
				ex.printStackTrace();
			}
		}
		updateOnlineStatus(result);
		return result;
	}

	private void createOrUpdateChatPartner(Map<String, Member> tmpMemory,
			List<Member> chatPartner, Tuple<MessageBox, Element> entry) {
		PartnerappParser parser = new PartnerappParser(null);
		String id = parser.getMemberId(entry.getValue());
		if (id != null) {
			if (!tmpMemory.containsKey(id)) {
				Member memb = new Member();
				tmpMemory.put(id, memb);
				memb.setId(id);
				String name = parser.getName(entry.getValue());
				memb.setName(name);
				String imageId = parser.getImageId(entry.getValue().child(2));
				memb.setImage(imageId);
				memb.addHtmlMessageEntry(entry);
				chatPartner.add(memb);
			} else {
				Member memb = tmpMemory.get(id);
				memb.addHtmlMessageEntry(entry);
			}
		}
	}

	@Override
	public String getLogTableName() {
		return null;
	}
}
