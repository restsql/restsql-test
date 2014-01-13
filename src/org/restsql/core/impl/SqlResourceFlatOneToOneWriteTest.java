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

public class SqlResourceFlatOneToOneWriteTest extends SqlResourceTestBase {

	@Before
	public void setUp() throws SQLException, SqlResourceException{
		super.setUp();
		final Statement statement = connection.createStatement();
		statement
				.execute("INSERT INTO film (film_id,title,release_year,language_id,rental_duration,rental_rate,replacement_cost)"
						+ " VALUES (5000,'ESCAPE FROM TOMORROW',2011,1,0,0,0)");
		statement
				.execute("INSERT INTO film (film_id,title,release_year,language_id,rental_duration,rental_rate,replacement_cost)"
						+ " VALUES (5001,'BLOOD PURPLE',2012,1,0,0,0)");
		statement
				.execute("INSERT INTO film (film_id,title,release_year,language_id,rental_duration,rental_rate,replacement_cost)"
						+ " VALUES (5002,'THE DARKENING',2012,1,0,0,0)");
		statement.execute("INSERT INTO film_rating (film_rating_id,film_id,stars) VALUES (1,5000,5)");
		statement.execute("INSERT INTO film_rating (film_rating_id,film_id,stars) VALUES (2,5001,1)");
		statement.execute("INSERT INTO film_rating (film_rating_id,film_id,stars) VALUES (3,5002,1)");
		statement.close();
		sqlResource = Factory.getSqlResource("FlatOneToOne");
	}

	@After
	public void tearDown() throws SQLException {
		super.tearDown();
		final Statement statement = connection.createStatement();
		statement.execute("DELETE FROM film_rating");
		statement.execute("DELETE FROM film WHERE film_id between 5000 and 5500");
		statement.close();
	}

	@Test
	public void testExecDelete_Flat_MultiRow() throws SqlResourceFactoryException, SqlResourceException, InvalidRequestException {
		// Delete test fixture
		Request request = RequestFactoryHelper.getRequest(Request.Type.DELETE, sqlResource.getName(), null, new String[] { "year",
				"2012" });
		final int rowsAffected = sqlResource.write(request).getRowsAffected();
		assertEquals(2, rowsAffected);

		// Verify one preserved
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), new String[] { "film_id", "5000" },
				null);
		final List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals(1, results.size());
	}

	@Test
	public void testExecDelete_Flat_SingleRow() throws SqlResourceFactoryException, SqlResourceException, InvalidRequestException {
		// Update test fixture
		Request request = RequestFactoryHelper.getRequest(Request.Type.DELETE, sqlResource.getName(), new String[] { "film_id",
				"5000" }, null);
		final int rowsAffected = sqlResource.write(request).getRowsAffected();
		assertEquals(2, rowsAffected);

		// Verify updates
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), new String[] { "film_id", "5000" },
				null);
		final List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals(0, results.size());
	}

	@Test
	public void testExecInsert_Flat_SingleRow() throws SqlResourceFactoryException, SqlResourceException, InvalidRequestException {
		// Update test fixture
		Request request = RequestFactoryHelper.getRequest(Request.Type.INSERT, sqlResource.getName(), null, new String[] {
				"film_id", "5003", "title", "BLESSED SUN", "year", "2011", "language_id", "1", "rental_duration", "0",
				"rental_rate", "0", "replacement_cost", "0", "film_rating_id", "4", "stars", "5" });
		final int rowsAffected = sqlResource.write(request).getRowsAffected();
		assertEquals(2, rowsAffected);

		// Verify updates
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), new String[] { "film_id", "5003" },
				null);
		final List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals(1, results.size());
		AssertionHelper.assertFilmRating(results.get(0), 5003, "BLESSED SUN", 2011, 4, 5);
	}

	@Test
	public void testExecUpdate_Flat_MultiRow() throws SqlResourceFactoryException, SqlResourceException, InvalidRequestException {
		// Update test fixture
		Request request = RequestFactoryHelper.getRequest(Request.Type.UPDATE, sqlResource.getName(), new String[] { "year", "2012",
				"stars", "1" }, new String[] { "year", "2013", "stars", "5" });
		final int rowsAffected = sqlResource.write(request).getRowsAffected();
		assertEquals(4, rowsAffected);

		// Verify updates
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), null, new String[] { "stars", "5" });
		List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals(3, results.size());
		AssertionHelper.assertFilmRating(results.get(0), 5000, "ESCAPE FROM TOMORROW", 2011, 1, 5);
		AssertionHelper.assertFilmRating(results.get(1), 5001, "BLOOD PURPLE", 2013, 2, 5);
		AssertionHelper.assertFilmRating(results.get(2), 5002, "THE DARKENING", 2013, 3, 5);
	}

	@Test
	public void testExecUpdate_Flat_SingleRow() throws SqlResourceFactoryException, SqlResourceException, InvalidRequestException {
		// Update test fixture
		Request request = RequestFactoryHelper.getRequest(Request.Type.UPDATE, sqlResource.getName(), new String[] { "film_id",
				"5000" }, new String[] { "year", "2010", "title", "ESCAPE FROM YESTERDAY", "stars", "2" });
		final int rowsAffected = sqlResource.write(request).getRowsAffected();
		assertEquals(2, rowsAffected);

		// Verify updates
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), new String[] { "film_id", "5000" },
				null);
		final List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals(1, results.size());
		AssertionHelper.assertFilmRating(results.get(0), 5000, "ESCAPE FROM YESTERDAY", 2010, 1, 2);
	}
}
