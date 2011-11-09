package com.madserve.sdk;

import android.content.Context;
import android.webkit.WebView;
import android.webkit.WebViewClient;

class AdserveWebViewClient extends WebViewClient {
	/**
	 * 
	 */
	private final AdserveView adServerViewCore;
	private Context context;

	public AdserveWebViewClient(AdserveView adServerViewCore, Context context) {
		this.adServerViewCore = adServerViewCore;
		this.context = context;
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		// if (this.adServerViewCore.isInternalBrowser) {
		// if (this.adServerViewCore.adClickListener != null) {
		// this.adServerViewCore.adClickListener.click(url);
		// }
		// } else {
		// int isAccessNetworkState =
		// context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE);
		//
		// if (isAccessNetworkState == PackageManager.PERMISSION_GRANTED) {
		// if (this.adServerViewCore.isInternetAvailable(context)) {
		// this.adServerViewCore.openUrlInExternalBrowser(context, url);
		// } else {
		// Toast.makeText(context, "Internet is not available",
		// Toast.LENGTH_LONG).show();
		// }
		// } else if (isAccessNetworkState == PackageManager.PERMISSION_DENIED) {
		// this.adServerViewCore.openUrlInExternalBrowser(context, url);
		// }
		// }

		return true;
	}
}