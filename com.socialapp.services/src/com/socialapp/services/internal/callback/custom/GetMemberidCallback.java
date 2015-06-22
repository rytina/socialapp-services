package com.socialapp.services.internal.callback.custom;

import org.apache.commons.lang3.StringUtils;

import com.socialapp.services.IResultProcessor;
import com.socialapp.services.dao.Member;
import com.socialapp.services.internal.callback.AjaxStatus;
import com.socialapp.services.util.Assert;
import com.socialapp.services.util.PartnerAppFeature;
import com.socialapp.services.util.ServerUtils;



public class GetMemberidCallback extends AbstractGetMemberidCallback {

	private Member member;

	public GetMemberidCallback(IResultProcessor<String> proc, Member memberWithoutID) {
		super(proc, memberWithoutID.getName(),
				memberWithoutID.getInterestsIdArray(),
				memberWithoutID.getZip());
		Assert.isTrue(StringUtils.isEmpty(memberWithoutID.getId()), "there is no need to make a ajax call for the id, because it is already known!");
		member = memberWithoutID;
	}

	@Override
	public void callback(String url, String memberID, AjaxStatus status) {
		member.setId(memberID);
		try {
			ServerUtils.updateUserDataOnServer(null,
					Integer.valueOf(memberID),
					Integer.valueOf(member.getImageID()), null);
		} catch (RuntimeException e) {
			System.err.println(PartnerAppFeature.WEB.toString() +
					" update user data on server failed!");
			e.printStackTrace();
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