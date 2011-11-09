package com.madserve.sdk.data;

import android.os.Build;

import com.madserve.sdk.Mode;

public class Request {

	private String requestType;
	private String userAgent;
	private String serverUrl;
	private String headers;
	private String deviceId;
	private Mode mode;
	private String protocolVersion;
	private String publisherId;
	private double longitude = 0.0;
	private double latitude = 0.0;

	public String getRequestType() {
		if (requestType == null) {
			return "";
		}
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public String getUserAgent() {
		if (userAgent == null) {
			return "";
		}
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getHeaders() {
		if (headers == null) {
			return "";
		}
		return headers;
	}

	public void setHeaders(String headers) {
		this.headers = headers;
	}

	public String getDeviceId() {
		if (deviceId == null) {
			return "";
		}
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public Mode getMode() {
		if (mode == null) {
			mode = Mode.LIVE;
		}
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public String getPublisherId() {
		if (publisherId == null) {
			return "";
		}
		return publisherId;
	}

	public void setPublisherId(String publisherId) {
		this.publisherId = publisherId;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public String getServerUrl() {
		if (serverUrl == null) {
			return "";
		}
		return serverUrl;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public String getDeviceMode() {
		return Build.MODEL;
	}

	public String getAndroidVersion() {
		return Build.VERSION.RELEASE;
	}

	public String getProtocolVersion() {
		if (protocolVersion == null) {
			return "";
		}
		return protocolVersion;
	}

	public void setProtocolVersion(String protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

}