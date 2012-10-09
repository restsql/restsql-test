/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.core.impl;

import static junit.framework.Assert.assertEquals;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restsql.core.Factory;
import org.restsql.core.HttpRequestAttributes;
import org.restsql.core.InvalidRequestException;
import org.restsql.core.Request;
import org.restsql.core.RequestFactoryHelper;
import org.restsql.core.SqlResourceException;
import org.restsql.core.Factory.SqlResourceFactoryException;

public class SqlResourceFlatManyToOneSelectTest extends SqlResourceTestBase {

	@Before
	public void setUp() throws SQLException, SqlResourceException {
		super.setUp();
		sqlResource = Factory.getSqlResource("FlatManyToOne");
	}

	@After
	public void tearDown() throws SQLException {
		super.tearDown();
	}

	@Test
	public void testExecSelectCollection_Flat_WithParameter() throws SqlResourceException, SqlResourceFactoryException,
			InvalidRequestException {
		Request request = RequestFactoryHelper.getRequest(Request.Type.SELECT, "FlatManyToOne", null, new String[] {
				"title", "AIRPLANE SIERRA" });
		List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals(1, results.size());
		AssertionHelper.assertFilmLanguageFlat(results.get(0), 7, "AIRPLANE SIERRA", 2006, 1, "English");
	}

	@Test
	public void testExecSelectCollection_Flat_WithLikeParameter() throws SqlResourceException, SqlResourceFactoryException,
			InvalidRequestException {
		Request request = RequestFactoryHelper.getRequest(Request.Type.SELECT, "FlatManyToOne", null, new String[] {
				"title", "AIR%" });
		List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals(2, results.size());
		AssertionHelper.assertFilmLanguageFlat(results.get(0), 7, "AIRPLANE SIERRA", 2006, 1, "English");
		AssertionHelper.assertFilmLanguageFlat(results.get(1), 8, "AIRPORT POLLOCK", 2006, 1, "English");
	}

	@Test
	public void testExecSelectCollection_Flat_WithLimit() throws SqlResourceException, SqlResourceFactoryException,
			InvalidRequestException {
		Request request = RequestFactoryHelper.getRequest(Request.Type.SELECT, "FlatManyToOne", null, new String[] {
				Request.PARAM_NAME_LIMIT, "10", Request.PARAM_NAME_OFFSET, "0" });
		List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals(10, results.size());
	}

	@Test
	public void testExecSelectXml_Flat_WithLikeParameter() throws SqlResourceException, SqlResourceFactoryException,
			InvalidRequestException {
		Request request = RequestFactoryHelper.getRequest(Request.Type.SELECT, "FlatManyToOne", null, new String[] {
				"title", "AIR%" });
		String results = sqlResource.read(request, HttpRequestAttributes.DEFAULT_MEDIA_TYPE);
		String expectedRow1 = "\n\t<film film_id=\"7\" title=\"AIRPLANE SIERRA\" year=\"2006\" language_id=\"1\" name=\"English\" />";
		String expectedRow2 = "\n\t<film film_id=\"8\" title=\"AIRPORT POLLOCK\" year=\"2006\" language_id=\"1\" name=\"English\" />";
		assertEquals("<readResponse>" + expectedRow1 + expectedRow2 + "\n</readResponse>", results);
	}

	@Test
	public void testExecSelectXml_Flat_WithLikeParameter_Schema() throws SqlResourceException, SqlResourceFactoryException,
			InvalidRequestException {
		XmlResponseSerializer.setUseXmlDirective(true);
		XmlResponseSerializer.setUseXmlSchema(true);
		Request request = RequestFactoryHelper.getRequest(Request.Type.SELECT, "FlatManyToOne", null, new String[] {
				"title", "AIR%" });
		String results = sqlResource.read(request, HttpRequestAttributes.DEFAULT_MEDIA_TYPE);
		String expectedRow1 = "\n\t<film film_id=\"7\" title=\"AIRPLANE SIERRA\" year=\"2006\" language_id=\"1\" name=\"English\" />";
		String expectedRow2 = "\n\t<film film_id=\"8\" title=\"AIRPORT POLLOCK\" year=\"2006\" language_id=\"1\" name=\"English\" />";
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<readResponse xmlns=\"http://restsql.org/schema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://restsql.org/schema Response.xsd \">" + expectedRow1 + expectedRow2 + "\n</readResponse>", results);
		// Reset these to the default of false for when multiple tests are run sequentially
		XmlResponseSerializer.setUseXmlDirective(false);
		XmlResponseSerializer.setUseXmlSchema(false);
	}
}
