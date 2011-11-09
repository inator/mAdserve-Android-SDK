package com.madserve.sdk.data;

public enum ClickType {
	INAPP, BROWSER;

	public static ClickType getValue(String value) {
		for (ClickType clickType : values()) {
			if (clickType.name().equalsIgnoreCase(value)) {
				return clickType;
			}
		}
		return null;
	}
}