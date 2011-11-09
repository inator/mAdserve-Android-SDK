package com.madserve.sdk;

import static com.madserve.sdk.Const.ENCODING;
import static com.madserve.sdk.Const.RESPONSE_ENCODING;
import static com.madserve.sdk.Const.TAG;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Log;

import com.madserve.sdk.data.ClickType;
import com.madserve.sdk.data.Request;
import com.madserve.sdk.data.Response;

public class RequestAd {

	public RequestAd() {
	}

	public Response sendRequest(Request request) throws RequestException {
		return sendGetRequest(request);
	}

	private Response sendGetRequest(Request request) throws RequestException {
		if (Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, "send Request");
		}
		StringBuilder url = new StringBuilder(request.getServerUrl());

		url.append("?rt=android_app");
		try {
			// url.append("&i=");
			// url.append(URLEncoder.encode(request.getIpAddress(), URL_ENCODING));
			url.append("&o=");
			url.append(URLEncoder.encode(request.getDeviceId(), ENCODING));
			url.append("&m=");
			url.append(URLEncoder.encode(request.getMode().toString().toLowerCase(), ENCODING));
			url.append("&s=");
			url.append(URLEncoder.encode(request.getPublisherId(), ENCODING));
			url.append("&u=");
			url.append(URLEncoder.encode(request.getUserAgent(), ENCODING));
			url.append("&v=");
			url.append(URLEncoder.encode(request.getProtocolVersion(), ENCODING));

			if (request.getLatitude() != 0.0 && request.getLongitude() != 0.0) {
				url.append("&latitude=");
				url.append(request.getLatitude());
				url.append("&longitude=");
				url.append(request.getLongitude());
			}
		} catch (UnsupportedEncodingException e) {
			throw new RequestException("Cannot create request URL", e);
		}

		if (Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, "Perform HTTP Get Url: " + url);
		}
		DefaultHttpClient client = new DefaultHttpClient();
		HttpConnectionParams.setSoTimeout(client.getParams(), Const.SOCKET_TIMEOUT);
		HttpConnectionParams.setConnectionTimeout(client.getParams(), Const.CONNECTION_TIMEOUT);
		HttpGet get = new HttpGet(url.toString());
		HttpResponse response;
		try {
			response = client.execute(get);
			return parse(response.getEntity().getContent());
		} catch (ClientProtocolException e) {
			throw new RequestException("Error in HTTP request", e);
		} catch (IOException e) {
			throw new RequestException("Error in HTTP request", e);
		} catch (Throwable t) {
			throw new RequestException("Error in HTTP request", t);
		}

	}

	private Response parse(InputStream inputStream) throws RequestException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		DocumentBuilder db;
		Response response = new Response();

		try {
			db = dbf.newDocumentBuilder();
			InputSource src = new InputSource(inputStream);
			src.setEncoding(RESPONSE_ENCODING);
			Document doc = db.parse(src);

			Element element = doc.getDocumentElement();

			if (element == null) {
				throw new RequestException("Cannot parse Response, document is not an xml");
			}

			String errorValue = getValue(doc, "error");
			if (errorValue != null) {
				throw new RequestException("Error Response received: " + errorValue);
			}

			String type = element.getAttribute("type");
			element.normalize();
			if ("imageAd".equalsIgnoreCase(type)) {
				response.setType(AdType.IMAGE);
				response.setBannerWidth(getValueAsInt(doc, "bannerwidth"));
				response.setBannerHeight(getValueAsInt(doc, "bannerheight"));
				ClickType clickType = ClickType.getValue(getValue(doc, "clicktype"));
				response.setClickType(clickType);
				response.setClickUrl(getValue(doc, "clickurl"));
				response.setImageUrl(getValue(doc, "imageurl"));
				response.setRefresh(getValueAsInt(doc, "refresh"));
				response.setScale(getValueAsBoolean(doc, "scale"));
				response.setSkipPreflight(getValueAsBoolean(doc, "skippreflight"));
			} else if ("textAd".equalsIgnoreCase(type)) {
				response.setType(AdType.TEXT);
				response.setText(getValue(doc, "htmlString"));
				ClickType clickType = ClickType.getValue(getValue(doc, "clicktype"));
				response.setClickType(clickType);
				response.setClickUrl(getValue(doc, "clickurl"));
				response.setRefresh(getValueAsInt(doc, "refresh"));
				response.setScale(getValueAsBoolean(doc, "scale"));
				response.setSkipPreflight(getValueAsBoolean(doc, "skippreflight"));
			} else if ("noAd".equalsIgnoreCase(type)) {
				response.setType(AdType.NO_AD);
			} else {
				throw new RequestException("Unknown response type " + type);
			}

		} catch (ParserConfigurationException e) {
			throw new RequestException("Cannot parse Response", e);
		} catch (SAXException e) {
			throw new RequestException("Cannot parse Response", e);
		} catch (IOException e) {
			throw new RequestException("Cannot read Response", e);
		} catch (Throwable t) {
			throw new RequestException("Cannot read Response", t);
		}

		return response;
	}

	private boolean getValueAsBoolean(Document document, String name) {
		return "yes".equalsIgnoreCase(getValue(document, name));
	}

	private String getValue(Document document, String name) {

		NodeList nodeList = document.getElementsByTagName(name);
		Element element = (Element) nodeList.item(0);
		if (element != null) {
			nodeList = element.getChildNodes();
			if (nodeList.getLength() > 0) {
				// if (Log.isLoggable(TAG, Log.DEBUG)) {
				// Log.d(TAG, "node value for " + name + ": " +
				// nodeList.item(0).getNodeValue());
				// }
				return nodeList.item(0).getNodeValue();
			}
		}
		return null;
	}

	private int getValueAsInt(Document document, String name) {
		return getInteger(getValue(document, name));
	}

	private int getInteger(String text) {
		if (text == null) {
			return 0;
		}
		try {
			return Integer.parseInt(text);
		} catch (NumberFormatException ex) {
			// do nothing, 0 is returned
		}
		return 0;
	}
}