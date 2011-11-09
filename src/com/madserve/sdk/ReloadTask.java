package com.madserve.sdk;

import java.util.TimerTask;

class ReloadTask extends TimerTask {

	private final AdserveView mAdserveView;

	public ReloadTask(AdserveView mAdserveView) {
		this.mAdserveView = mAdserveView;
	}

	@Override
	public void run() {
		mAdserveView.loadNextAd();
	}
}