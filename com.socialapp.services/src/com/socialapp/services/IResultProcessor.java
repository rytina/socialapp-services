package com.socialapp.services;


public interface IResultProcessor<T> {
	
	IResultProcessor<String> NULL_PROCESSOR = new IResultProcessor<String>() {
		public void process(String result, Object... params) {
		}
	};

	void process(T result, Object ...params);

}
