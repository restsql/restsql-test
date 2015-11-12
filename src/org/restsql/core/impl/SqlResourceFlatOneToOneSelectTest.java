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
import org.restsql.core.Factory.SqlResourceFactoryException;
import org.restsql.core.InvalidRequestException;
import org.restsql.core.Request;
import org.restsql.core.RequestFactoryHelper;
import org.restsql.core.SqlResourceException;

public class SqlResourceFlatOneToOneSelectTest extends SqlResourceTestBase {

	@Before
	public void setUp() throws SQLException, SqlResourceException {
		super.setUp();
		final Statement statement = connection.createStatement();
		statement
				.execute("INSERT INTO film (film_id,title,release_year,language_id,rental_duration,rental_rate,replacement_cost)"
						+ " VALUES (5000,'ESCAPE FROM TOMORROW',2011,1,0,0,0)");
		statement
				.execute("INSERT INTO film (film_id,title,release_year,language_id,rental_duration,rental_rate,replacement_cost)"
						+ " VALUES (5001,'BLOOD PURPLE',2012,1,0,0,0)");
		statement
				.execute("INSERT INTO film (film_id,title,description,release_year,language_id,rental_duration,rental_rate,replacement_cost)"
						+ " VALUES (5002,'THE DARKENING','A dark tale...',2012,1,0,0,0)");
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
	public void testExecSelect_ParameterQueryWithValue() throws SqlResourceFactoryException, SqlResourceException,
			InvalidRequestException {
		// Verify select by nullable attribute with value
		Request request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), null,
				new String[] {"description", "A dark tale..."});
		final List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals("result size", 1, results.size());
		assertEquals("id", 5002, ((Integer)results.get(0).get("film_id")).intValue());
	}

	@Test
	public void testExecSelect_ParameterQueryWithNotNull() throws SqlResourceFactoryException, SqlResourceException,
			InvalidRequestException {
		// Verify select by nullable attribute with value
		Request request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), null,
				new String[] {"description", "!null"});
		final List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals("result size", 1, results.size());
		assertEquals("id", 5002, ((Integer)results.get(0).get("film_id")).intValue());
	}

	@Test
	public void testExecSelect_ParameterQueryWithNull() throws SqlResourceFactoryException, SqlResourceException,
			InvalidRequestException {
		// Verify select by nullable attribute with value
		Request request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), null,
				new String[] {"description", null});
		final List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals(2, results.size());
		assertEquals("id", 5000, ((Integer)results.get(0).get("film_id")).intValue());
		assertEquals("id", 5001, ((Integer)results.get(1).get("film_id")).intValue());
	}
}
