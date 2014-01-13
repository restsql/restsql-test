/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.core.impl;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restsql.core.Config;
import org.restsql.core.Factory;
import org.restsql.core.Request;
import org.restsql.core.RequestFactoryHelper;
import org.restsql.core.SqlResourceException;

public class DateTimeTest extends SqlResourceTestBase {
	private static final String SOME_DATE = "2011-07-23";
	private static final String SOME_TIME = "16:54:00";
	private static final String SOME_TIMESTAMP = "2011-07-23 16:54:00";
	private static final String SOME_OTHER_DATE = "1980-01-01";
	private static final String SOME_OTHER_TIME = "12:00:00";
	private static final String SOME_OTHER_TIMESTAMP = "1980-01-01 12:00:00";
	private boolean usingMysql;

	@Override
	@Before
	public void setUp() throws SQLException, SqlResourceException {
		super.setUp();
		usingMysql = Config.properties.getProperty(Config.KEY_DATABASE_URL, null).contains("mysql");
		sqlResource = Factory.getSqlResource("DateTime");
		final Statement statement = connection.createStatement();
		statement.execute("INSERT INTO datetime (id, time, timestamp, date, datetime) VALUES (1000, '"
				+ SOME_TIME + "', '" + SOME_TIMESTAMP + "', '" + SOME_DATE + "', '" + SOME_TIMESTAMP + "')");
		statement
				.execute("INSERT INTO datetime (id, time, timestamp, date, datetime) VALUES (1001, NULL, NULL, NULL, NULL)");
		statement.close();
	}

	@Override
	@After
	public void tearDown() throws SQLException {
		super.tearDown();
		final Statement statement = connection.createStatement();
		statement.execute("DELETE FROM datetime");
		statement.close();
	}

	@Test
	public void testDelete() throws SqlResourceException, ParseException {
		final Request request = RequestFactoryHelper.getRequest(Request.Type.DELETE, sqlResource.getName(),
				new String[] { "timestamp", SOME_TIMESTAMP }, null);

		int rowsAffected = sqlResource.write(request).getRowsAffected();
		assertEquals(1, rowsAffected);
	}

	@Test
	public void testInsert() throws SqlResourceException, ParseException {
		Request request = RequestFactoryHelper.getRequest(Request.Type.INSERT, sqlResource.getName(), null,
				new String[] { "id", "1003", "time", SOME_TIME, "timestamp", SOME_TIMESTAMP, "date",
						SOME_DATE, "datetime", SOME_TIMESTAMP });
		int rowsAffected = sqlResource.write(request).getRowsAffected();
		assertEquals(1, rowsAffected);

		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), new String[] {
				"id", "1003" }, null);
		final List<Map<String, Object>> results = sqlResource.read(request);
		assertTrue(results.size() == 1);
		AssertionHelper.assertDateTime(results.get(0), 1003, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.parse(SOME_TIMESTAMP));
	}

	@Test
	public void testSelect() throws SqlResourceException, ParseException {
		final Request request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(),
				null, null);
		final List<Map<String, Object>> results = sqlResource.read(request);
		assertTrue(results.size() > 0);
		AssertionHelper.assertDateTime(results.get(0), 1000, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.parse(SOME_TIMESTAMP));
	}

	@Test
	public void testSelectNullTimestamp() throws SqlResourceException, ParseException, SQLException {
		if (usingMysql) {
			final Statement statement = connection.createStatement();
			statement
					.execute("INSERT INTO datetime (id, time, timestamp, date, datetime) VALUES (1002, 0, 0, 0, 0)");
			statement.close();
		}
		final Request request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(),
				null, null);
		final List<Map<String, Object>> results = sqlResource.read(request);
		assertTrue(results.size() > 0);

		// Assert the null row
		Map<String, Object> row = results.get(1);
		assertEquals(new Integer(1001), row.get("id"));
		if (usingMysql) {
			assertNull(row.get("time"));
			assertNotNull(row.get("timestamp"));
		} else {
			assertNull(row.get("time"));
			assertNull(row.get("timestamp"));
		}
		assertNull(row.get("date"));
		assertNull(row.get("datetime"));

		// Assert the zero row
		if (usingMysql) {
			row = results.get(2);
			assertEquals(new Integer(1002), row.get("id"));
			assertEquals(new SimpleDateFormat("HH:mm:ss").parse("00:00:00"), row.get("time"));
			assertNull(row.get("timestamp"));
			assertNull(row.get("date"));
			assertNull(row.get("datetime"));
		}
	}

	@Test
	public void testUpdate() throws SqlResourceException, ParseException {
		Request request = RequestFactoryHelper.getRequest(Request.Type.UPDATE, sqlResource.getName(),
				new String[] { "timestamp", SOME_TIMESTAMP }, new String[] { "time", SOME_OTHER_TIME,
						"timestamp", SOME_OTHER_TIMESTAMP, "date", SOME_OTHER_DATE, "datetime", SOME_OTHER_TIMESTAMP });
		int rowsAffected = sqlResource.write(request).getRowsAffected();
		RequestFactoryHelper.logRequest();
		assertEquals(1, rowsAffected);
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), new String[] {
				"id", "1000" }, null);
		final List<Map<String, Object>> results = sqlResource.read(request);
		RequestFactoryHelper.logRequest();
		assertEquals(1, results.size());
		AssertionHelper.assertDateTime(results.get(0), 1000, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.parse(SOME_OTHER_TIMESTAMP));
	}
}
