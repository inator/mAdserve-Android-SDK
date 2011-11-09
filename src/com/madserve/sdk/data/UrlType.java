package com.madserve.sdk.data;

public enum UrlType {
	WEB, OTHER;

	public static UrlType getValue(String value) {
		for (UrlType urlType : values()) {
			if (urlType.name().equalsIgnoreCase(value)) {
				return urlType;
			}
		}
		return null;
	}
}