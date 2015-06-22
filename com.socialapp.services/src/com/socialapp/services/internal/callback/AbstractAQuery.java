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

import java.lang.reflect.Constructor;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;

import com.socialapp.services.internal.callback.custom.ProcessableCallback;
import com.socialapp.services.internal.callback.util.Constants;


/**
 * The core class of AQuery. Contains all the methods available from an AQuery object.
 *
 * @param <T> the generic type
 */
public abstract class AbstractAQuery<T extends AbstractAQuery<T>> implements Constants {

	
	private Transformer trans;
	private int policy = Constants.CACHE_DEFAULT;
	//private Integer policy = null;
	private HttpHost proxy;

	@SuppressWarnings("unchecked")
	protected T create(){
		
		AbstractAQuery<?> result = null;
		
		try{
			Constructor<T> c = getConstructor();
			result = c.newInstance();
		}catch(Exception e){
			//should never happen
			e.printStackTrace();
		}
		return (T) result;
		
	}
	
	
	private Constructor<T> constructor;
	@SuppressWarnings("unchecked")
	private Constructor<T> getConstructor(){
		
		if(constructor == null){
		
			try{
				constructor = (Constructor<T>) getClass().getConstructor();
			}catch(Exception e){
				//should never happen
				e.printStackTrace();
			}
		}
		
		return constructor;
	}
	
	
	@SuppressWarnings("unchecked")
	protected T self(){
		return (T) this;
	}

	
	/**
	 * Apply the transformer to convert raw data to desired object type for the next ajax request. 
	 *
	 * @param transformer transformer
	 * @return self
	 */
	public T transformer(Transformer transformer){
		trans = transformer;
		return self();
	}	
	
	public T policy(int cachePolicy){
		policy = cachePolicy;
		return self();
	}	
	
	/**
	 * Apply the proxy info to next ajax request. 
	 *
	 * @param transformer transformer
	 * @return self
	 */
	public T proxy(String host, int port){
		proxy = new HttpHost(host, port);
		return self();
	}	
	
	
	
	/**
	 * Advanced Ajax callback. User must manually prepare the callback object settings (url, type, etc...) by using its methods.
	 *
	 * @param callback callback handler
	 * @return self
	 * 
	 * @see testAjax1
	 */
	
	public <K> T ajax(ProcessableCallback<?> callback){
		return invoke(callback);
	}
	
	
	
	protected <K> T invoke(ProcessableCallback<?> cb){
				
		if(trans != null){
			cb.transformer(trans);
		}
		
		//if(policy != null){
			cb.policy(policy);
		//}
		
		if(proxy != null){
			cb.proxy(proxy.getHostName(), proxy.getPort());
		}
		
		cb.internalAsync();
		
		reset();
		
		return self();
	}	
	
	protected void reset(){
		
		trans = null;
		policy = CACHE_DEFAULT;
		proxy = null;
		
		
	}
	
	
	/**
	 * Ajax call with various callback data types.
	 *
	 * @param url url
	 * @param type data type
	 * @param callback callback handler
	 * @return self
	 * 
	 */
	
	public <K> T ajax(String url, ProcessableCallback<?> callback){
		callback.url(url);
		return ajax(callback);
	}
	
	/**
	 * Ajax call with various callback data types with file caching.
	 * 
	 * The expire param is the duration to consider cached data expired (if hit).
	 * For example, expire = 15 * 60 * 1000 means if the cache data is within 15 minutes old, 
	 * return cached data immediately, otherwise go fetch the source again.
	 * 
	 *
	 * @param url url
	 * @param type data type
	 * @param expire duration in millseconds, 0 = always use cache
	 * @param callback callback handler
	 * @return self
	 * 
	 * 
	 */
	
	public <K> T ajax(String url, long expire, ProcessableCallback<?> callback){
		
		callback.url(url).fileCache(true).expire(expire);
		
		return ajax(callback);
	}
	
	
	
	
	
	
	/**
	 * Ajax call with POST method.
	 *
	 * The handler signature must be (String url, <K> object, AjaxStatus status)
	 *
	 * @param url url
	 * @param params 
	 * @param type data type
	 * @param callback callback method name
	 * @return self
	 * 
	 * 
	 */
	
	public <K> T ajax(String url, Map<String, ?> params, ProcessableCallback<?> callback){
		
		callback.url(url).params(params);
		return ajax(callback);
	}
	
	
	/**
	 * Ajax HTTP delete.
	 *
	 * @param url url
	 * @param type data type
	 * @param callback callback handler
	 * @return self
	 * 
	 */
	
	public T delete(String url, ProcessableCallback<?> callback){
		
		callback.url(url).method(AQuery.METHOD_DELETE);		
		return ajax(callback);
		
	}
	
	/**
	 * Ajax HTTP put.
	 *
	 * @param url url
	 * @param contentHeader Content-Type header
	 * @param type reponse type
	 * @param callback callback
	 * @return self
	 * 
	 */
	
	public <K> T put(String url, String contentHeader, HttpEntity entity, ProcessableCallback<?> callback){
		
		callback.url(url).method(AQuery.METHOD_PUT).header("Content-Type", contentHeader).param(AQuery.POST_ENTITY, entity);		
		return ajax(callback);
		
	}
	
	public <K> T post(String url, String contentHeader, HttpEntity entity, ProcessableCallback<?> callback){
        
        callback.url(url).method(AQuery.METHOD_POST).header("Content-Type", contentHeader).param(AQuery.POST_ENTITY, entity);     
        return ajax(callback);
        
    }
	
	
	/**
	 * Ajax call with that block until response is ready. This method cannot be called on UI thread.
	 * 
	 *
	 * @param callback callback 
	 * @return self
	 * 
	 */
	
	public T sync(ProcessableCallback<?> callback){
		ajax(callback);
		callback.block();
		return self();
	}
	
	
	/**
	 * Stop all ajax activities. Should only be called when app exits.
	 *
	 * 
	 * @return self
	 */
	public T ajaxCancel(){
		
		AbstractAjaxCallback.cancel();
		
		return self();
	}
	
	
}
