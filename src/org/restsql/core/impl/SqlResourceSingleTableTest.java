/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.core.impl;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

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
import org.restsql.core.SqlBuilder;
import org.restsql.core.SqlResourceException;
import org.restsql.core.WriteResponse;

public class SqlResourceSingleTableTest extends SqlResourceTestBase {

	@Before
	public void setUp() throws SQLException, SqlResourceException {
		super.setUp();
		sqlResource = Factory.getSqlResource("SingleTable");
		final Statement statement = connection.createStatement();
		statement.execute("INSERT INTO actor (actor_id,first_name,last_name) VALUES (1000,'John','Smith')");
		statement.execute("INSERT INTO actor (actor_id,first_name,last_name) VALUES (1001,'Bob','Black')");
		statement.execute("INSERT INTO actor (actor_id,first_name,last_name) VALUES (1002,'Manuel','Black')");
		statement.close();
	}

	@After
	public void tearDown() throws SQLException {
		super.tearDown();
		final Statement statement = connection.createStatement();
		statement.execute("DELETE FROM actor WHERE actor_id between 1000 and 1100");
		statement.close();
	}

	@Test
	public void testExecDelete_MultiRow() throws SqlResourceException {
		// Update test fixture
		Request request = RequestFactoryHelper.getRequest(Request.Type.DELETE, sqlResource.getName(),
				new String[] { "surname", "Black" }, null);
		final WriteResponse response = sqlResource.write(request);
		assertEquals("rows affected", 2, response.getRowsAffected());
		assertNull("null results", response.getRows());

		// Verify deletes
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), null,
				new String[] { "surname", "Black" });
		final List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals(0, results.size());
	}

	@Test
	public void testExecDelete_SingleRow() throws SqlResourceException {
		// Update test fixture
		Request request = RequestFactoryHelper.getRequest(Request.Type.DELETE, sqlResource.getName(),
				new String[] { "id", "1000" }, null);
		final int rowsAffected = sqlResource.write(request).getRowsAffected();
		assertEquals(1, rowsAffected);

		// Verify updates
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), new String[] {
				"id", "1000" }, null);
		final List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals(0, results.size());
	}

	@Test
	public void testExecInsert_WithSequence() throws SqlResourceException {
		// Reset sequence
		Factory.getSequenceManager().setNextValue(connection, "actor", "actor_actor_id_seq", 1003, false);

		// Insert it
		Request request = RequestFactoryHelper.getRequest(Request.Type.INSERT, sqlResource.getName(), null,
				new String[] { "first_name", "Patty", "surname", "White" });
		final Map<String, SqlBuilder.SqlStruct> sql = Factory.getSqlBuilder().buildWriteSql(
				((SqlResourceImpl) sqlResource).getMetaData(), request, false);
		String qualifiedTableName = getQualifiedTableName("actor");

		assertEquals(
				"INSERT INTO " + qualifiedTableName + " (first_name,last_name) VALUES ('Patty','White')", sql
						.get(qualifiedTableName).getStatement());

		final WriteResponse response = sqlResource.write(request);
		AssertionHelper.assertResponse(request, 1, new Object[] { "id", Integer.valueOf(1003), "first_name",
				"Patty", "surname", "White" }, response);

		// Now select it
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), new String[] {
				"id", "1003" }, null);
		final List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals(1, results.size());
		AssertionHelper.assertActor(false, results.get(0), 1003, "Patty", "White");
	}

	@Test
	public void testExecInsert_WithoutSequence() throws SqlResourceException {
		// Insert it
		Request request = RequestFactoryHelper.getRequest(Request.Type.INSERT, sqlResource.getName(), null,
				new String[] { "id", "1003", "first_name", "Patty", "surname", "White" });
		final Map<String, SqlBuilder.SqlStruct> sql = Factory.getSqlBuilder().buildWriteSql(
				((SqlResourceImpl) sqlResource).getMetaData(), request, false);
		String qualifiedTableName = getQualifiedTableName("actor");

		assertEquals("INSERT INTO " + qualifiedTableName
				+ " (actor_id,first_name,last_name) VALUES (1003,'Patty','White')",
				sql.get(qualifiedTableName).getStatement());

		final WriteResponse response = sqlResource.write(request);
		AssertionHelper.assertResponse(request, 1, new Object[] { "id", new Integer(1003), "first_name", "Patty",
				"surname", "White" }, response);

		// Now select it
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), new String[] {
				"id", "1003" }, null);
		final List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals(1, results.size());
		AssertionHelper.assertActor(false, results.get(0), 1003, "Patty", "White");
	}

	@Test
	public void testExecUpdate_MultiRow() throws SqlResourceException {
		// Update test fixture
		Request request = RequestFactoryHelper.getRequest(Request.Type.UPDATE, sqlResource.getName(),
				new String[] { "surname", "Black" }, new String[] { "first_name", "Robert", });
		final WriteResponse response = sqlResource.write(request);
		assertEquals("rows affected", 2, response.getRowsAffected());
		assertNull("null results", response.getRows());

		// Verify updates
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), null,
				new String[] { "surname", "Black" });
		final List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals(2, results.size());
		AssertionHelper.assertActor(false, results.get(0), 1001, "Robert", "Black");
		AssertionHelper.assertActor(false, results.get(1), 1002, "Robert", "Black");
	}

	@Test
	public void testExecUpdate_SingleRow() throws SqlResourceException {
		// Update test fixture
		Request request = RequestFactoryHelper
				.getRequest(Request.Type.UPDATE, sqlResource.getName(), new String[] { "id", "1000" },
						new String[] { "first_name", "Marcus", "surname", "Aurelius" });
		final int rowsAffected = sqlResource.write(request).getRowsAffected();
		assertEquals(1, rowsAffected);

		// Verify updates
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), new String[] {
				"id", "1000" }, null);
		final List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals(1, results.size());
		AssertionHelper.assertActor(false, results.get(0), 1000, "Marcus", "Aurelius");
	}

	@Test
	public void testExecUpdate_SingleRow_WithNoUpdateParameters() throws SqlResourceException {
		try {
			RequestFactoryHelper.getRequest(Request.Type.UPDATE, sqlResource.getName(), new String[] { "id",
					"1000" }, null);
			fail("SqlResourceException expected for no update parameters");
		} catch (InvalidRequestException exception) {
			assertEquals(InvalidRequestException.MESSSAGE_UPDATE_MISSING_PARAMS, exception.getMessage());
		}
	}

	@Test
	public void testExecSelect_SingleRow_ResId() throws SqlResourceException {
		Request request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(),
				new String[] { "id", "1000" }, null);
		final List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals(1, results.size());
		AssertionHelper.assertActor(false, results.get(0), 1000, "John", "Smith");
	}

	@Test
	public void testExecSelect_MultiRow_Param() throws SqlResourceException {
		Request request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), null,
				new String[] { "surname", "Black" });
		final List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals(2, results.size());
		AssertionHelper.assertActor(false, results.get(0), 1001, "Bob", "Black");
		AssertionHelper.assertActor(false, results.get(1), 1002, "Manuel", "Black");
	}

	@Test
	public void testExecSelect_SingleRow_Param() throws SqlResourceException {
		Request request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), null,
				new String[] { "first_name", "Manuel" });
		List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals(1, results.size());
		AssertionHelper.assertActor(false, results.get(0), 1002, "Manuel", "Black");

		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), null,
				new String[] { "id", "1002" });
		results = sqlResource.read(request);
		assertEquals(1, results.size());
		AssertionHelper.assertActor(false, results.get(0), 1002, "Manuel", "Black");
	}

	@Test
	public void testExecSelect_SingleRow_TwoParams() throws SqlResourceException {
		Request request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), null,
				new String[] { "first_name", "Manuel", "surname", "Black" });
		List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals(1, results.size());
		AssertionHelper.assertActor(false, results.get(0), 1002, "Manuel", "Black");

		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), null,
				new String[] { "id", "1002" });
		results = sqlResource.read(request);
		assertEquals(1, results.size());
		AssertionHelper.assertActor(false, results.get(0), 1002, "Manuel", "Black");
	}
}
