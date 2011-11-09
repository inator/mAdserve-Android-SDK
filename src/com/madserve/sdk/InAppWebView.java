package com.madserve.sdk;

import static com.madserve.sdk.Const.TAG;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class InAppWebView extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		final String url = (String) bundle.get(Const.REDIRECT_URI);
		if (url == null) {
			if (Log.isLoggable(TAG, Log.DEBUG)) {
				Log.d(TAG, "url is null so do not load anything");
			}
			return;
		}

		final WebView webView = (WebView) new WebView(this);

		webView.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(final WebView view, String url) {
				view.loadUrl(url);
				return false; // then it is not handled by default action
			}
		});

		webView.loadUrl(url);
		setContentView(webView);
	}
}
