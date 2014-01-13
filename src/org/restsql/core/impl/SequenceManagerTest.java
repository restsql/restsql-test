/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.core.impl;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Test;
import org.restsql.core.Factory;
import org.restsql.core.SequenceManager;
import org.restsql.core.SqlResourceException;

/**
 * Tests SequenceManager.
 * 
 * @author Mark Sawers
 */
public class SequenceManagerTest extends SqlResourceTestBase {

	private SequenceManager manager = Factory.getSequenceManager();
	int actorIdValue = 10000;

	public void tearDown()  {
		Statement statement;
		try {
			statement = connection.createStatement();
			statement.executeUpdate("delete from actor where actor_id = " + actorIdValue);
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetAndSetNextValue() throws SqlResourceException, SQLException {
		manager.setNextValue(connection, "actor", "actor_actor_id_seq", actorIdValue, false);
		Statement statement = connection.createStatement();
		int rowsAffected = statement.executeUpdate("insert into actor(first_name, last_name) values ('Bob', 'Smith')");
		statement.close();
		assertEquals("rows affected", 1, rowsAffected);
		int actual = manager.getCurrentValue(connection, "actor_actor_id_seq");
		assertEquals("sequence value", actorIdValue, actual);
	}
}
