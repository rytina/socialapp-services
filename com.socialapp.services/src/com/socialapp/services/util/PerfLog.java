package com.socialapp.services.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class PerfLog {
	public static final PerfLog NULL = new PerfLog();
	
	public enum PerfMeasureStep{
		waitingTimeForCallback,
		responseParsingTime,
		extractEntriesTime,
		parsingResultIteratingTime,
		membersFinalizationTime,
		overallCallbackTime,
		loadIntoCacheTime
	}
	
	// linked map, because the order is relevant: step 1, step 2,...
	private Map<String, Long> log = new LinkedHashMap<String, Long>();

	@Override
	public String toString() {
		return "PerfLog [" + (log != null ? "log=" + log : "") + "]";
	}
	
	
	public void log(PerfMeasureStep step, long stepTook){
		log.put(step.name(), stepTook);
	}

}
