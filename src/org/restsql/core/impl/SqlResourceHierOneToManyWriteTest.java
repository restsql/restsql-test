/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.core.impl;

import static junit.framework.Assert.assertEquals;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restsql.core.Factory;
import org.restsql.core.InvalidRequestException;
import org.restsql.core.Request;
import org.restsql.core.RequestFactoryHelper;
import org.restsql.core.SqlResourceException;
import org.restsql.core.Factory.SqlResourceFactoryException;

public class SqlResourceHierOneToManyWriteTest extends SqlResourceTestBase {

	@Before
	public void setUp() throws SQLException, SqlResourceException{
		super.setUp();
		sqlResource = Factory.getSqlResource("HierOneToMany");
		final Statement statement = connection.createStatement();
		statement.execute("INSERT INTO language (language_id,name) VALUES (100,'New Esperanto')");
		statement.execute("INSERT INTO language (language_id,name) VALUES (101,'New Greek')");
		statement.execute("INSERT INTO language (language_id,name) VALUES (102,'New Latin')");
		statement.execute("INSERT INTO language (language_id,name) VALUES (103,'Old Latin')");
		statement
				.execute("INSERT INTO film (film_id,title,release_year,language_id,rental_duration,rental_rate,replacement_cost)"
						+ " VALUES (5000,'ESCAPE FROM TOMORROW',2011,100,0,0,0)");
		statement
				.execute("INSERT INTO film (film_id,title,release_year,language_id,rental_duration,rental_rate,replacement_cost)"
						+ " VALUES (5001,'BLOOD PURPLE',2012,101,0,0,0)");
		statement
				.execute("INSERT INTO film (film_id,title,release_year,language_id,rental_duration,rental_rate,replacement_cost)"
						+ " VALUES (5002,'THE DARKENING',2012,101,0,0,0)");
		// No films for New or Old Latin
		statement.close();
	}

	@After
	public void tearDown() throws SQLException {
		super.tearDown();
		final Statement statement = connection.createStatement();
		statement.execute("DELETE FROM film WHERE film_id between 5000 and 5500");
		statement.execute("DELETE FROM language WHERE language_id between 100 and 150");
		statement.close();
	}

	@Test
	public void testExecDelete_Parent_SingleRow_NoChildren() throws SqlResourceFactoryException,
			SqlResourceException, InvalidRequestException {
		// Update test fixture
		Request request = RequestFactoryHelper.getRequest(Request.Type.DELETE, sqlResource.getName(), new String[] {
				"language_id", "102" }, null);
		final int rowsAffected = sqlResource.write(request);
		assertEquals(1, rowsAffected);

		// Verify update
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), new String[] {
				"language_id", "102" }, null);
		final List<Map<String, Object>> results = sqlResource.readAsCollection(request);
		assertEquals(0, results.size());
	}

	@Test
	public void testExecDelete_Parent_SingleRow_WithChildren() throws SqlResourceFactoryException,
			SqlResourceException, InvalidRequestException {
		// Update test fixture
		Request request = RequestFactoryHelper.getRequest(Request.Type.DELETE, sqlResource.getName(), new String[] {
				"language_id", "101" }, null);
		final int rowsAffected = sqlResource.write(request);
		assertEquals(3, rowsAffected);

		// Verify update
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), new String[] {
				"language_id", "101" }, null);
		final List<Map<String, Object>> results = sqlResource.readAsCollection(request);
		assertEquals(0, results.size());
	}

	@Test(expected = SqlResourceException.class)
	public void testExecDelete_Parent_MultiRow_WithChildren() throws SqlResourceFactoryException,
			SqlResourceException, InvalidRequestException {
		// Update test fixture
		Request request = RequestFactoryHelper.getRequest(Request.Type.DELETE, sqlResource.getName(), null, new String[] {
				"langName", "New%" }, null);
		sqlResource.write(request);
	}

	@Test
	public void testExecDelete_Parent_MultiRow_NoChildren() throws SqlResourceFactoryException, SqlResourceException,
			InvalidRequestException {
		// Update test fixture
		Request request = RequestFactoryHelper.getRequest(Request.Type.DELETE, sqlResource.getName(), null, new String[] {
				"langName", "%Latin" }, null);
		int rowsAffected = sqlResource.write(request);
		assertEquals(2, rowsAffected);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExecDelete_Children_MultiRow() throws SqlResourceFactoryException, SqlResourceException,
			InvalidRequestException {
		// Update test fixture
		Request request = RequestFactoryHelper
				.getRequest(Request.Type.DELETE, sqlResource.getName(),
						new String[] { "language_id", "101" }, null, new String[][] { { "film_id", "5001" },
								{ "film_id", "5002" } });
		final int rowsAffected = sqlResource.write(request);
		assertEquals(2, rowsAffected);

		// Verify update
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), new String[] {
				"language_id", "101" }, null);
		final List<Map<String, Object>> results = sqlResource.readAsCollection(request);
		assertEquals(1, results.size());
		List<Map<String, Object>> childRows = (List<Map<String, Object>>) results.get(0).get("movies");
		assertEquals(0, childRows.size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExecInsert_Children_MultiRow() throws SqlResourceFactoryException, SqlResourceException,
			InvalidRequestException {
		// Update test fixture
		Request request = RequestFactoryHelper.getRequest(Request.Type.INSERT, sqlResource.getName(), new String[] {
				"language_id", "102" }, null, new String[][] { { "film_id", "5003", "title", "BLESSED SUN",
				"year", "2011", "rental_duration", "0", "rental_rate", "0", "replacement_cost", "0" } });
		int rowsAffected = sqlResource.write(request);
		assertEquals(1, rowsAffected);
		request = RequestFactoryHelper.getRequest(Request.Type.INSERT, sqlResource.getName(), new String[] {
				"language_id", "102" }, null, new String[][] { { "film_id", "5004", "title", "WICKED SUN",
				"year", "2011", "rental_duration", "0", "rental_rate", "0", "replacement_cost", "0" } });
		rowsAffected = sqlResource.write(request);
		assertEquals(1, rowsAffected);

		// Verify update
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), new String[] {
				"language_id", "102" }, null);
		final List<Map<String, Object>> results = sqlResource.readAsCollection(request);
		assertEquals(1, results.size());
		List<Map<String, Object>> childRows = (List<Map<String, Object>>) results.get(0).get("movies");
		AssertionHelper.assertFilmBasics(childRows, 5003, "BLESSED SUN", 2011);
		AssertionHelper.assertFilmBasics(childRows, 5004, "WICKED SUN", 2011);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExecUpdate_Parent_SingleRow() throws SqlResourceFactoryException, SqlResourceException,
			InvalidRequestException {
		// Update test fixture
		Request request = RequestFactoryHelper.getRequest(Request.Type.UPDATE, sqlResource.getName(), new String[] {
				"language_id", "100" }, new String[] { "langName", "Greater Esperanto" });
		final int rowsAffected = sqlResource.write(request);
		assertEquals(1, rowsAffected);

		// Verify update
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), new String[] {
				"language_id", "100" }, null);
		final List<Map<String, Object>> results = sqlResource.readAsCollection(request);
		assertEquals(1, results.size());
		AssertionHelper.assertLanguageHierarchical(results.get(0), 100, "Greater Esperanto");
		List<Map<String, Object>> childRows = (List<Map<String, Object>>) results.get(0).get("movies");
		assertEquals(1, childRows.size());
		AssertionHelper.assertFilmBasics(childRows.get(0), 5000, "ESCAPE FROM TOMORROW", 2011);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExecUpdate_Children_SingleRow() throws SqlResourceFactoryException, SqlResourceException,
			InvalidRequestException {
		// Update test fixture
		Request request = RequestFactoryHelper
				.getRequest(Request.Type.UPDATE, sqlResource.getName(),
						new String[] { "language_id", "101" }, null, new String[][] { { "film_id", "5001",
								"title", "BLOOD BLUE" } });
		final int rowsAffected = sqlResource.write(request);
		assertEquals(1, rowsAffected);

		// Verify update
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), new String[] {
				"language_id", "101" }, null);
		final List<Map<String, Object>> results = sqlResource.readAsCollection(request);
		assertEquals(1, results.size());
		AssertionHelper.assertLanguageHierarchical(results.get(0), 101, "New Greek");
		List<Map<String, Object>> childRows = (List<Map<String, Object>>) results.get(0).get("movies");
		assertEquals(2, childRows.size());
		AssertionHelper.assertFilmBasics(childRows.get(0), 5001, "BLOOD BLUE", 2012);
		AssertionHelper.assertFilmBasics(childRows.get(1), 5002, "THE DARKENING", 2012);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExecInsert_Parent() throws SqlResourceFactoryException, SqlResourceException,
			InvalidRequestException {
		// Update test fixture
		Request request = RequestFactoryHelper.getRequest(Request.Type.INSERT, sqlResource.getName(), null, new String[] {
				"language_id", "104", "langName", "Old Esperanto" });
		final int rowsAffected = sqlResource.write(request);
		assertEquals(1, rowsAffected);

		// Verify update
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), new String[] {
				"language_id", "104" }, null);
		final List<Map<String, Object>> results = sqlResource.readAsCollection(request);
		assertEquals(1, results.size());
		AssertionHelper.assertLanguageHierarchical(results.get(0), 104, "Old Esperanto");
		List<Map<String, Object>> childRows = (List<Map<String, Object>>) results.get(0).get("movies");
		assertEquals(0, childRows.size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExecUpdate_Parent_MultiRow() throws SqlResourceFactoryException, SqlResourceException,
			InvalidRequestException {
		// Update test fixture
		Request request = RequestFactoryHelper.getRequest(Request.Type.UPDATE, sqlResource.getName(), new String[] {
				"langName", "New%" }, new String[] { "langName", "Unspeakable" });
		final int rowsAffected = sqlResource.write(request);
		assertEquals(3, rowsAffected);

		// Verify updates
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), new String[] { "langName",
				"Unspeakable" }, null);
		final List<Map<String, Object>> results = sqlResource.readAsCollection(request);
		assertEquals(3, results.size());

		// Assert film 1
		AssertionHelper.assertLanguageHierarchical(results.get(0), 100, "Unspeakable");
		List<Map<String, Object>> childRows = (List<Map<String, Object>>) results.get(0).get("movies");
		assertEquals(1, childRows.size());
		AssertionHelper.assertFilmBasics(childRows.get(0), 5000, "ESCAPE FROM TOMORROW", 2011);
		// Assert film 2
		AssertionHelper.assertLanguageHierarchical(results.get(1), 101, "Unspeakable");
		childRows = (List<Map<String, Object>>) results.get(1).get("movies");
		assertEquals(2, childRows.size());
		AssertionHelper.assertFilmBasics(childRows.get(0), 5001, "BLOOD PURPLE", 2012);
		AssertionHelper.assertFilmBasics(childRows.get(1), 5002, "THE DARKENING", 2012);
		// Assert film 3
		AssertionHelper.assertLanguageHierarchical(results.get(2), 102, "Unspeakable");
		childRows = (List<Map<String, Object>>) results.get(2).get("movies");
		assertEquals(0, childRows.size());
	}
}
