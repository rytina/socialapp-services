package com.socialapp.services.internal.callback.custom;

import static com.socialapp.services.internal.util.UrlConstants.PAGEOFFSET_TOKEN;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.socialapp.services.IResultProcessor;
import com.socialapp.services.dao.Member;
import com.socialapp.services.internal.callback.custom.sharedstate.LoginState;
import com.socialapp.services.internal.parser.PartnerappParser;
import com.socialapp.services.persistence.IDataSource;
import com.socialapp.services.util.SocialappServiceConstants;

public class GetMembersCallbackForSearch extends AbstractGetMembersCallback {

	private final IDataSource datasource;

	private String gender;

	private int interest;

	public GetMembersCallbackForSearch(IResultProcessor<List<Member>> proc, IResultProcessor<Boolean> updateOnlineStatusProcessor,
			IDataSource datasource, String gender,
			int interest) {
		super(proc, updateOnlineStatusProcessor);
		this.datasource = datasource;
		this.gender = gender;
		this.interest = interest;
	}

	public void finalize(List<Member> processable, Object... paras) {
		if (paras != null && paras.length > 0) {
			String response = paras[0].toString();
			beginFinalize = System.currentTimeMillis();
			int currentPageNumber = 0;
			int numberOfPages = 0;
			if (response != null) {
				currentPageNumber = getCurrentPageNumber(response);
				numberOfPages = getNumberOfPages(response);
			}
			super.finalize(members, currentPageNumber, numberOfPages);
		}
		datasource.close();
	};

	private int getNumberOfPages(String response) {
		int numberOfPages = StringUtils
				.countMatches(response, PAGEOFFSET_TOKEN);
		int currentPage = getCurrentPageNumber(response);
		if (currentPage > 1 && currentPage < numberOfPages) {
			numberOfPages--;
		}
		return numberOfPages;
	}

	private int getCurrentPageNumber(String response) {
		int currentPageNumber = 0;
		Matcher matcher = SocialappServiceConstants.ACTIVEPAGE_PATTERN
				.matcher(response);
		if (matcher.find()) {
			String activePage = matcher.group(1);
			currentPageNumber = Integer.parseInt(activePage);
		}
		return currentPageNumber;
	}

	@Override
	public boolean shouldLog() {
		return true;
	}

	@Override
	protected List<Element> extractEntries(Document response) {
		List<Element> entries = new ArrayList<Element>();
		if (LoginState.isLoggedIn()
				|| interest == 0) {
			entries = response.getElementsByTag("tr");
		} else {
			entries = response.getElementsByClass("member");
		}
		return entries;
	}

	@Override
	protected List<Member> createMembersListFromEntries(List<Element> entries) {
		List<Member> result;
		PartnerappParser parser = new PartnerappParser(datasource);
		result = parser.createMembersListFromEntries(entries, interest, gender);
		updateOnlineStatus(result);
		return result;
	}
	
	@Override
	public String getLogTableName() {
		return "searches";
	}


}