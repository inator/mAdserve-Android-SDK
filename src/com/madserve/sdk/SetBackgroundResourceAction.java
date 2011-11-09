package com.madserve.sdk;

import android.webkit.WebView;

class SetBackgroundResourceAction implements Runnable {
	private WebView view;
	private int backgroundResource;

	public SetBackgroundResourceAction(WebView view, int backgroundResource) {
		this.view = view;
		this.backgroundResource = backgroundResource;
	}

	@Override
	public void run() {
		try {
			view.setBackgroundResource(backgroundResource);
		} catch (Exception e) {
		}
	}
}