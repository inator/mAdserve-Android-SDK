package com.madserve.sdk;

public interface BannerListener {

	/**
	 * This method is called when the banner is clicked.
	 */
	public void adClicked();

	/**
	 * This method is called when the banner loading has failed an no banner is displayed.
	 */
	public void noAdFound();

	/**
	 * This method is called if an error occurred while loading the banner
	 * 
	 * @param e
	 */
	public void bannerLoadFailed(RequestException e);

	/**
	 * This method is called after loading the banner successfully
	 * 
	 * @param e
	 */
	public void bannerLoadSucceeded();
}
