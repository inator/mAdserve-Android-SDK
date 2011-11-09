package com.madserve.sdk;

public class RequestException extends Exception {

	private static final long serialVersionUID = 1L;

	public RequestException() {
		super();
	}

	public RequestException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public RequestException(String detailMessage) {
		super(detailMessage);
	}

	public RequestException(Throwable throwable) {
		super(throwable);
	}

}
