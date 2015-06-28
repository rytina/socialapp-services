package com.socialapp.services.ui;

import com.socialapp.services.IResultProcessor;

public interface UiThreadExecuter {
	
	<T> void execute(IResultProcessor<T> processor, T result, Object ...params);

}
