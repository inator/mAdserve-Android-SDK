package com.madserve.sdk;

import static com.madserve.sdk.Const.PREFS_DEVICE_ID;
import static com.madserve.sdk.Const.PROTOCOL_VERSION;
import static com.madserve.sdk.Const.TAG;
import static com.madserve.sdk.Const.VERSION;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.MessageFormat;
import java.util.Timer;
import java.util.UUID;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.ViewFlipper;

import com.madserve.sdk.data.ClickType;
import com.madserve.sdk.data.Request;
import com.madserve.sdk.data.Response;

/**
 * Viewer of advertising.
 */
public class AdserveView extends RelativeLayout {

	private boolean includeLocation = false;
	private Mode mode;
	private String publisherId;
	private String serverUrl;
	private boolean animation;

	private Timer reloadTimer;
	boolean isInternalBrowser = false;

	private Response response;
	private Animation fadeInAnimation = null;
	private Animation fadeOutAnimation = null;
	private WebSettings webSettings;
	private Request request;

	private LocationManager locationManager;
	private int isAccessFineLocation;
	private int isAccessCoarseLocation;
	private int telephonyPermission;

	private WebView firstWebView;
	private WebView secondWebView;

	private ViewFlipper viewFlipper;

	private BannerListener bannerListener;

	private boolean touchMove;

	private Thread loadContentThread;

	final Handler updateHandler = new Handler();

	final Runnable showContent = new Runnable() {
		public void run() {
			showContent();
		}
	};

	private OnTouchListener onTouchListener = new View.OnTouchListener() {

		private float distanceX;
		private float distanceY;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			try {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					touchMove = false;
					distanceX = event.getX();
					distanceY = event.getY();
				}
				if (event.getAction() == MotionEvent.ACTION_MOVE) {
					if (Math.abs(distanceX - event.getX()) > Const.TOUCH_DISTANCE) {
						touchMove = true;
					}
					if (Math.abs(distanceY - event.getY()) > Const.TOUCH_DISTANCE) {
						touchMove = true;
					}
					if (Log.isLoggable(TAG, Log.DEBUG)) {
						Log.d(TAG, "touchMove: " + touchMove);
					}
					return true;
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					if (Log.isLoggable(TAG, Log.DEBUG)) {
						Log.d(TAG, "size x: " + event.getX());
						Log.d(TAG, "getHistorySize: " + event.getHistorySize());
					}
					if (response != null && !touchMove) {
						openLink();
						if (bannerListener != null) {
							bannerListener.adClicked();
						}
					}
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
			return AdserveView.this.onTouchEvent(event);
		}
	};

	/**
	 * Creates an instance of the AdserveView using the live mode.
	 * 
	 * @param context
	 * @param publisherId
	 * @param serverUrl
	 * @param includeLocation
	 */
	public AdserveView(final Context context, final String publisherId, final String serverUrl, final boolean includeLocation,
			final boolean animation) {
		this(context, publisherId, serverUrl, Mode.LIVE, includeLocation, animation);
	}

	/**
	 * 
	 * 
	 * @param context
	 * @param publisherId
	 *          the publisherId
	 * @param serverUrl
	 *          mAdserve Url
	 * @param mode
	 *          the operating mode
	 * @param includeLocation
	 *          if true the location is added to the request, otherwise not location data is send.
	 */
	public AdserveView(final Context context, final String publisherId, final String serverUrl, final Mode mode,
			final boolean includeLocation, final boolean animation) {
		super(context);
		this.publisherId = publisherId;
		this.serverUrl = serverUrl;
		this.includeLocation = includeLocation;
		this.mode = mode;
		this.animation = animation;
		initialize(context);
	}

	/**
	 * Default constructor
	 * 
	 * @param context
	 */
	public AdserveView(final Context context, final AttributeSet attributes) {
		super(context, attributes);

		if (attributes != null) {
			int count = attributes.getAttributeCount();
			for (int i = 0; i < count; i++) {
				String name = attributes.getAttributeName(i);
				if (name.equals("publisherId")) {
					int resId = attributes.getAttributeResourceValue(i, 0);
					if (resId != 0) {
						try {
							this.publisherId = context.getString(resId);
						} catch (Exception e) {
							this.publisherId = attributes.getAttributeValue(i);
						}
					} else {
						this.publisherId = attributes.getAttributeValue(i);
					}
					if (Log.isLoggable(TAG, Log.DEBUG)) {
						Log.d(TAG, "mAdserve Publisher Id:" + publisherId);
					}
				} else if (name.equals("serverUrl")) {
					int resId = attributes.getAttributeResourceValue(i, 0);
					if (resId != 0) {
						try {
							this.serverUrl = context.getString(resId);
						} catch (Exception e) {
							this.serverUrl = attributes.getAttributeValue(i);
						}
					} else {
						this.serverUrl = attributes.getAttributeValue(i);
					}
					if (Log.isLoggable(TAG, Log.DEBUG)) {
						Log.d(TAG, "mAdserve url:" + serverUrl);
					}
				} else if (name.equals("mode")) {
					String modeValue = attributes.getAttributeValue(i);
					if (modeValue != null && modeValue.equalsIgnoreCase("test")) {
						this.mode = Mode.TEST;
					}
					if (Log.isLoggable(TAG, Log.DEBUG)) {
						Log.d(TAG, "mAdserve Mode:" + modeValue);
					}
				} else if (name.equals("animation")) {
					this.animation = attributes.getAttributeBooleanValue(i, false);
					if (Log.isLoggable(TAG, Log.DEBUG)) {
						Log.d(TAG, "mAdserve Animation:" + animation);
					}
				} else if (name.equals("includeLocation")) {
					this.includeLocation = attributes.getAttributeBooleanValue(i, false);
					if (Log.isLoggable(TAG, Log.DEBUG)) {
						Log.d(TAG, "mAdserve includeLocation:" + includeLocation);
					}
				}
			}
		}
		initialize(context);
	}

	private void initialize(Context context) {
		if (Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, "mAdserve SDK Version:" + VERSION);
		}
		locationManager = null;
		telephonyPermission = context.checkCallingOrSelfPermission(Manifest.permission.READ_PHONE_STATE);
		isAccessFineLocation = context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
		isAccessCoarseLocation = context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
		if ((isAccessFineLocation == PackageManager.PERMISSION_GRANTED) || (isAccessCoarseLocation == PackageManager.PERMISSION_GRANTED)) {
			locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
		}
		firstWebView = createWebView(context);
		secondWebView = createWebView(context);
		if (Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, "Create view flipper");
		}
		viewFlipper = new ViewFlipper(getContext()) {
			/*
			 * See http://daniel-codes.blogspot.com/2010/05/viewflipper-receiver-not-registered.html
			 */
			@Override
			protected void onDetachedFromWindow() {
				try {
					super.onDetachedFromWindow();
				} catch (IllegalArgumentException e) {
					stopFlipping();
				}
			}
		};
		viewFlipper.addView(firstWebView);
		viewFlipper.addView(secondWebView);

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		addView(viewFlipper, params);

		firstWebView.setOnTouchListener(onTouchListener);
		secondWebView.setOnTouchListener(onTouchListener);

		if (Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, "animation: " + animation);
		}
		if (animation) {
			// fadeInAnimation = new AlphaAnimation(0.0f, 1.0f);
			fadeInAnimation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, +1.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
			fadeInAnimation.setDuration(1000);

			// fadeOutAnimation = new AlphaAnimation(1.0f, 0.0f);
			fadeOutAnimation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, -1.0f);
			fadeOutAnimation.setDuration(1000);
			viewFlipper.setInAnimation(fadeInAnimation);
			viewFlipper.setOutAnimation(fadeOutAnimation);
		}
	}

	private WebView createWebView(final Context context) {
		WebView webView = new WebView(this.getContext());

		webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		// webSettings.setSavePassword(false);
		// webSettings.setSaveFormData(false);
		// webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		// webSettings.setSupportZoom(false);
		webView.setBackgroundColor(Color.TRANSPARENT);

		webView.setWebViewClient(new AdserveWebViewClient(this, context));

		webView.setVerticalScrollBarEnabled(false);
		webView.setHorizontalScrollBarEnabled(false);

		return webView;
	}

	private String getDeviceId() {
		String androidId = Secure.getString(this.getContext().getContentResolver(), Secure.ANDROID_ID);
		if ((androidId == null) || (androidId.equals("9774d56d682e549c")) || (androidId.equals("0000000000000000"))) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
			androidId = prefs.getString(PREFS_DEVICE_ID, null);
			if (androidId == null) {
				try {
					String uuid = UUID.randomUUID().toString();
					MessageDigest digest = MessageDigest.getInstance("MD5");
					digest.update(uuid.getBytes(), 0, uuid.length());
					androidId = String.format("%032X", new Object[] { new BigInteger(1, digest.digest()) }).substring(0, 16);
				} catch (Exception e) {
					Log.d(TAG, "Could not generate pseudo unique id", e);
					androidId = "9774d56d682e549c";
				}
				prefs.edit().putString(PREFS_DEVICE_ID, androidId).commit();
			}
			if (Log.isLoggable(TAG, Log.DEBUG)) {
				Log.d(TAG, "Unknown Android ID using pseudo unique id:" + androidId);
			}
		}
		return androidId;
	}

	private Request getRequest() {
		if (request == null) {
			request = new Request();
			if (telephonyPermission == PackageManager.PERMISSION_GRANTED) {
				TelephonyManager tm = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
				request.setDeviceId(tm.getDeviceId());
				request.setProtocolVersion(PROTOCOL_VERSION);
			} else {
				request.setDeviceId(getDeviceId());
				request.setProtocolVersion("N" + PROTOCOL_VERSION);
			}
			request.setMode(mode);
			request.setPublisherId(publisherId);
			request.setServerUrl(serverUrl);
			request.setUserAgent(webSettings.getUserAgentString());
		}
		Location location = null;
		if (this.includeLocation) {
			location = getLocation();
		}
		if (location != null) {
			if (Log.isLoggable(TAG, Log.DEBUG)) {
				Log.d(TAG, "location is longitude: " + location.getLongitude() + ", latitude: " + location.getLatitude());
			}
			request.setLatitude(location.getLatitude());
			request.setLongitude(location.getLongitude());
		} else {
			request.setLatitude(0.0);
			request.setLongitude(0.0);
		}
		return request;
	}

	public boolean isInternalBrowser() {
		return isInternalBrowser;
	}

	public void setInternalBrowser(boolean isInternalBrowser) {
		this.isInternalBrowser = isInternalBrowser;
	}

	private void loadContent() {
		if (Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, "load content");
		}

		if (loadContentThread == null) {
			loadContentThread = new Thread(new Runnable() {
				@Override
				public void run() {
					if (Log.isLoggable(TAG, Log.DEBUG)) {
						Log.d(TAG, "starting request thread");
					}
					RequestAd requestAd = new RequestAd();
					try {

						response = requestAd.sendRequest(getRequest());
						if (response != null) {
							if (Log.isLoggable(TAG, Log.DEBUG)) {
								Log.d(TAG, "response received");
							}

							if (Log.isLoggable(TAG, Log.DEBUG)) {
								Log.d(TAG, "getVisibility: " + getVisibility());
							}
							updateHandler.post(showContent);
						}
					} catch (Throwable e) {
						if (Log.isLoggable(TAG, Log.ERROR)) {
							Log.e(TAG, "Uncaught exception in request thread", e);
						}
						if (bannerListener != null) {
							Log.d(TAG, "notify bannerListener: " + bannerListener.getClass().getName());
							RequestException ex;
							if (e instanceof RequestException) {
								ex = (RequestException) e;
							} else {
								ex = new RequestException(e);
							}
							bannerListener.bannerLoadFailed(ex);
						}
						Log.e(TAG, e.getMessage(), e);
					}
					loadContentThread = null;
					if (Log.isLoggable(TAG, Log.DEBUG)) {
						Log.d(TAG, "finishing request thread");
					}
				}

			});
			loadContentThread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

				@Override
				public void uncaughtException(Thread thread, Throwable ex) {
					if (Log.isLoggable(TAG, Log.ERROR)) {
						Log.e(TAG, "Exception in request thread detected. Closing request thread.", ex);
					}
					loadContentThread = null;
				}
			});
			loadContentThread.start();
		}

	}

	int count = 0;

	private void showContent() {

		try {

			WebView webView;
			if (viewFlipper.getCurrentView() == firstWebView) {
				webView = secondWebView;
			} else {
				webView = firstWebView;
			}
			if (response.getType() == AdType.IMAGE) {

				String text = MessageFormat.format(Const.IMAGE, response.getImageUrl(), response.getBannerWidth(), response.getBannerHeight());
				if (Log.isLoggable(TAG, Log.DEBUG)) {
					Log.d(TAG, "set image: " + text);
				}
				text = Uri.encode(Const.HIDE_BORDER + text);
				webView.loadData(text, "text/html", Const.ENCODING);
				if (bannerListener != null) {
					if (Log.isLoggable(TAG, Log.DEBUG)) {
						Log.d(TAG, "notify bannerListener of load succeeded: " + bannerListener.getClass().getName());
					}
					bannerListener.bannerLoadSucceeded();
				}
			} else if (response.getType() == AdType.TEXT) {
				String text = Uri.encode(Const.HIDE_BORDER + response.getText());
				if (Log.isLoggable(TAG, Log.DEBUG)) {
					Log.d(TAG, "set text: " + text);
				}
				webView.loadData(text, "text/html", Const.ENCODING);
				if (bannerListener != null) {
					if (Log.isLoggable(TAG, Log.DEBUG)) {
						Log.d(TAG, "notify bannerListener of load succeeded: " + bannerListener.getClass().getName());
					}
					bannerListener.bannerLoadSucceeded();
				}
			} else {
				if (Log.isLoggable(TAG, Log.DEBUG)) {
					Log.d(TAG, "No Ad");
				}
				if (bannerListener != null) {
					bannerListener.noAdFound();
				}
				return;
			}

			if (viewFlipper.getCurrentView() == firstWebView) {
				if (Log.isLoggable(TAG, Log.DEBUG)) {
					Log.d(TAG, "show next");
				}
				viewFlipper.showNext();
			} else {
				if (Log.isLoggable(TAG, Log.DEBUG)) {
					Log.d(TAG, "show previous");
				}
				viewFlipper.showPrevious();
			}

			startReloadTimer();
		} catch (Throwable t) {
			if (Log.isLoggable(TAG, Log.ERROR)) {
				Log.e(TAG, "Uncaught exception in show content", t);
			}
		}
	}

	private void startReloadTimer() {
		if (Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, "start reload timer");
		}
		if (reloadTimer == null) {
			return;
		}

		int refreshTime = response.getRefresh() * 1000;
		if (Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, "set timer: " + refreshTime);
		}

		ReloadTask reloadTask = new ReloadTask(AdserveView.this);
		reloadTimer.schedule(reloadTask, refreshTime);
	}

	private Location getLocation() {

		if (locationManager != null) {
			if (isAccessFineLocation == PackageManager.PERMISSION_GRANTED) {
				boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
				if (isGpsEnabled) {
					return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				}
			}
			if (isAccessCoarseLocation == PackageManager.PERMISSION_GRANTED) {
				boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

				if (isNetworkEnabled) {
					return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				}
			}
		}
		return null;
	}

	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		super.onWindowVisibilityChanged(visibility);

		if (visibility == VISIBLE) {
			resume();
		} else {
			pause();
		}
		if (Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, "onWindowVisibilityChanged: " + visibility);
		}
	}

	public void openLink() {

		if (response != null && response.getClickUrl() != null) {

			if (response.isSkipPreflight()) {
				doOpenUrl(response.getClickUrl());
			} else {
				if (Log.isLoggable(TAG, Log.DEBUG)) {
					Log.d(TAG, "prefetch url: " + response.getClickUrl());
				}

				DefaultHttpClient client = new DefaultHttpClient();
				HttpConnectionParams.setSoTimeout(client.getParams(), Const.SOCKET_TIMEOUT);
				HttpConnectionParams.setConnectionTimeout(client.getParams(), Const.CONNECTION_TIMEOUT);
				HttpGet get = new HttpGet(response.getClickUrl());

				HttpResponse response;
				HttpContext httpContext = new BasicHttpContext();
				try {
					response = client.execute(get, httpContext);
					if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
						throw new IOException(response.getStatusLine().toString());
					}
					HttpUriRequest currentRequeset = (HttpUriRequest) httpContext.getAttribute(ExecutionContext.HTTP_REQUEST);
					HttpHost currentHost = (HttpHost) httpContext.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
					String currentUrl = currentHost.toURI() + currentRequeset.getURI();
					doOpenUrl(currentUrl);
				} catch (ClientProtocolException e) {
					Log.e(TAG, "Error in HTTP request", e);
				} catch (IOException e) {
					Log.e(TAG, "Error in HTTP request", e);
				} catch (Throwable t) {
					Log.e(TAG, "Error in HTTP request", t);
				}

			}
		}

	}

	private void doOpenUrl(String url) {
		if (response.getClickType() != null && response.getClickType().equals(ClickType.INAPP)) {
			Intent intent = new Intent(getContext(), InAppWebView.class);
			intent.putExtra(Const.REDIRECT_URI, response.getClickUrl());
			getContext().startActivity(intent);
		} else {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			getContext().startActivity(intent);
		}
	}

	public void loadNextAd() {
		if (Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, "load next ad");
		}
		loadContent();
	}

	/**
	 * Sets the banner listener. Only one listener can be added.
	 * 
	 * @param bannerListener
	 */
	public void setBannerListener(BannerListener bannerListener) {
		this.bannerListener = bannerListener;
	}

	public void pause() {
		if (reloadTimer != null) {
			try {
				if (Log.isLoggable(TAG, Log.DEBUG)) {
					Log.d(TAG, "cancel reload timer");
				}
				reloadTimer.cancel();
				reloadTimer = null;
			} catch (Exception e) {
				Log.e(TAG, "unable to cancel reloadTimer", e);
			}
		}
	}

	public void resume() {
		if (reloadTimer != null) {
			reloadTimer.cancel();
			reloadTimer = null;
		}
		reloadTimer = new Timer();
		if (Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, "response: " + response);
		}

		if (response != null && response.getRefresh() > 0) {
			startReloadTimer();
		} else {
			loadContent();
		}
	}

}