package com.socialapp.services.internal.callback.custom;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.socialapp.services.IResultProcessor;
import com.socialapp.services.UIResultProcessor;
import com.socialapp.services.internal.callback.AQuery;
import com.socialapp.services.internal.callback.AbstractAjaxCallback;
import com.socialapp.services.internal.callback.AjaxStatus;
import com.socialapp.services.internal.util.ExternalUrlConstants;
import com.socialapp.services.util.Assert;
import com.socialapp.services.util.PerfLog;
import com.socialapp.services.util.SocialappServiceConstants;


public abstract class ProcessableCallback<T> extends AbstractAjaxCallback implements PartnerappCallback{

	protected final IResultProcessor<T> proc;
	private Map<String, String> parameter;
	public final PerfLog perfLog = new PerfLog();
	
	protected String requestString;
	protected String responseString;
	protected AjaxStatus status;
	
	protected long creationTime;

	public ProcessableCallback(IResultProcessor<T> proc) {
		creationTime = System.currentTimeMillis();
		this.proc = proc;
	}

	public void finalize(T processable, Object ...paras) {
		if (proc != null) {
			if(proc instanceof UIResultProcessor){
				((UIResultProcessor<?>) proc).getUiExecuter().execute(proc,processable, paras);
			}else{
				proc.process(processable, paras);
			}
		}
	}

	public abstract boolean shouldLog();
	
	public abstract String getLogTableName();

	public void log(String request, String response, AjaxStatus status) {
		Assert.isNotNull(parameter,
				"because logging is active, you need to invoke setParameter() on the callback!");
		String logmsg = "ProcessableCallback.log() request=> " + request + " "
				+ parameter.toString() + "\nresponse=> " + (response != null ? StringUtils.substring(response, 0, 20000) : "response is null=server problem!")
				+ "\" partnerapp-version=> " + SocialappServiceConstants.SOCIAL_APP_VERSION
				+ "\" ajax-status=> " + toReadableStatusString(status)
				+ "\" perfLog=> " + perfLog.toString();
		LogMessageCallback cb = new LogMessageCallback(null);
		HashMap<String, String> params = new HashMap<String,String>();
		params.put("activity", getLogTableName());
		params.put("logmsg", logmsg);
		AQuery aq = new AQuery();
		aq.ajax(ExternalUrlConstants.LOGGING_URL, params, cb);
	}
	


	private String toReadableStatusString(AjaxStatus status) {
		StringBuilder sb = new StringBuilder();
		sb.append("code=\"");
		sb.append(status.getCode());
		sb.append("\"");
		sb.append("\n");
		sb.append("duration=\"");
		sb.append(status.getDuration());
		sb.append("\"");
		sb.append("\n");
		sb.append("error=\"");
		sb.append(status.getError());
		sb.append("\"");
		sb.append("\n");
		sb.append("message=\"");
		sb.append(status.getMessage());
		sb.append("\"");
		sb.append("\n");
		sb.append("time=\"");
		sb.append(status.getTime().toGMTString());
		sb.append("\"");
		sb.append("\n");
		sb.append("redirect=\"");
		sb.append(status.getRedirect());
		sb.append("\"");
		sb.append("\n");
		sb.append("source=\"");
		sb.append(status.getSource());
		sb.append("\"");
		sb.append("\n");
		sb.append("cookies=\"");
		sb.append(status.getCookies().toString());
		sb.append("\"");
		sb.append("\n");
		sb.append("headers=\"");
		sb.append(status.getHeaders().toString());
		sb.append("\"");
		sb.append("\n");
		return sb.toString();
	}
	
	@Override
	public void callback(final String request, final String response,final AjaxStatus status) {
		if (shouldLog()) {
			asyncLog(request, response, status);
		}
	}
	
	
	/**
	 * can be used to log status for the callback from outside 
	 */
	public void asyncLog() {
		asyncLog(requestString, responseString, status);
	}

	private void asyncLog(final String request,final String response,final AjaxStatus status) {
		Assert.isNotNull(request,
				"for logging you need to initialize the variable ProcessableCallback.requestString in the callback method!");
		Assert.isNotNull(response,
				"for logging you need to initialize the variable ProcessableCallback.responseString in the callback method!");
		Assert.isNotNull(status,
				"for logging you need to initialize the variable ProcessableCallback.status in the callback method!");
		if(response == null){
			System.err.println("Ein Fehler beim Server ist aufgetreten. Bitte später erneut versuchen");
		}
		try {
				new Thread(new Runnable(){
					public void run() {
						log(request, response, status);
					}
				}).start();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void setParameter(Map<String, String> params) {
		this.parameter = params;
	}
	
	public long getCreationTime() {
		return creationTime;
	}

	public static ProcessableCallback<String> create() {
		return new ProcessableCallback<String>(null) {

			@Override
			public boolean shouldLog() {
				return false;
			}

			@Override
			public String getLogTableName() {
				return null;
			}
		};
	}
}
