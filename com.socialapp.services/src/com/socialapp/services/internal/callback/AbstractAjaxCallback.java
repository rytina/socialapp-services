/*
 * Copyright 2011 - AndroidQuery.com (tinyeeliu@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.socialapp.services.internal.callback;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import com.socialapp.services.internal.callback.util.AQUtility;
import com.socialapp.services.internal.callback.util.Constants;
import com.socialapp.services.internal.callback.util.PredefinedBAOS;

/**
 * The core class of ajax callback handler.
 * 
 */
public abstract class AbstractAjaxCallback implements Runnable {

	private static int NET_TIMEOUT = 30000;
	private static String AGENT = null;
	private static int NETWORK_POOL = 4;
	private static boolean GZIP = true;
	private static boolean REUSE_CLIENT = true;
	private static boolean SIMULATE_ERROR = false;

	private Reference<Object> whandler;
	private Object handler;
	private String callback;

	private String url;
	private String networkUrl;
	protected Map<String, String> params;
	protected Map<String, String> headers;
	protected Map<String, String> cookies;

	protected String result;

	private int policy = Constants.CACHE_DEFAULT;

	protected AjaxStatus status;

	protected boolean fileCache;
	protected boolean memCache;
	private boolean refresh;
	private int timeout = 0;
	private boolean redirect = true;

	private long expire;
	private String encoding = "UTF-8";

	private int method = Constants.METHOD_DETECT;
	private HttpUriRequest request;

	private int retry = 0;

	@SuppressWarnings("unchecked")
	private AbstractAjaxCallback self() {
		return this;
	}

	private void clear() {
		whandler = null;
		handler = null;
		request = null;
	}

	/**
	 * Sets the timeout.
	 * 
	 * @param timeout
	 *            the default network timeout in milliseconds
	 */
	public static void setTimeout(int timeout) {
		NET_TIMEOUT = timeout;
	}

	/**
	 * Sets the agent.
	 * 
	 * @param agent
	 *            the default agent sent in http header
	 */
	public static void setAgent(String agent) {
		AGENT = agent;
	}

	/**
	 * Use gzip.
	 * 
	 * @param gzip
	 */
	public static void setGZip(boolean gzip) {
		GZIP = gzip;
	}

	/**
	 * Set to true to simulate network error.
	 * 
	 * @param error
	 */
	public static void setSimulateError(boolean error) {
		SIMULATE_ERROR = error;
	}


	/**
	 * Set a callback handler with a weak reference. Use weak handler if you do
	 * not want the ajax callback to hold the handler object from garbage
	 * collection. For example, if the handler is an activity, weakHandler
	 * should be used since the method shouldn't be invoked if an activity is
	 * already dead and garbage collected.
	 * 
	 * @param handler
	 *            the handler
	 * @param callback
	 *            the callback
	 * @return self
	 */
	public AbstractAjaxCallback weakHandler(Object handler, String callback) {
		this.whandler = new WeakReference<Object>(handler);
		this.callback = callback;
		this.handler = null;
		return self();
	}

	/**
	 * Set a callback handler. See weakHandler for handler objects, such as
	 * Activity, that should not be held from garbaged collected.
	 * 
	 * @param handler
	 *            the handler
	 * @param callback
	 *            the callback
	 * @return self
	 */
	public AbstractAjaxCallback handler(Object handler, String callback) {
		this.handler = handler;
		this.callback = callback;
		this.whandler = null;
		return self();
	}

	/**
	 * Url.
	 * 
	 * @param url
	 *            the url
	 * @return self
	 */
	public AbstractAjaxCallback url(String url) {
		this.url = url;
		return self();
	}

	public AbstractAjaxCallback networkUrl(String url) {
		this.networkUrl = url;
		return self();
	}

	public AbstractAjaxCallback method(int method) {
		this.method = method;
		return self();
	}

	public AbstractAjaxCallback timeout(int timeout) {
		this.timeout = timeout;
		return self();
	}

	/**
	 * Set if http requests should follow redirect. Default is true.
	 * 
	 * @param redirect
	 *            follow redirect
	 * @return self
	 */

	public AbstractAjaxCallback redirect(boolean redirect) {
		this.redirect = redirect;
		return self();
	}

	public AbstractAjaxCallback retry(int retry) {
		this.retry = retry;
		return self();
	}


	/**
	 * Set ajax request to be file cached.
	 * 
	 * @param cache
	 *            the cache
	 * @return self
	 */
	public AbstractAjaxCallback fileCache(boolean cache) {
		this.fileCache = cache;
		return self();
	}

	/**
	 * Indicate ajax request to be memcached. Note: The default ajax handler
	 * does not supply a memcache. Subclasses such as BitmapAjaxCallback can
	 * provide their own memcache.
	 * 
	 * @param cache
	 *            the cache
	 * @return self
	 */
	public AbstractAjaxCallback memCache(boolean cache) {
		this.memCache = cache;
		return self();
	}

	public AbstractAjaxCallback policy(int policy) {
		this.policy = policy;
		return self();
	}

	/**
	 * Indicate the ajax request should ignore memcache and filecache.
	 * 
	 * @param refresh
	 *            the refresh
	 * @return self
	 */
	public AbstractAjaxCallback refresh(boolean refresh) {
		this.refresh = refresh;
		return self();
	}

	/**
	 * The expire duation for filecache. If a cached copy will be served if a
	 * cached file exists within current time minus expire duration.
	 * 
	 * @param expire
	 *            the expire
	 * @return self
	 */
	public AbstractAjaxCallback expire(long expire) {
		this.expire = expire;
		return self();
	}

	/**
	 * Set the header fields for the http request.
	 * 
	 * @param name
	 *            the name
	 * @param value
	 *            the value
	 * @return self
	 */
	public AbstractAjaxCallback header(String name, String value) {
		if (headers == null) {
			headers = new HashMap<String, String>();
		}
		headers.put(name, value);
		return self();
	}

	/**
	 * Set the header fields for the http request.
	 * 
	 * @param headers
	 *            the header
	 * @return self
	 */

	public AbstractAjaxCallback headers(Map<String, String> headers) {
		this.headers = (Map<String, String>) headers;
		return self();
	}

	/**
	 * Set the cookies for the http request.
	 * 
	 * @param name
	 *            the name
	 * @param value
	 *            the value
	 * @return self
	 */
	public AbstractAjaxCallback cookie(String name, String value) {
		if (cookies == null) {
			cookies = new HashMap<String, String>();
		}
		cookies.put(name, value);
		return self();
	}

	/**
	 * Set cookies for the http request.
	 * 
	 * @param cookies
	 *            the cookies
	 * @return self
	 */

	public AbstractAjaxCallback cookies(Map<String, String> cookies) {
		this.cookies = (Map<String, String>) cookies;
		return self();
	}

	/**
	 * Set the encoding used to parse the response.
	 * 
	 * Default is UTF-8.
	 * 
	 * @param encoding
	 * @return self
	 */
	public AbstractAjaxCallback encoding(String encoding) {
		this.encoding = encoding;
		return self();
	}

	private HttpHost proxy;

	public AbstractAjaxCallback proxy(String host, int port) {
		proxy = new HttpHost(host, port);
		return self();
	}

	public AbstractAjaxCallback proxy(String host, int port, String user,
			String password) {

		proxy(host, port);

		String authHeader = makeAuthHeader(user, password);

		AQUtility.debug("proxy auth", authHeader);

		return header("Proxy-Authorization", authHeader);

	}

	private static String makeAuthHeader(String username, String password) {

		String cred = username + ":" + password;
		byte[] data = cred.getBytes();

		String auth = "Basic "
				+ new String(AQUtility.encode64(data, 0, data.length));

		return auth;

	}

	/**
	 * Set http POST params. If params are set, http POST method will be used.
	 * The UTF-8 encoded value.toString() will be sent with POST.
	 * 
	 * Header field
	 * "Content-Type: application/x-www-form-urlencoded;charset=UTF-8" will be
	 * added if no Content-Type header field presents.
	 * 
	 * @param name
	 *            the name
	 * @param value
	 *            the value
	 * @return self
	 */
	public AbstractAjaxCallback param(String name, String value) {
		if (params == null) {
			params = new HashMap<String, String>();
		}
		params.put(name, value);
		return self();
	}

	/**
	 * Set the http POST params. See param(String name, Object value).
	 * 
	 * @param params
	 *            the params
	 * @return self
	 */

	@SuppressWarnings("unchecked")
	public AbstractAjaxCallback params(Map<String, String> params) {
		this.params = (Map<String, String>) params;
		return self();
	}

	private static final Class<?>[] DEFAULT_SIG = { String.class, Object.class,
			AjaxStatus.class };

	private boolean completed;

	void callback() {
		completed = true;

		if (callback != null) {
			Object handler = getHandler();
			Class<?>[] AJAX_SIG = { String.class, String.class,
					AjaxStatus.class };
			AQUtility.invokeHandler(handler, callback, true, true, AJAX_SIG,
					DEFAULT_SIG, url, result, status);
		} else {
			try {
				callback(url, result, status);
			} catch (Exception e) {
				AQUtility.report(e);
			}
		}

		if (!blocked) {
			status.close();
		}

		wake();
		AQUtility.debugNotify();
	}

	private void wake() {

		if (!blocked)
			return;

		synchronized (this) {
			try {
				notifyAll();
			} catch (Exception e) {
			}
		}

	}

	private boolean blocked;

	/**
	 * Block the current thread until the ajax call is completed. Returns
	 * immediately if ajax is already completed. Exception will be thrown if
	 * this method is called in main thread.
	 * 
	 */

	public void block() {
		if (completed)
			return;

		try {
			synchronized (this) {
				blocked = true;
				// wait at most the network timeout plus 5 seconds, this
				// guarantee thread will never be blocked forever
				this.wait(NET_TIMEOUT + 5000);
			}
		} catch (Exception e) {
		}

	}

	/**
	 * The callback method to be overwritten for subclasses.
	 * 
	 * @param url
	 *            the url
	 * @param object
	 *            the object
	 * @param status
	 *            the status
	 */
	public void callback(String url, String object, AjaxStatus status) {

	}

	protected void skip(String url, String object, AjaxStatus status) {

	}

	protected String datastoreGet(String url) {

		return null;

	}

	@SuppressWarnings("unchecked")
	protected String transform(String url, byte[] data, AjaxStatus status) {

		String result = null;

		if (status.getSource() == AjaxStatus.NETWORK) {
			AQUtility.debug("network");
			result = correctEncoding(data, encoding, status);
		} else {
			AQUtility.debug("file");
			try {
				result = new String(data, encoding);
			} catch (Exception e) {
				AQUtility.debug(e);
			}
		}

		return result;
	}

	// This is an adhoc way to get charset without html parsing library, might
	// not cover all cases.
	private String getCharset(String html) {

		String pattern = "<meta [^>]*http-equiv[^>]*\"Content-Type\"[^>]*>";

		Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(html);

		if (!m.find())
			return null;

		String tag = m.group();

		return parseCharset(tag);
	}

	private String parseCharset(String tag) {

		if (tag == null)
			return null;
		int i = tag.indexOf("charset");
		if (i == -1)
			return null;

		int e = tag.indexOf(";", i);
		if (e == -1)
			e = tag.length();

		String charset = tag.substring(i + 7, e).replaceAll("[^\\w-]", "");
		return charset;
	}

	private String correctEncoding(byte[] data, String target, AjaxStatus status) {

		String result = null;

		try {
			if (!"utf-8".equalsIgnoreCase(target)) {
				return new String(data, target);
			}

			String header = parseCharset(status.getHeader("Content-Type"));
			AQUtility.debug("parsing header", header);
			if (header != null) {
				return new String(data, header);
			}

			result = new String(data, "utf-8");

			String charset = getCharset(result);

			AQUtility.debug("parsing needed", charset);

			if (charset != null && !"utf-8".equalsIgnoreCase(charset)) {
				AQUtility.debug("correction needed", charset);
				result = new String(data, charset);
				status.data(result.getBytes("utf-8"));
			}

		} catch (Exception e) {
			AQUtility.report(e);
		}

		return result;

	}

	protected String memGet(String url) {
		return null;
	}

	protected void memPut(String url, String object) {
	}

	protected File accessFile(File cacheDir, String url) {

		if (expire < 0)
			return null;

		File file = AQUtility.getExistedCacheByUrl(cacheDir, url);

		if (file != null && expire != 0) {
			long diff = System.currentTimeMillis() - file.lastModified();
			if (diff > expire) {
				return null;
			}
		}

		return file;
	}

	/**
	 * Starts the async process.
	 * 
	 * If activity is passed, the callback method will not be invoked if the
	 * activity is no longer in use. Specifically, isFinishing() is called to
	 * determine if the activity is active.
	 * 
	 * @param act
	 *            activity
	 */
	public void async() {
		internalAsync();
	}

	/**
	 * Starts the async process.
	 * 
	 * @param context
	 *            the context
	 */
	public void internalAsync() {

		if (status == null) {
			status = new AjaxStatus();
			status.redirect(url).refresh(refresh);
		} else if (status.getDone()) {
			status.reset();
			result = null;
		}

		work();
	}

	public void failure(int code, String message) {

		if (status != null) {
			status.code(code).message(message).done();

			afterWork();

			// callback();
		}

	}

	private void work() {

		String object = memGet(url);

		if (object != null) {
			result = object;
			status.source(AjaxStatus.MEMORY).done();
			callback();
		} else {
			execute(this);
		}
	}

	/**
	 * AQuert internal use. Do not call this method directly.
	 */

	public void run() {

		if (!status.getDone()) {

			try {
				backgroundWork();
			} catch (Throwable e) {
				AQUtility.debug(e);
				status.code(AjaxStatus.NETWORK_ERROR).done();
			}

			if (!status.getReauth()) {
				// if doesn't need to reauth
				afterWork();
			}
		} else {
			afterWork();
		}

	}

	private void backgroundWork() {

		if (result == null) {
			datastoreWork();
		}

		if (result == null) {
			networkWork();
		}

	}

	private String getNetworkUrl(String url) {

		String result = url;

		if (networkUrl != null) {
			result = networkUrl;
		}

		return result;
	}

	private void datastoreWork() {

		result = datastoreGet(url);

		if (result != null) {
			status.source(AjaxStatus.DATASTORE).done();
		}
	}

	private void networkWork() {

		if (url == null) {
			status.code(AjaxStatus.NETWORK_ERROR).done();
			return;
		}

		byte[] data = null;

		try {

			network(retry + 1);

			data = status.getData();

		} catch (IOException e) {

			AQUtility.debug("IOException");

			// work around for IOException when 401 is returned
			// reference:
			// http://stackoverflow.com/questions/11735636/how-to-get-401-response-without-handling-it-using-try-catch-in-android
			String message = e.getMessage();
			if (message != null
					&& message.contains("No authentication challenges found")) {
				status.code(401).message(message);
			} else {
				status.code(AjaxStatus.NETWORK_ERROR).message("network error");
			}

		} catch (Exception e) {
			AQUtility.debug(e);
			status.code(AjaxStatus.NETWORK_ERROR).message("network error");
		}

		try {
			result = transform(url, data, status);
		} catch (Exception e) {
			AQUtility.debug(e);
		}

		if (result == null && data != null) {
			status.code(AjaxStatus.TRANSFORM_ERROR).message("transform error");
		}

		lastStatus = status.getCode();
		status.done();
	}

	// added retry logic
	private void network(int attempts) throws IOException {

		if (attempts <= 1) {
			network();
			return;
		}

		for (int i = 0; i < attempts; i++) {

			try {
				network();
				return;
			} catch (IOException e) {
				if (i == attempts - 1) {
					throw e;
				}
			}

		}

	}

	private void network() throws IOException {

		String url = this.url;
		Map<String, String> params = this.params;

		url = getNetworkUrl(url);

		if (Constants.METHOD_DELETE == method) {
			httpDelete(url, status);
		} else if (Constants.METHOD_PUT == method) {
			httpPut(url, params, status);
		} else {

			if (Constants.METHOD_POST == method && params == null) {
				params = new HashMap<String, String>();
			}

			if (params == null) {
				httpGet(url, status);
			} else {
					httpPost(url, params, status);
			}

		}

	}

	private void afterWork() {

		if (url != null && memCache) {
			memPut(url, result);
		}

		callback();
		clear();
	}

	private static ExecutorService fetchExe;

	public static void execute(Runnable job) {

		if (fetchExe == null) {
			fetchExe = Executors.newFixedThreadPool(NETWORK_POOL);
		}

		fetchExe.execute(job);
	}

	/**
	 * Return the number of active ajax threads. Note that this doesn't
	 * necessarily correspond to active network connections. Ajax threads might
	 * be reading a cached url from file system or transforming the response
	 * after a network transfer.
	 * 
	 */

	public static int getActiveCount() {

		int result = 0;

		if (fetchExe instanceof ThreadPoolExecutor) {
			result = ((ThreadPoolExecutor) fetchExe).getActiveCount();
		}

		return result;

	}

	/**
	 * Sets the simultaneous network threads limit. Highest limit is 25.
	 * 
	 * @param limit
	 *            the new network threads limit
	 */
	public static void setNetworkLimit(int limit) {

		NETWORK_POOL = Math.max(1, Math.min(25, limit));
		fetchExe = null;

		AQUtility.debug("setting network limit", NETWORK_POOL);
	}

	/**
	 * Cancel ALL ajax tasks.
	 * 
	 * Warning: Do not call this method unless you are exiting an application.
	 * 
	 */

	public static void cancel() {

		if (fetchExe != null) {
			fetchExe.shutdownNow();
			fetchExe = null;
		}
	}

	private static String patchUrl(String url) {

		url = url.replaceAll(" ", "%20").replaceAll("\\|", "%7C");
		return url;
	}

	private void httpGet(String url, AjaxStatus status) throws IOException {

		AQUtility.debug("get", url);
		url = patchUrl(url);

		HttpGet get = new HttpGet(url);

		httpDo(get, url, status);

	}

	private void httpDelete(String url, AjaxStatus status) throws IOException {

		AQUtility.debug("get", url);
		url = patchUrl(url);

		HttpDelete del = new HttpDelete(url);

		httpDo(del, url, status);

	}

	private void httpPost(String url, Map<String, String> params,
			AjaxStatus status) throws ClientProtocolException, IOException {

		AQUtility.debug("post", url);

		HttpEntityEnclosingRequestBase req = new HttpPost(url);

		httpEntity(url, req, params, status);

	}

	private void httpPut(String url, Map<String, String> params,
			AjaxStatus status) throws ClientProtocolException, IOException {

		AQUtility.debug("put", url);

		HttpEntityEnclosingRequestBase req = new HttpPut(url);

		httpEntity(url, req, params, status);

	}

	private void httpEntity(String url, HttpEntityEnclosingRequestBase req,
			Map<String, String> params, AjaxStatus status)
			throws ClientProtocolException, IOException {

		// This setting seems to improve post performance
		// http://stackoverflow.com/questions/3046424/http-post-requests-using-httpclient-take-2-seconds-why
		req.getParams().setBooleanParameter(
				CoreProtocolPNames.USE_EXPECT_CONTINUE, false);

		HttpEntity entity = null;

		String value = params.get(AQuery.POST_ENTITY);


			List<NameValuePair> pairs = new ArrayList<NameValuePair>();

			for (Map.Entry<String, String> e : params.entrySet()) {
				value = e.getValue();
				if (value != null) {
					pairs.add(new BasicNameValuePair(e.getKey(), value));
				}
			}

			entity = new UrlEncodedFormEntity(pairs, "UTF-8");


		if (headers != null && !headers.containsKey("Content-Type")) {
			headers.put("Content-Type",
					"application/x-www-form-urlencoded;charset=UTF-8");
		}

		req.setEntity(entity);
		httpDo(req, url, status);

	}

	private static SocketFactory ssf;

	/**
	 * Set the secure socket factory.
	 * 
	 * Could be used to work around SSL certificate not truested issue.
	 * 
	 * http://stackoverflow.com/questions/1217141/self-signed-ssl-acceptance-
	 * android
	 */

	public static void setSSF(SocketFactory sf) {
		ssf = sf;
		client = null;
	}

	public static void setReuseHttpClient(boolean reuse) {

		REUSE_CLIENT = reuse;
		client = null;

	}

	/*
	 * private static HttpHost phost; private static String pheader;
	 * 
	 * 
	 * public static void setProxy(String host, int port, String user, String
	 * password){
	 * 
	 * if(host == null || host.isEmpty()) phost = null; else phost = new
	 * HttpHost(host, port);
	 * 
	 * //String authHeader = makeAuthHeader(user, password); //return
	 * header("Proxy-Authorization", authHeader);
	 * 
	 * if(user == null || user.isEmpty()) pheader = null; else pheader =
	 * makeAuthHeader(user, password);
	 * 
	 * }
	 */

	private static DefaultHttpClient client;

	private static DefaultHttpClient getClient() {

		if (client == null || !REUSE_CLIENT) {

			AQUtility.debug("creating http client");

			HttpParams httpParams = new BasicHttpParams();

			// httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
			// HttpVersion.HTTP_1_1);

			HttpConnectionParams.setConnectionTimeout(httpParams, NET_TIMEOUT);
			HttpConnectionParams.setSoTimeout(httpParams, NET_TIMEOUT);

			// ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new
			// ConnPerRouteBean(NETWORK_POOL));
			ConnManagerParams.setMaxConnectionsPerRoute(httpParams,
					new ConnPerRouteBean(25));

			// Added this line to avoid issue at:
			// http://stackoverflow.com/questions/5358014/android-httpclient-oom-on-4g-lte-htc-thunderbolt
			HttpConnectionParams.setSocketBufferSize(httpParams, 8192);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			registry.register(new Scheme("https",
					ssf == null ? SSLSocketFactory.getSocketFactory() : ssf,
					443));

			ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(
					httpParams, registry);
			client = new DefaultHttpClient(cm, httpParams);

		}
		return client;
	}

	// helper method to support underscore subdomain
	private HttpResponse execute(HttpUriRequest hr, DefaultHttpClient client,
			HttpContext context) throws ClientProtocolException, IOException {

		HttpResponse response = null;

		if (hr.getURI().getAuthority().contains("_")) {
			URL urlObj = hr.getURI().toURL();
			HttpHost host;
			if (urlObj.getPort() == -1) {
				host = new HttpHost(urlObj.getHost(), 80, urlObj.getProtocol());
			} else {
				host = new HttpHost(urlObj.getHost(), urlObj.getPort(),
						urlObj.getProtocol());
			}
			response = client.execute(host, hr, context);
		} else {
			response = client.execute(hr, context);
		}

		return response;
	}

	private static ProxyHandle proxyHandle;

	public static void setProxyHandle(ProxyHandle handle) {
		proxyHandle = handle;
	}

	private void httpDo(HttpUriRequest hr, String url, AjaxStatus status)
			throws ClientProtocolException, IOException {
		
		DefaultHttpClient client = getClient();

		if (proxyHandle != null) {
			proxyHandle.applyProxy(this, hr, client);
		}

		if (AGENT != null) {
			hr.addHeader("User-Agent", AGENT);
		} else if (AGENT == null && GZIP) {
			hr.addHeader("User-Agent", "gzip");
		}

		if (headers != null) {
			for (String name : headers.keySet()) {
				hr.addHeader(name, headers.get(name));
			}

		}

		if (GZIP
				&& (headers == null || !headers.containsKey("Accept-Encoding"))) {
			hr.addHeader("Accept-Encoding", "gzip");
		}

		String cookie = makeCookie();
		if (cookie != null) {
			hr.addHeader("Cookie", cookie);
		}

		HttpParams hp = hr.getParams();

		if (proxy != null)
			hp.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

		if (timeout > 0) {
			hp.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout);
			hp.setParameter(CoreConnectionPNames.SO_TIMEOUT, timeout);
		}

		if (!redirect) {
			hp.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
		}

		HttpContext context = new BasicHttpContext();
		CookieStore cookieStore = new BasicCookieStore();
		context.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

		request = hr;

		if (abort) {
			throw new IOException("Aborted");
		}

		if (SIMULATE_ERROR) {
			throw new IOException("Simulated Error");
		}

		HttpResponse response = null;

		try {
			// response = client.execute(hr, context);
			response = execute(hr, client, context);
		} catch (HttpHostConnectException e) {

			// if proxy is used, automatically retry without proxy
			if (proxy != null) {
				AQUtility.debug("proxy failed, retrying without proxy");
				hp.setParameter(ConnRoutePNames.DEFAULT_PROXY, null);
				// response = client.execute(hr, context);
				response = execute(hr, client, context);
			} else {
				throw e;
			}
		}

		byte[] data = null;

		String redirect = url;

		int code = response.getStatusLine().getStatusCode();
		String message = response.getStatusLine().getReasonPhrase();
		String error = null;

		HttpEntity entity = response.getEntity();

		if (code < 200 || code >= 300) {

			InputStream is = null;

			try {

				if (entity != null) {

					is = entity.getContent();
					byte[] s = toData(getEncoding(entity), is);

					error = new String(s, "UTF-8");

					AQUtility.debug("error", error);

				}
			} catch (Exception e) {
				AQUtility.debug(e);
			} finally {
				AQUtility.close(is);
			}

		} else {

			HttpHost currentHost = (HttpHost) context
					.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
			HttpUriRequest currentReq = (HttpUriRequest) context
					.getAttribute(ExecutionContext.HTTP_REQUEST);
			redirect = currentHost.toURI() + currentReq.getURI();

			int size = Math.max(32,
					Math.min(1024 * 64, (int) entity.getContentLength()));

			OutputStream os = null;
			InputStream is = null;

			try {
		        os = new PredefinedBAOS(size);
		        
		        is = entity.getContent();
		        
		        boolean gzip = "gzip".equalsIgnoreCase(getEncoding(entity));
		        
				if(gzip){
					is = new GZIPInputStream(is);
				}
		        
				int contentLength = (int) entity.getContentLength();
				
				//AQUtility.debug("gzip response", entity.getContentEncoding());
				
		        copy(is, os, contentLength, null, null);
		        
		        //os.flush();
		        
		        data = ((PredefinedBAOS) os).toByteArray();

			} finally {
				AQUtility.close(is);
				AQUtility.close(os);
			}

		}

		AQUtility.debug("response", code);
		if (data != null) {
			AQUtility.debug(data.length, url);
		}

		status.code(code).message(message).error(error).redirect(redirect)
				.time(new Date()).data(data).client(client).context(context)
				.headers(response.getAllHeaders());

	}

	private String getEncoding(HttpEntity entity) {

		if (entity == null)
			return null;

		Header eheader = entity.getContentEncoding();
		if (eheader == null)
			return null;

		return eheader.getValue();

	}

	private void copy(InputStream is, OutputStream os, int max, File tempFile,
			File destFile) throws IOException {

		// if no file operation is involved
		if (destFile == null) {
			copy(is, os, max);
			return;
		}

		try {

			copy(is, os, max);
			is.close();
			os.close();

			// copy success, rename file to destination
			// AQUtility.time("rename");
			tempFile.renameTo(destFile);
			// AQUtility.timeEnd("rename", 0);
		} catch (IOException e) {

			AQUtility.debug("copy failed, deleting files");

			// copy is a failure, delete everything
			tempFile.delete();
			destFile.delete();

			AQUtility.close(is);
			AQUtility.close(os);

			throw e;
		}

	}


	private void copy(InputStream is, OutputStream os, int max)
			throws IOException {
		AQUtility.copy(is, os, max);
	}

	/**
	 * Gets the url.
	 * 
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Gets the handler.
	 * 
	 * @return the handler
	 */
	public Object getHandler() {
		if (handler != null)
			return handler;
		if (whandler == null)
			return null;
		return whandler.get();
	}

	/**
	 * Gets the callback method name.
	 * 
	 * @return the callback
	 */
	public String getCallback() {
		return callback;
	}

	private static int lastStatus = 200;

	protected static int getLastStatus() {
		return lastStatus;
	}

	/**
	 * Gets the result. Can be null if ajax is not completed or the ajax call
	 * failed. This method should only be used after the block() method.
	 * 
	 * @return the result
	 */
	public String getResult() {
		return result;
	}

	/**
	 * Gets the ajax status. This method should only be used after the block()
	 * method.
	 * 
	 * @return the status
	 */

	public AjaxStatus getStatus() {
		return status;
	}

	/**
	 * Gets the encoding. Default is UTF-8.
	 * 
	 * @return the encoding
	 */
	public String getEncoding() {
		return encoding;
	}

	private boolean abort;

	/**
	 * Abort the http request that will interrupt the network transfer. This
	 * method currently doesn't work with multi-part post.
	 * 
	 * If no network transfer is involved (eg. response is file cached), this
	 * method has no effect.
	 * 
	 */

	public void abort() {

		abort = true;

		if (request != null && !request.isAborted()) {
			request.abort();
		}

	}


	private byte[] toData(String encoding, InputStream is) throws IOException {

		boolean gzip = "gzip".equalsIgnoreCase(encoding);

		if (gzip) {
			is = new GZIPInputStream(is);
		}

		return AQUtility.toBytes(is);
	}


	private String makeCookie() {

		if (cookies == null || cookies.size() == 0)
			return null;

		Iterator<String> iter = cookies.keySet().iterator();

		StringBuilder sb = new StringBuilder();

		while (iter.hasNext()) {
			String key = iter.next();
			String value = cookies.get(key);
			sb.append(key);
			sb.append("=");
			sb.append(value);
			if (iter.hasNext()) {
				sb.append("; ");
			}
		}

		return sb.toString();

	}

}
