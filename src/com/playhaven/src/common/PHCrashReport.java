package com.playhaven.src.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;



/**
 * @author samstewart
 * Simple class which allows you to send a crash report to the server. After you 
 * catch an exception, simply init this class and send the report. This class
 * is meant to be dead simple with no performance overhead. You can disable crash reporting
 * all together via {@see PHConstants#etUseCrashReporting()}.
 * 
 * TODO: I'd like to transition the codebase towards googles naming conventions.
 * TODO: implement GZIP compression before sending to the server? How slow is it? We need benchmarks 
 */
public class PHCrashReport extends PHAPIRequest {
	
	private String tag;
	
	private Exception exception;
	
	public enum Urgency { // all lower case to match config string from json
		critical,
		high,
		medium,
		low,
		none
	};
	
	private Urgency level = Urgency.critical;
	
	private Date reportTime = new Date();
	
	////////////////////////////////////////////////
	///////////// Static Handlers //////////////////
	//TODO.....
	
	/////////// Constants //////////////////////////
	private final String DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";
			
	private final String POST_PAYLOAD_NAME = "payload"; // name of post variable containing report
	
	//TODO: collect more device parameters? Locale?
	private final String CRASH_REPORT_TEMPLATE = 
			"Crash Report [PHCrashReport]\n" +
			"Tag: %s\n" +
			"Platform: %s\n" +
			"Version: %s\n" +
			"Time: %s\n" +
			"Session: %s\n" + // TODO: implement session support on Android
			"Device: %s\n" +
			"Urgency: %s\n" +
			"Message: %s" +
			"Stack Trace:\n" + 
			"\n" +
			"%s";
	
	///////////////////////////////////////////////
	////////////// Various Constructors //////////
	public PHCrashReport() {
		//TODO: !!!!! We MUST FIND A WAY TO ACCESS THE CONTEXT FROM WHEREVER IN THE CODE. We cannot pass null
		super(null);
		// capture exact time of report
		this.reportTime = new Date(); // current time
		this.exception = null;
	}

	public PHCrashReport(Exception e, Urgency level) {
		this(); // call default constructor
		this.exception = e;
		this.tag = null;
	}
	
	public PHCrashReport(Exception e, String tag, Urgency level) {
		this(); // call default constructor
		this.exception = e;
		this.tag = null;
	}
	
	/////////////////////////////////////////////////
	//////////////// Convenience Creators ///////////
	public static PHCrashReport reportCrash(Exception e, String tag, Urgency level) {
		e.printStackTrace();
		return new PHCrashReport(e, tag, level);
	}
	
	public static PHCrashReport reportCrash(Exception e, Urgency level) {
		e.printStackTrace();
		return new PHCrashReport(e, level);
	}
	
	public PHCrashReport(PHAPIRequest.Delegate delegate) {
		super(null);
		throw new UnsupportedOperationException("PHCrashReport does not accept a delegate");
	}
	
	@Override
	public String baseURL() {
		return super.createAPIURL("/v3/publisher/crash/");
	}
	
	@Override
	public Hashtable<String, String> getAdditionalParams() {
		Hashtable<String, String> params = new Hashtable<String, String>();
		
		// both params make URL debugging easier even though we send with the report
		params.put("ts", Long.toString(System.currentTimeMillis()));
		params.put("urgency", level.toString());
		
		if (this.tag != null)
			params.put("tag", this.tag);
		
		return params;
	}
	
	@Override
	public PHAsyncRequest.RequestType getRequestType() {
		return PHAsyncRequest.RequestType.Post;
	}
	
	@Override
	public Hashtable<String, String> getPostParams() {
		Hashtable<String, String> params = new Hashtable<String, String>();
		params.put(POST_PAYLOAD_NAME, generateCrashReport());
		
		return params;
	}
	
	@Override
	public void send() {
		return; // TODO: we *never* want a crash report sent since it is an incomplete feature
		/*
		if (level.ordinal() < Urgency.valueOf(PHConfig.urgency_level.toLowerCase()).ordinal())
			return;
		
		// only send if we are listening to the proper level or reporting
		super.send();
		*/	
	}
	
	
	
	/** Actually generates the crash report.
	 * TODO: in the future, if this requires any overhead at all 
	 * we should move this into an AsyncTask
	 */
	private String generateCrashReport() {
		if (exception == null) return "(No Exception)";
		
		// make sure we have the full stack trace
		exception.fillInStackTrace();
		
		StringWriter sWriter = new StringWriter();
		PrintWriter pWriter = new PrintWriter(sWriter); // thanks Java!!
		
		exception.printStackTrace(pWriter);
		
		// formatted date
		SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
		
		return String.format(CRASH_REPORT_TEMPLATE,
								(tag != null ? tag : "(No Tag)"),
								"android",
								PHConfig.sdk_version,
								df.format(reportTime),
								"(No Session)",
								PHConfig.device_id,
								level.toString(),
								exception.getMessage(),
								sWriter.toString()); 	
	}
	
}
