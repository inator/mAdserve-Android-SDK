package com.madserve.sdk.data;

import com.madserve.sdk.AdType;

public class Response {

	private AdType type;
	private int bannerWidth;
	private int bannerHeight;
	private String text;
	private String imageUrl;
	private ClickType clickType;
	private String clickUrl;
	private UrlType urlType;
	private int refresh;
	private boolean scale;
	private boolean skipPreflight;

	public AdType getType() {
		return type;
	}

	public void setType(AdType adType) {
		this.type = adType;
	}

	public int getBannerWidth() {
		return bannerWidth;
	}

	public void setBannerWidth(int bannerWidth) {
		this.bannerWidth = bannerWidth;
	}

	public int getBannerHeight() {
		return bannerHeight;
	}

	public void setBannerHeight(int bannerHeight) {
		this.bannerHeight = bannerHeight;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public ClickType getClickType() {
		return clickType;
	}

	public void setClickType(ClickType clickType) {
		this.clickType = clickType;
	}

	public String getClickUrl() {
		return clickUrl;
	}

	public void setClickUrl(String clickUrl) {
		this.clickUrl = clickUrl;
	}

	public UrlType getUrlType() {
		return urlType;
	}

	public void setUrlType(UrlType urlType) {
		this.urlType = urlType;
	}

	public int getRefresh() {
		return refresh;
	}

	public void setRefresh(int refresh) {
		this.refresh = refresh;
	}

	public boolean isScale() {
		return scale;
	}

	public void setScale(boolean scale) {
		this.scale = scale;
	}

	public boolean isSkipPreflight() {
		return skipPreflight;
	}

	public void setSkipPreflight(boolean skipPreflight) {
		this.skipPreflight = skipPreflight;
	}

	@Override
	public String toString() {
		return "Response [refresh=" + refresh + ", type=" + type + ", bannerWidth=" + bannerWidth + ", bannerHeight=" + bannerHeight
				+ ", text=" + text + ", imageUrl=" + imageUrl + ", clickType=" + clickType + ", clickUrl=" + clickUrl + ", urlType=" + urlType
				+ ", scale=" + scale + ", skipPreflight=" + skipPreflight + "]";
	}

}
