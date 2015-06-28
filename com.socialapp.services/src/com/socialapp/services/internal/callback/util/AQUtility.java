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

package com.socialapp.services.internal.callback.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Utility methods. Warning: Methods might changed in future versions.
 *
 */

public class AQUtility {

	private static boolean debug = false;
	private static Object wait;
	
	public static void setDebug(boolean debug){
		AQUtility.debug = debug;
	}
	
	public static boolean isDebug(){
		return debug;
	}
	
	public static void debugWait(long time){
		
		if(!debug) return;
		
		if(wait == null) wait = new Object();
		
		synchronized(wait) {
			
			try {
				wait.wait(time);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	
	
	public static void debugNotify(){
		
		if(!debug || wait == null) return;
		
		synchronized(wait) {
			wait.notifyAll();			
		}
		
	}
	
	
	public static void debug(Object msg){
		if(debug){
			System.out.println("AQuery "+ msg + "");
		}
	}
	
	public static void warn(Object msg, Object msg2){
		System.out.println("AQuery " + msg + ":" + msg2);
	}
	
	public static void debug(Object msg, Object msg2){
		if(debug){
			System.out.println("AQuery "+ msg + ":" + msg2);
		}
	}
	
	public static void debug(Throwable e){
			e.printStackTrace();
	}
	
	public static void report(Throwable e){
		
		if(e == null) return;

		try{

			e.printStackTrace();
			
			if(eh != null){
				eh.uncaughtException(Thread.currentThread(), e);
			}
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
	}
	
	private static UncaughtExceptionHandler eh;
	public static void setExceptionHandler(UncaughtExceptionHandler handler){
		eh = handler;
	}
	
	private static Map<String, Long> times = new HashMap<String, Long>();
	public static void time(String tag){
		
		times.put(tag, System.currentTimeMillis());
		
	}
	
	public static long timeEnd(String tag, long threshold){
		
		
		Long old = times.get(tag);
		if(old == null) return 0;
		
		long now = System.currentTimeMillis();
		
		long diff = now - old;
		
		if(threshold == 0 || diff > threshold){
			debug(tag, diff);
		}
		
		return diff;
		
		
	}
	
	public static Object invokeHandler(Object handler, String callback, boolean fallback, boolean report, Class<?>[] cls, Object... params){
    	
		return invokeHandler(handler, callback, fallback, report, cls, null, params);
		
    }

	public static Object invokeHandler(Object handler, String callback, boolean fallback, boolean report, Class<?>[] cls, Class<?>[] cls2, Object... params){
		try {
			return invokeMethod(handler, callback, fallback, cls, cls2, params);
		} catch (Exception e) {
			if(report){
				AQUtility.report(e);
			}else{
				AQUtility.debug(e);
			}
			return null;
		}
	}
	
	
	
	
	private static Object invokeMethod(Object handler, String callback, boolean fallback, Class<?>[] cls, Class<?>[] cls2, Object... params) throws Exception{
		
		if(handler == null || callback == null) return null;
		
		Method method = null;
		
		try{   
			if(cls == null) cls = new Class[0];
			method = handler.getClass().getMethod(callback, cls);
			return method.invoke(handler, params);			
		}catch(NoSuchMethodException e){
			//AQUtility.debug(e.getMessage());
		}
		
		
		try{
			if(fallback){
			
				if(cls2 == null){
					method = handler.getClass().getMethod(callback);	
					return method.invoke(handler);
				}else{
					method = handler.getClass().getMethod(callback, cls2);
					return method.invoke(handler, params);	
				}
				
			}
		}catch(NoSuchMethodException e){
		}
		
		return null;
		
	}
	
	
	
	
	private static String getMD5Hex(String str){
		byte[] data = getMD5(str.getBytes());
		
		BigInteger bi = new BigInteger(data).abs();
	
		String result = bi.toString(36);
		return result;
	}
	
	
	private static byte[] getMD5(byte[] data){

		MessageDigest digest;
		try {
			digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(data);
		    byte[] hash = digest.digest();
		    return hash;
		} catch (NoSuchAlgorithmException e) {
			AQUtility.report(e);
		}
	    
		return null;

	}
	
    private static final int IO_BUFFER_SIZE = 1024 * 4;
    public static void copy(InputStream in, OutputStream out) throws IOException {
    	copy(in, out, 0);
    }
    
    public static boolean TEST_IO_EXCEPTION = false;
    
    public static void copy(InputStream in, OutputStream out, int max) throws IOException {
    	
    	
    	byte[] b = new byte[IO_BUFFER_SIZE];
        int read;
        int count = 0;
        
        while((read = in.read(b)) != -1){
            out.write(b, 0, read);
            
            count++;
            
            if(TEST_IO_EXCEPTION && count > 2){
                AQUtility.debug("simulating internet error");
                throw new IOException();
            }
        }
        
    	
    }
    
    public static byte[] toBytes(InputStream is){
    	
    	byte[] result = null;
    	
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	
    	try {
			copy(is, baos);			
			result = baos.toByteArray();
		} catch (IOException e){
			AQUtility.report(e);
		}
		
		close(is);
    	
 	
    	return result;
    	
    }

    public static void write(File file, byte[] data){
    	
	    try{
	    	if(!file.exists()){
	    		try{
	    			file.createNewFile();
	    		}catch(Exception e){
	    			AQUtility.debug("file create fail", file);
	    			AQUtility.report(e);
	    		}
	    	}
	    	
	    	FileOutputStream fos = new FileOutputStream(file);
	    	fos.write(data);
	    	fos.close();
    	}catch(Exception e){
    		AQUtility.report(e);
    	}
    	
    }
    
    public static void close(Closeable c){
    	try{
    		if(c != null){
    			c.close();
    		}
    	}catch(Exception e){   		
    	}
    }
   
	
	private static ScheduledExecutorService storeExe;
	private static ScheduledExecutorService getFileStoreExecutor(){
		
		if(storeExe == null){
			storeExe = Executors.newSingleThreadScheduledExecutor();
		}
		
		return storeExe;
	}
	
	
	private static File makeCacheFile(File dir, String name){
				
		File result = new File(dir, name);		
		return result;
	}
	
	private static String getCacheFileName(String url){
		
		String hash = getMD5Hex(url);
		return hash;
	}
	
	public static File getCacheFile(File dir, String url){
		if(url == null) return null;
		if(url.startsWith(File.separator)){
			return new File(url);
		}
		
		String name = getCacheFileName(url);
		File file = makeCacheFile(dir, name);
		return file;
	}
	
	public static File getExistedCacheByUrl(File dir, String url){
		
		File file = getCacheFile(dir, url);
		if(file == null || !file.exists() || file.length() == 0){
			return null;
		}
		return file;
	}
	
	public static File getExistedCacheByUrlSetAccess(File dir, String url){
		File file = getExistedCacheByUrl(dir, url);
		if(file != null){
			lastAccess(file);
		}
		return file;
	}
	
	private static void lastAccess(File file){
		long now = System.currentTimeMillis();		
		file.setLastModified(now);
	}
	
	public static void store(File file, byte[] data){
		
		try{
			
			if(file != null){			
				AQUtility.write(file, data);
			}
		}catch(Exception e){
			AQUtility.report(e);
		}
		
		
	}
	
	
	
	private static boolean testCleanNeeded(File[] files, long triggerSize){
		
		long total = 0;
		
		for(File f: files){
			total += f.length();
			if(total > triggerSize){
				return true;
			}
		}
		
		return false;
	}
	
	private static void cleanCache(File[] files, long maxSize){
		
		long total = 0;
		int deletes = 0;
		
		for(int i = 0; i < files.length; i++){
			
			File f = files[i];
			
			if(f.isFile()){
			
				total += f.length();
				
				if(total < maxSize){
					//ok
				}else{				
					f.delete();
					deletes++;
					//AQUtility.debug("del", f.getAbsolutePath());
				}
				
			}
		}
		
		AQUtility.debug("deleted" , deletes);
	}
	
	
    // Mapping table from 6-bit nibbles to Base64 characters.
    private static final char[] map1 = new char[64];
       static {
          int i=0;
          for (char c='A'; c<='Z'; c++) map1[i++] = c;
          for (char c='a'; c<='z'; c++) map1[i++] = c;
          for (char c='0'; c<='9'; c++) map1[i++] = c;
          map1[i++] = '+'; map1[i++] = '/'; }

    // Mapping table from Base64 characters to 6-bit nibbles.
    private static final byte[] map2 = new byte[128];
       static {
          for (int i=0; i<map2.length; i++) map2[i] = -1;
          for (int i=0; i<64; i++) map2[map1[i]] = (byte)i; }
    
    //Source: http://www.source-code.biz/base64coder/java/Base64Coder.java.txt
    public static char[] encode64(byte[] in, int iOff, int iLen) {
        
       int oDataLen = (iLen*4+2)/3;       // output length without padding
       int oLen = ((iLen+2)/3)*4;         // output length including padding
       char[] out = new char[oLen];
       int ip = iOff;
       int iEnd = iOff + iLen;
       int op = 0;
       while (ip < iEnd) {
          int i0 = in[ip++] & 0xff;
          int i1 = ip < iEnd ? in[ip++] & 0xff : 0;
          int i2 = ip < iEnd ? in[ip++] & 0xff : 0;
          int o0 = i0 >>> 2;
          int o1 = ((i0 &   3) << 4) | (i1 >>> 4);
          int o2 = ((i1 & 0xf) << 2) | (i2 >>> 6);
          int o3 = i2 & 0x3F;
          out[op++] = map1[o0];
          out[op++] = map1[o1];
          out[op] = op < oDataLen ? map1[o2] : '='; op++;
          out[op] = op < oDataLen ? map1[o3] : '='; op++; }
       return out; 
    }

}
