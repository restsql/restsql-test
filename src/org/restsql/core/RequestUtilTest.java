package org.restsql.core;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class RequestUtilTest {
	@Test
	public void testConvertToStandardInternetMediaType() {
		assertEquals("application/json", RequestUtil.convertToStandardInternetMediaType("application/json"));
		assertEquals("application/json", RequestUtil.convertToStandardInternetMediaType("json"));
		assertEquals("application/json", RequestUtil.convertToStandardInternetMediaType("JSON"));
		assertEquals("application/xml", RequestUtil.convertToStandardInternetMediaType("application/xml"));
		assertEquals("application/xml", RequestUtil.convertToStandardInternetMediaType("xml"));
		assertEquals("application/xml", RequestUtil.convertToStandardInternetMediaType("XML"));
	}

	@Test
	public void testGetRequestMediaType() {
		assertEquals(null, RequestUtil.getRequestMediaType(null));
		assertEquals(null, RequestUtil.getRequestMediaType(""));
		assertEquals("unknown", RequestUtil.getRequestMediaType("unknown"));
		assertEquals("application/unknown", RequestUtil.getRequestMediaType("application/unknown"));
		assertEquals("application/json", RequestUtil.getRequestMediaType("application/json"));
		assertEquals("application/json", RequestUtil.getRequestMediaType("application/json; charset=UTF-8"));
		assertEquals("application/xml", RequestUtil.getRequestMediaType("application/xml"));
		assertEquals("application/xml", RequestUtil.getRequestMediaType("application/xml; charset=UTF-8"));
		assertEquals("application/x-www-form-urlencoded", RequestUtil.getRequestMediaType("application/x-www-form-urlencoded"));
		assertEquals("application/x-www-form-urlencoded", RequestUtil.getRequestMediaType("application/x-www-form-urlencoded; charset=UTF-8"));
		assertEquals("application/json", RequestUtil.getRequestMediaType("application/json;q=0.9,application/xml;q=0.8"));
		assertEquals("application/xml", RequestUtil.getRequestMediaType("application/json;q=0.9,application/xml"));
	}

	@Test
	public void testGetResponseMediaType_WithParamsAndRequestMediaType() {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		String mediaType = RequestUtil.getResponseMediaType(params, null, null);
		assertEquals(HttpRequestAttributes.DEFAULT_MEDIA_TYPE, mediaType);
		assertEquals(0, params.size());
		
		NameValuePair param1 = new NameValuePair("test", "123");
		params.add(param1);
		mediaType = RequestUtil.getResponseMediaType(params, null, null);
		assertEquals(HttpRequestAttributes.DEFAULT_MEDIA_TYPE, mediaType);
		assertEquals(1, params.size());

		NameValuePair param2 = new NameValuePair("test2", "12345");
		params.add(param2);
		mediaType = RequestUtil.getResponseMediaType(params, "application/json", null);
		assertEquals("application/json", mediaType);
		assertEquals(2, params.size());

		mediaType = RequestUtil.getResponseMediaType(params, "application/x-www-form-urlencoded", null);
		assertEquals("application/xml", mediaType);
		assertEquals(2, params.size());
		
		params.add(new NameValuePair(Request.PARAM_NAME_OUTPUT, "application/json"));
		mediaType = RequestUtil.getResponseMediaType(params, null, null);
		assertEquals("application/json", mediaType);
		assertEquals(2, params.size());
		assertEquals(param1, params.get(0));
		assertEquals(param2, params.get(1));

		params.add(new NameValuePair(Request.PARAM_NAME_OUTPUT, "application/xml"));
		mediaType = RequestUtil.getResponseMediaType(params, "application/json", null);
		assertEquals("application/xml", mediaType);
		assertEquals(2, params.size());
		assertEquals(param1, params.get(0));
		assertEquals(param2, params.get(1));
		
		params.add(new NameValuePair(Request.PARAM_NAME_OUTPUT, "application/xml"));
		mediaType = RequestUtil.getResponseMediaType(params, "application/x-www-form-urlencoded", null);
		assertEquals("application/xml", mediaType);
		assertEquals(2, params.size());
		assertEquals(param1, params.get(0));
		assertEquals(param2, params.get(1));
	}

	@Test
	public void testGetResponseMediaType_WithParamsAndResponseMediaType() {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		NameValuePair param1 = new NameValuePair("test", "123");
		params.add(param1);
		String mediaType = RequestUtil.getResponseMediaType(params, null, "application/json");
		assertEquals("application/json", mediaType);
		assertEquals(1, params.size());

		NameValuePair param2 = new NameValuePair("test2", "12345");
		params.add(param2);
		mediaType = RequestUtil.getResponseMediaType(params, "application/json", "application/json");
		assertEquals("application/json", mediaType);
		assertEquals(2, params.size());

		mediaType = RequestUtil.getResponseMediaType(params, "application/xml", "application/json");
		assertEquals("application/json", mediaType);
		assertEquals(2, params.size());

		mediaType = RequestUtil.getResponseMediaType(params, "application/x-www-form-urlencoded", "application/json");
		assertEquals("application/json", mediaType);
		assertEquals(2, params.size());

		// Parameter overrides accept media type
		params.add(new NameValuePair(Request.PARAM_NAME_OUTPUT, "application/xml"));
		mediaType = RequestUtil.getResponseMediaType(params, "application/x-www-form-urlencoded", "application/json");
		assertEquals("application/xml", mediaType);
		assertEquals(2, params.size());
		assertEquals(param1, params.get(0));
		assertEquals(param2, params.get(1));
		
		// Parameter overrides accept media type
		params.add(new NameValuePair(Request.PARAM_NAME_OUTPUT, "application/xml"));
		mediaType = RequestUtil.getResponseMediaType(params, "application/json", "application/json");
		assertEquals("application/xml", mediaType);
		assertEquals(2, params.size());
		assertEquals(param1, params.get(0));
		assertEquals(param2, params.get(1));
	}
	
	@Test
	public void testGetResponseMediaType_WithComplexAcceptHeader() {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		NameValuePair param1 = new NameValuePair("test", "123");
		params.add(param1);
		
		String acceptMediaType = "application/xml";
		assertEquals("application/xml", RequestUtil.getResponseMediaType(params, null, acceptMediaType));
		
		acceptMediaType = "application/xml; charset=UTF-8";
		assertEquals("application/xml", RequestUtil.getResponseMediaType(params, null, acceptMediaType));

		acceptMediaType = "application/json";
		assertEquals("application/json", RequestUtil.getResponseMediaType(params, null, acceptMediaType));
		
		acceptMediaType = "application/json; charset=UTF-8";
		assertEquals("application/json", RequestUtil.getResponseMediaType(params, null, acceptMediaType));
		
		acceptMediaType = "*/*";
		assertEquals("application/xml", RequestUtil.getResponseMediaType(params, null, acceptMediaType));

		acceptMediaType = "application/*";
		assertEquals("application/xml", RequestUtil.getResponseMediaType(params, null, acceptMediaType));

		acceptMediaType = "application/json;q=0.9,application/xml;q=0.8";
		assertEquals("application/json", RequestUtil.getResponseMediaType(params, null, acceptMediaType));
		
		acceptMediaType = "application/json;q=0.9,application/xml";
		assertEquals("application/xml", RequestUtil.getResponseMediaType(params, null, acceptMediaType));
		
		// Chrome
		acceptMediaType = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
		assertEquals("application/xml", RequestUtil.getResponseMediaType(params, null, acceptMediaType));
		
		// Firefox
		acceptMediaType = "text/xml, application/xml, application/xhtml+xml, text/html;q=0.9, text/plain;q=0.8, image/png,*/*;q=0.5";
		assertEquals("application/xml", RequestUtil.getResponseMediaType(params, null, acceptMediaType));
		
		//IE 
		acceptMediaType = "image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/x-shockwave-flash, */*";
		assertEquals("application/xml", RequestUtil.getResponseMediaType(params, null, acceptMediaType));
	}
	
	@Test
	public void testGetResponseMediaType_WithInvalidAcceptHeader() {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		NameValuePair param1 = new NameValuePair("test", "123");
		params.add(param1);

		String acceptMediaType = null;
		assertEquals("application/xml", RequestUtil.getResponseMediaType(params, null, acceptMediaType));

		acceptMediaType = "";
		assertEquals("application/xml", RequestUtil.getResponseMediaType(params, null, acceptMediaType));

		acceptMediaType = "application/unknown";
		assertEquals("application/unknown", RequestUtil.getResponseMediaType(params, null, acceptMediaType));
	}
	
}
