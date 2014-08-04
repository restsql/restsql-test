/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.core.impl;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restsql.core.Factory;
import org.restsql.core.Factory.SqlResourceFactoryException;
import org.restsql.core.InvalidRequestException;
import org.restsql.core.Request;
import org.restsql.core.RequestFactoryHelper;
import org.restsql.core.SqlResourceException;
import org.restsql.core.WriteResponse;

public class SqlResourceHierOneToManyWriteTest extends SqlResourceTestBase {

	@Override
	@Before
	public void setUp() throws SQLException, SqlResourceException {
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

	@Override
	@After
	public void tearDown() throws SQLException {
		super.tearDown();
		final Statement statement = connection.createStatement();
		statement.execute("DELETE FROM film WHERE film_id between 5000 and 5500");
		statement.execute("DELETE FROM language WHERE language_id between 100 and 150");
		statement.close();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExecDelete_Children_MultiRow() throws SqlResourceFactoryException, SqlResourceException,
			InvalidRequestException {
		// Update test fixture
		Request request = RequestFactoryHelper.getRequest(Request.Type.DELETE, sqlResource.getName(),
				new String[] { "langId", "101" }, null, new String[][] { { "film_id", "5001" },
						{ "film_id", "5002" } });
		final int rowsAffected = sqlResource.write(request).getRowsAffected();
		assertEquals(2, rowsAffected);

		// Verify update
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), new String[] {
				"langId", "101" }, null);
		final List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals(1, results.size());
		final List<Map<String, Object>> childRows = (List<Map<String, Object>>) results.get(0).get("movies");
		assertEquals(0, childRows.size());
	}

	@Test
	public void testExecDelete_Parent_MultiRow_NoChildren() throws SqlResourceFactoryException,
			SqlResourceException, InvalidRequestException {
		// Update test fixture
		final Request request = RequestFactoryHelper.getRequest(Request.Type.DELETE, sqlResource.getName(),
				null, new String[] { "langName", "%Latin" }, null);
		final int rowsAffected = sqlResource.write(request).getRowsAffected();
		assertEquals(2, rowsAffected);
	}

	@Test(expected = SqlResourceException.class)
	public void testExecDelete_Parent_MultiRow_WithChildren() throws SqlResourceFactoryException,
			SqlResourceException, InvalidRequestException {
		// Update test fixture
		final Request request = RequestFactoryHelper.getRequest(Request.Type.DELETE, sqlResource.getName(),
				null, new String[] { "langName", "New%" }, null);
		sqlResource.write(request).getRowsAffected();
	}

	@Test
	public void testExecDelete_Parent_SingleRow_NoChildren() throws SqlResourceFactoryException,
			SqlResourceException, InvalidRequestException {
		// Update test fixture
		Request request = RequestFactoryHelper.getRequest(Request.Type.DELETE, sqlResource.getName(),
				new String[] { "langId", "102" }, null);
		final int rowsAffected = sqlResource.write(request).getRowsAffected();
		assertEquals(1, rowsAffected);

		// Verify update
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), new String[] {
				"langId", "102" }, null);
		final List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals(0, results.size());
	}

	@Test
	public void testExecDelete_Parent_SingleRow_WithChildren() throws SqlResourceFactoryException,
			SqlResourceException, InvalidRequestException {
		// Update test fixture
		Request request = RequestFactoryHelper.getRequest(Request.Type.DELETE, sqlResource.getName(),
				new String[] { "langId", "101" }, null);
		final int rowsAffected = sqlResource.write(request).getRowsAffected();
		assertEquals(3, rowsAffected);

		// Verify update
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), new String[] {
				"langId", "101" }, null);
		final List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals(0, results.size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExecInsert_Children_MultiRow_WithSequence() throws SqlResourceFactoryException,
			SqlResourceException, InvalidRequestException {
		// Reset sequence
		Factory.getSequenceManager().setNextValue(connection, "film", "film_film_id_seq", 5003, false);

		// Insert test fixture
		Request request = RequestFactoryHelper.getRequest(Request.Type.INSERT, sqlResource.getName(),
				new String[] { "langId", "102" }, null, new String[][] {
						{ "title", "BLESSED SUN", "year", "2011", "rental_duration", "0", "rental_rate", "0",
								"replacement_cost", "0" },
						{ "title", "WICKED SUN", "year", "2011", "rental_duration", "0", "rental_rate", "0",
								"replacement_cost", "0" } });
		final WriteResponse response = sqlResource.write(request);
		assertEquals(2, response.getRowsAffected());
		AssertionHelper.assertResponse(request, 2, new Object[] {
				"langId",
				new Integer(102),
				"movies",
				new Object[][] {
						{ "film_id", new Integer(5003), "title", "BLESSED SUN", "year", new Integer(2011) },
						{ "film_id", new Integer(5004), "title", "WICKED SUN", "year", new Integer(2011) } } },
				response);

		// Verify insert
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), new String[] {
				"langId", "102" }, null);
		final List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals(1, results.size());
		final List<Map<String, Object>> childRows = (List<Map<String, Object>>) results.get(0).get("movies");
		AssertionHelper.assertFilmBasics(childRows, 5003, "BLESSED SUN", 2011);
		AssertionHelper.assertFilmBasics(childRows, 5004, "WICKED SUN", 2011);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExecInsert_Children_MultiRow_WithoutSequence() throws SqlResourceFactoryException,
			SqlResourceException, InvalidRequestException {
		// Insert test fixture
		Request request = RequestFactoryHelper.getRequest(Request.Type.INSERT, sqlResource.getName(),
				new String[] { "langId", "102" }, null, new String[][] {
						{ "film_id", "5003", "title", "BLESSED SUN", "year", "2011", "rental_duration", "0",
								"rental_rate", "0", "replacement_cost", "0" },
						{ "film_id", "5004", "title", "WICKED SUN", "year", "2011", "rental_duration", "0",
								"rental_rate", "0", "replacement_cost", "0" } });
		final WriteResponse response = sqlResource.write(request);
		assertEquals(2, response.getRowsAffected());
		AssertionHelper.assertResponse(request, 2, new Object[] {
				"langId",
				new Integer(102),
				"movies",
				new Object[][] { { "film_id", new Integer(5003), "title", "BLESSED SUN", "year", new Integer(2011) },
						{ "film_id", new Integer(5004), "title", "WICKED SUN", "year", new Integer(2011) } } }, response);

		// Verify insert
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), new String[] {
				"langId", "102" }, null);
		final List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals(1, results.size());
		final List<Map<String, Object>> childRows = (List<Map<String, Object>>) results.get(0).get("movies");
		AssertionHelper.assertFilmBasics(childRows, 5003, "BLESSED SUN", 2011);
		AssertionHelper.assertFilmBasics(childRows, 5004, "WICKED SUN", 2011);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExecInsert_Children_MultiRow_WithSeqeunce() throws SqlResourceFactoryException,
			SqlResourceException, InvalidRequestException {
		// Reset sequence
		Factory.getSequenceManager().setNextValue(connection, "film", "film_film_id_seq", 5003, false);

		// Insert test fixture
		Request request = RequestFactoryHelper.getRequest(Request.Type.INSERT, sqlResource.getName(),
				new String[] { "langId", "102" }, null, new String[][] {
						{ "title", "BLESSED SUN", "year", "2011", "rental_duration", "0", "rental_rate", "0",
								"replacement_cost", "0" },
						{ "title", "WICKED SUN", "year", "2011", "rental_duration", "0", "rental_rate", "0",
								"replacement_cost", "0" } });
		final WriteResponse response = sqlResource.write(request);
		assertEquals(2, response.getRowsAffected());
		AssertionHelper.assertResponse(request, 2, new Object[] {
				"langId",
				new Integer(102),
				"movies",
				new Object[][] {
						{ "film_id", Integer.valueOf(5003), "title", "BLESSED SUN", "year", new Integer(2011) },
						{ "film_id", Integer.valueOf(5004), "title", "WICKED SUN", "year", new Integer(2011) } } },
				response);

		// Verify insert
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), new String[] {
				"langId", "102" }, null);
		final List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals(1, results.size());
		final List<Map<String, Object>> childRows = (List<Map<String, Object>>) results.get(0).get("movies");
		AssertionHelper.assertFilmBasics(childRows, 5003, "BLESSED SUN", 2011);
		AssertionHelper.assertFilmBasics(childRows, 5004, "WICKED SUN", 2011);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExecInsert_Parent_WithoutSequence() throws SqlResourceFactoryException,
			SqlResourceException, InvalidRequestException {
		// Insert test fixture
		Request request = RequestFactoryHelper.getRequest(Request.Type.INSERT, sqlResource.getName(), null,
				new String[] { "langId", "104", "langName", "Old Esperanto" });
		final WriteResponse response = sqlResource.write(request);
		assertEquals(1, response.getRowsAffected());
		AssertionHelper.assertResponse(request, 1, new Object[] { "langId", new Integer(104), "langName",
				"Old Esperanto" }, response);

		// Verify insert
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), new String[] {
				"langId", "104" }, null);
		final List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals(1, results.size());
		AssertionHelper.assertLanguageHierarchical(results.get(0), 104, "Old Esperanto");
		final List<Map<String, Object>> childRows = (List<Map<String, Object>>) results.get(0).get("movies");
		assertEquals(0, childRows.size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExecInsert_Parent_WithSequence() throws SqlResourceFactoryException,
			SqlResourceException, InvalidRequestException {
		// Reset sequence
		Factory.getSequenceManager().setNextValue(connection, "language", "language_language_id_seq", 104,
				false);

		// Insert test fixture
		Request request = RequestFactoryHelper.getRequest(Request.Type.INSERT, sqlResource.getName(), null,
				new String[] { "langName", "Old Esperanto" });
		final WriteResponse response = sqlResource.write(request);
		assertEquals(1, response.getRowsAffected());
		AssertionHelper.assertResponse(request, 1, new Object[] { "langId", Integer.valueOf(104), "langName",
				"Old Esperanto" }, response);

		// Verify insert
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), new String[] {
				"langId", "104" }, null);
		final List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals(1, results.size());
		AssertionHelper.assertLanguageHierarchical(results.get(0), 104, "Old Esperanto");
		final List<Map<String, Object>> childRows = (List<Map<String, Object>>) results.get(0).get("movies");
		assertEquals(0, childRows.size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExecUpdate_Children_SingleRow() throws SqlResourceFactoryException, SqlResourceException,
			InvalidRequestException {
		// Update test fixture
		Request request = RequestFactoryHelper.getRequest(Request.Type.UPDATE, sqlResource.getName(),
				new String[] { "langId", "101" }, null, new String[][] { { "film_id", "5001", "title",
						"BLOOD BLUE" } });
		final int rowsAffected = sqlResource.write(request).getRowsAffected();
		assertEquals(1, rowsAffected);

		// Verify update
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), new String[] {
				"langId", "101" }, null);
		final List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals(1, results.size());
		AssertionHelper.assertLanguageHierarchical(results.get(0), 101, "New Greek");
		final List<Map<String, Object>> childRows = (List<Map<String, Object>>) results.get(0).get("movies");
		assertEquals(2, childRows.size());
		AssertionHelper.assertFilmBasics(childRows.get(0), 5001, "BLOOD BLUE", 2012);
		AssertionHelper.assertFilmBasics(childRows.get(1), 5002, "THE DARKENING", 2012);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExecUpdate_Parent_MultiRow() throws SqlResourceFactoryException, SqlResourceException,
			InvalidRequestException {
		// Update test fixture
		Request request = RequestFactoryHelper.getRequest(Request.Type.UPDATE, sqlResource.getName(),
				new String[] { "langName", "New%" }, new String[] { "langName", "Unspeakable" });
		final int rowsAffected = sqlResource.write(request).getRowsAffected();
		assertEquals(3, rowsAffected);

		// Verify updates
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), new String[] {
				"langName", "Unspeakable" }, null);
		final List<Map<String, Object>> results = sqlResource.read(request);
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

	@SuppressWarnings("unchecked")
	@Test
	public void testExecUpdate_Parent_SingleRow() throws SqlResourceFactoryException, SqlResourceException,
			InvalidRequestException {
		// Update test fixture
		Request request = RequestFactoryHelper.getRequest(Request.Type.UPDATE, sqlResource.getName(),
				new String[] { "langId", "100" }, new String[] { "langName", "Greater Esperanto" });
		final WriteResponse response = sqlResource.write(request);
		assertEquals("rows affected", 1, response.getRowsAffected());
		assertNull("null results", response.getRows());

		// Verify update
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), new String[] {
				"langId", "100" }, null);
		final List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals(1, results.size());
		AssertionHelper.assertLanguageHierarchical(results.get(0), 100, "Greater Esperanto");
		final List<Map<String, Object>> childRows = (List<Map<String, Object>>) results.get(0).get("movies");
		assertEquals(1, childRows.size());
		AssertionHelper.assertFilmBasics(childRows.get(0), 5000, "ESCAPE FROM TOMORROW", 2011);
	}
}
