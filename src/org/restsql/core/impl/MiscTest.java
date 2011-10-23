/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.core.impl;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restsql.core.Factory;
import org.restsql.core.Request;
import org.restsql.core.RequestFactoryHelper;
import org.restsql.core.SqlResourceException;

public class MiscTest extends SqlResourceTestBase {
	private static final String SOME_TIMESTAMP = "2011-07-23 16:54:00";
	
	@Override
	@Before
	public void setUp() throws SQLException, SqlResourceException {
		super.setUp();
		sqlResource = Factory.getSqlResource("TestTimestamp");
		final Statement statement = connection.createStatement();
		statement.execute("INSERT INTO test_timestamp (id, time) VALUES (1000, 0)");
		statement.execute("INSERT INTO test_timestamp (id, time) VALUES (1001, '" + SOME_TIMESTAMP + "')");
		statement.close();
	}

	@Override
	@After
	public void tearDown() throws SQLException {
		super.tearDown();
		final Statement statement = connection.createStatement();
		statement.execute("DELETE FROM test_timestamp");
		statement.close();
	}

	@Test
	public void testNullTimestamp() throws SqlResourceException, ParseException {
		final Request request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(),
				null, null);
		final List<Map<String, Object>> results = sqlResource.readAsCollection(request);
		assertEquals(2, results.size());
		AssertionHelper.assertTestTimestamp(results.get(0), 1000, null);
		AssertionHelper.assertTestTimestamp(results.get(1), 1001, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.parse(SOME_TIMESTAMP));
	}
}
