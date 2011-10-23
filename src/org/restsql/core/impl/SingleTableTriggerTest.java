/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.core.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.sql.Statement;

import org.junit.After;
import org.junit.Test;
import org.restsql.core.Factory;
import org.restsql.core.InvalidRequestException;
import org.restsql.core.Request;
import org.restsql.core.RequestFactoryHelper;
import org.restsql.core.SqlResource;
import org.restsql.core.SqlResourceException;

public class SingleTableTriggerTest extends SqlResourceTestBase {

	@After
	public void tearDown() throws SQLException {
		super.tearDown();
		final Statement statement = connection.createStatement();
		statement.execute("DELETE FROM actor WHERE actor_id between 1000 and 1100");
		statement.close();
	}
	
	@Test
	public void testInsert() throws SqlResourceException {
		SqlResource sqlResource = Factory.getSqlResource("SingleTable");
		TriggerManager.addTrigger(new SingleTableTrigger(), sqlResource.getName());
		Request insertRequest = RequestFactoryHelper.getRequest(Request.Type.INSERT, sqlResource.getName(), null,
				new String[] { "id", "1003", "first_name",
						"A really long name that will throw an exception that we are expecting", "surname",
						"White" });
		try {
			sqlResource.write(insertRequest);
			fail("Expected InvaidRequestException");
		} catch (InvalidRequestException exception) {
			assertEquals("First name length must be less or equal to 25 characters", exception.getMessage());
		}

		insertRequest = RequestFactoryHelper.getRequest(Request.Type.INSERT, sqlResource.getName(), null, new String[] {
				"id", "1003", "first_name", "A valid name", "surname", "White" });
		try {
			sqlResource.write(insertRequest);
		} catch (InvalidRequestException exception) {
			fail("Didn't expect InvalidRequestException " + exception.getMessage());
		}
	}
}
