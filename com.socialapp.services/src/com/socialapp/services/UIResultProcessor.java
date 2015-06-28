package com.socialapp.services;

import com.socialapp.services.ui.UiThreadExecuter;


public interface UIResultProcessor<T> extends IResultProcessor<T>{
	
	public UiThreadExecuter getUiExecuter();
	
}
