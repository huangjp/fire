package com.fire.common.cock.util;

import com.fire.core.service.IWolfService;
import com.fire.core.thread.WolfExecutors;

public class CommonExecutors implements IWolfService {

	private WolfExecutors executorService;

	public CommonExecutors() {

	}

	public WolfExecutors getWolfExecutors() {
		if (executorService == null) {
			executorService = WolfExecutors.newWolfThreadPool(10);
		}
		return executorService;
	}

	@Override
	public void wolfInit() {
		// TODO Auto-generated method stub

	}

	@Override
	public void wolfClose() {
		if (executorService != null)
			executorService.shutdown();
	}
}
