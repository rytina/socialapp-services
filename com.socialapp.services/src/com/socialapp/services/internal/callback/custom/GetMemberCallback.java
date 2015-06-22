package com.socialapp.services.internal.callback.custom;

import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.socialapp.services.IResultProcessor;
import com.socialapp.services.dao.Member;
import com.socialapp.services.internal.callback.AjaxStatus;


public class GetMemberCallback extends ProcessableCallback<Member>{
	
	public GetMemberCallback(IResultProcessor<Member> proc) {
		super(proc);
	}

	@Override
	public void callback(String url, String response, AjaxStatus status) {
		super.callback(url, response, status);
		Document document = Jsoup.parse(response);
		Member member = new Member();
		member.setAbout(getAbout(document));
	    member.setHobbies(getHobbies(document));
	    member.setDesires(getDesires(document));
	    member.setInterests(getInterests(document));
		finalize(member);
	}

	private String getInterests(Document document) {
		StringBuilder sb = new StringBuilder();
		Iterator<Element> iter = document.getElementsByClass("interests").select("li").iterator();
		while(iter.hasNext()){
			Element next = iter.next();
			String text = StringUtils.trim(next.text());
			if(!StringUtils.isEmpty(text)){
				if(!sb.toString().equals("")){
					sb.append(", ");
				}
				sb.append(text);
			}
		}
		return sb.toString();
	}

	private String getDesires(Document document) {
		Element elem = getElement(document, "Meine Wünsche:");
		if(elem !=null){
			return elem.text();
		}else{
			return "";
		}
	}

	private String getHobbies(Document document) {
		Element elem = getElement(document, "Hobbies & Interessen:");
		if(elem !=null){
			return elem.text();
		}else{
			return "";
		}
	}

	private String getAbout(Document document) {
		Element elem = getElement(document, "Über mich:");
		if(elem !=null){
			return elem.text();
		}else{
			return "";
		}
	}

	private Element getElement(Document document, String header) {
		try{
			return document.select(":containsOwn("+header+")").first().parent().parent().nextElementSibling();
		}catch(Throwable e){
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean shouldLog() {
		return false;
	}

	@Override
	public String getActivityTableName() {
		return null;
	}

}
