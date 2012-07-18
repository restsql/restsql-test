/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.restsql.core.Factory.SqlResourceFactoryException;

public class RequestFactoryTest extends BaseTestCase {
	@Test
	public void testGetRequest_Insert() throws InvalidRequestException, SqlResourceFactoryException {
		final Request request = RequestFactoryHelper.getRequest(Request.Type.INSERT, "Test", null,
				new String[] { "actor_id", "1003", "first_name", "Patty", "last_name", "White" });
		assertEquals(Request.Type.INSERT, request.getType());
		assertEquals("Test", request.getSqlResource());
		assertNull(request.getResourceIdentifiers());
		assertNotNull(request.getParameters());
		assertEquals(3, request.getParameters().size());
	}

	@Test(expected = InvalidRequestException.class)
	public void testGetRequest_InsertInvalidParams() throws InvalidRequestException,
			SqlResourceFactoryException {
		RequestFactoryHelper.getRequest(Request.Type.INSERT, null, new String[] { "actor_id", "1003",
				"first_name", "Patty", "last_name", "White" }, null);
	}

	@Test(expected = InvalidRequestException.class)
	public void testGetRequest_InsertInvalidSqlResource() throws InvalidRequestException,
			SqlResourceFactoryException {
		RequestFactoryHelper.getRequest(Request.Type.INSERT, null, new String[] { "actor_id", "1003",
				"first_name", "Patty", "last_name", "White" }, null);
	}

	@Test(expected = InvalidRequestException.class)
	public void testGetRequest_SelectInvalidSqlResource() throws InvalidRequestException,
			SqlResourceFactoryException {
		RequestFactoryHelper.getRequest(Request.Type.SELECT, null, new String[] { "actor_id", "1003",
				"first_name", "Patty", "last_name", "White" }, null);
	}

	@Test
	public void testGetRequest_SelectNoParameters() throws InvalidRequestException,
			SqlResourceFactoryException {
		final Request request = RequestFactoryHelper.getRequest(Request.Type.SELECT, "Test", new String[] {
				"actor_id", "1003" }, null);
		assertEquals(Request.Type.SELECT, request.getType());
		assertEquals("Test", request.getSqlResource());
		assertNotNull(request.getResourceIdentifiers());
		assertEquals(1, request.getResourceIdentifiers().size());
		assertNull(request.getParameters());
	}

	@Test
	public void testGetRequest_SelectWithParameters() throws InvalidRequestException,
			SqlResourceFactoryException {
		final Request request = RequestFactoryHelper.getRequest(Request.Type.SELECT, "Test", new String[] {
				"actor_id", "1003" }, new String[] { "first_name", "Patty", "last_name", "White" });
		assertEquals(Request.Type.SELECT, request.getType());
		assertEquals("Test", request.getSqlResource());
		assertNotNull(request.getResourceIdentifiers());
		assertEquals(1, request.getResourceIdentifiers().size());
		assertNotNull(request.getParameters());
		assertEquals(2, request.getParameters().size());
	}

	@Test
	public void testGetRequest_Update() throws InvalidRequestException, SqlResourceFactoryException {
		final Request request = RequestFactoryHelper.getRequest(Request.Type.UPDATE, "Test", new String[] {
				"actor_id", "1003" }, new String[] { "first_name", "Patty", "last_name", "White" });
		assertEquals(Request.Type.UPDATE, request.getType());
		assertEquals("Test", request.getSqlResource());
		assertNotNull(request.getResourceIdentifiers());
		assertEquals(1, request.getResourceIdentifiers().size());
		assertNotNull(request.getParameters());
		assertEquals(2, request.getParameters().size());
	}

	// params are null
	@Test(expected = InvalidRequestException.class)
	public void testGetRequest_UpdateInvalidParams() throws InvalidRequestException,
			SqlResourceFactoryException {
		RequestFactoryHelper.getRequest(Request.Type.UPDATE, "Test", new String[] { "actor_id", "1003",
				"first_name", "Patty", "last_name", "White" }, null);
	}

	@Test
	public void testResponseMediaType_WithOutputParam() throws SqlResourceException {
		HttpRequestAttributes httpAttributes = Factory
				.getHttpRequestAttributes("localhost", "GET", "/res/SingleTable/1", null, null, null);
		Request request = Factory.getRequest(httpAttributes);
		assertEquals(HttpRequestAttributes.DEFAULT_MEDIA_TYPE, httpAttributes.getResponseMediaType());

		httpAttributes = Factory.getHttpRequestAttributes("localhost", "GET",
				"/res/SingleTable/1?_output=XML", null, null, null);
		request = Factory.getRequest(httpAttributes);
		assertEquals("application/xml", httpAttributes.getResponseMediaType());

		httpAttributes = Factory.getHttpRequestAttributes("localhost", "GET",
				"/res/SingleTable/1?_output=JSON", null, null, null);
		request = Factory.getRequest(httpAttributes);
		assertEquals("application/json", httpAttributes.getResponseMediaType());
		assertEquals(0, request.getParameters().size());
	}

	@Test
	public void testResponseMediaType_WithoutOutputParam() throws SqlResourceException {
		HttpRequestAttributes httpAttributes = Factory.getHttpRequestAttributes("localhost", "GET",
				"/res/SingleTable/1?first_name=Bob&last_name=Hernandez", null, null, null);
		@SuppressWarnings("unused")
		Request request = Factory.getRequest(httpAttributes);
		assertEquals(HttpRequestAttributes.DEFAULT_MEDIA_TYPE, httpAttributes.getResponseMediaType());

		httpAttributes = Factory.getHttpRequestAttributes("localhost", "GET",
				"/res/SingleTable?first_name=Bob&_output=application/xml", null, null, null);
		request = Factory.getRequest(httpAttributes);
		assertEquals(HttpRequestAttributes.DEFAULT_MEDIA_TYPE, httpAttributes.getResponseMediaType());

		httpAttributes = Factory.getHttpRequestAttributes("localhost", "GET",
				"/res/SingleTable?first_name=Bob&_output=json", null, null, null);
		request = Factory.getRequest(httpAttributes);
		assertEquals("application/json", httpAttributes.getResponseMediaType());

		httpAttributes = Factory.getHttpRequestAttributes("localhost", "GET",
				"/res/SingleTable?first_name=New%25&_output=json", null, null, HttpRequestAttributes.DEFAULT_MEDIA_TYPE);
		request = Factory.getRequest(httpAttributes);
		assertEquals("application/json", httpAttributes.getResponseMediaType());

		httpAttributes = Factory.getHttpRequestAttributes("localhost", "DELETE",
				"/res/SingleTable?first_name=Bob", null, null, HttpRequestAttributes.DEFAULT_MEDIA_TYPE);
		request = Factory.getRequest(httpAttributes);
		assertEquals(HttpRequestAttributes.DEFAULT_MEDIA_TYPE, httpAttributes.getResponseMediaType());

		httpAttributes = Factory.getHttpRequestAttributes("localhost", "PUT",
				"/res/SingleTable?first_name=Bob", null, null, "application/json");
		request = Factory.getRequest(httpAttributes);
		assertEquals("application/json", httpAttributes.getResponseMediaType());
	}
}
