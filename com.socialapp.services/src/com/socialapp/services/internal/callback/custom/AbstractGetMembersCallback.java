package com.socialapp.services.internal.callback.custom;

import java.util.Collections;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.socialapp.services.IResultProcessor;
import com.socialapp.services.dao.Member;
import com.socialapp.services.internal.callback.AQuery;
import com.socialapp.services.internal.callback.AjaxStatus;
import com.socialapp.services.internal.callback.custom.sharedstate.LoginState;
import com.socialapp.services.util.SocialappFeature;
import com.socialapp.services.util.PerfLog.PerfMeasureStep;


abstract public class AbstractGetMembersCallback extends
		ProcessableCallback<List<Member>> {

	private long responseParsingTime;
	private long extractEntriesTime;
	private long waitingTimeForCallback;
	private long parsingResultIteratingTime;
	private long membersFinalizationTime;
	protected long beginFinalize;

	List<Element> entries = Collections.emptyList();
	List<Member> members = null;
	protected IResultProcessor<Boolean> updateOnlineStatusProcessor;

	protected AbstractGetMembersCallback(IResultProcessor<List<Member>> proc,
			IResultProcessor<Boolean> updateOnlineStatusProcessor) {
		super(proc);
		this.updateOnlineStatusProcessor = updateOnlineStatusProcessor;
	}

	@Override
	public void callback(String request, String response, AjaxStatus status) {
		try {
			this.requestString = request;
			this.responseString = response;
			this.status = status;

			waitingTimeForCallback = System.currentTimeMillis() - creationTime;
			entries = Collections.emptyList();
			members = null;
			if (response != null) {
				entries = parseEntries(response);
			}
			long beginIteration = System.currentTimeMillis();
			if (!entries.isEmpty()) {
				members = createMembersListFromEntries(entries);
			} else {
				System.err.println(SocialappFeature.WEB.name() +
						" cannot retrive entries from response!");
			}
			parsingResultIteratingTime = System.currentTimeMillis()
					- beginIteration;
			finalize(members, response);

			perfLog.log(PerfMeasureStep.waitingTimeForCallback,
					waitingTimeForCallback);
			perfLog.log(PerfMeasureStep.responseParsingTime,
					responseParsingTime);
			perfLog.log(PerfMeasureStep.extractEntriesTime, extractEntriesTime);
			perfLog.log(PerfMeasureStep.parsingResultIteratingTime,
					parsingResultIteratingTime);
			perfLog.log(PerfMeasureStep.membersFinalizationTime,
					membersFinalizationTime);
		} catch (Throwable e) {
			System.err.println(SocialappFeature.WEB.name() +
					" Error in Get Members Callback: ");
			e.printStackTrace();
		}
	}

	protected List<Element> parseEntries(String response) {
		long begin = System.currentTimeMillis();
		Document document = Jsoup.parse(response);
		responseParsingTime = System.currentTimeMillis() - begin;
		begin = System.currentTimeMillis();
		List<Element> entries = extractEntries(document);
		extractEntriesTime = System.currentTimeMillis() - begin;
		return entries;
	}

	abstract protected List<Element> extractEntries(Document response);

	abstract protected List<Member> createMembersListFromEntries(
			List<Element> entries);

	@Override
	public void finalize(List<Member> processable, Object... paras) {
		super.finalize(processable, paras);
		membersFinalizationTime = System.currentTimeMillis() - beginFinalize;
	}


	protected void updateOnlineStatus(final List<Member> result) {
		new Thread(new Runnable(){

			public void run() {
				try {
					internalUpdateOnlineStatus(result);
				} catch (RuntimeException ex) {
					System.err.println(SocialappFeature.WEB.toString() +
							" error while updating online status!");
					ex.printStackTrace();
				}
			}
			
		}).start();
	}

	protected void internalUpdateOnlineStatus(List<Member> result) {
		if (LoginState.isLoggedIn()) { // the online status is
														// available only in
														// logged in state
			UpdateLastOnlineTimestampCallback updateOnlineTimestampCallback = new UpdateLastOnlineTimestampCallback(result, this.updateOnlineStatusProcessor);
			AQuery aq = new AQuery();
			aq.ajax(updateOnlineTimestampCallback.getUrl(),
					updateOnlineTimestampCallback.getParams(), updateOnlineTimestampCallback);
		}
	}

}