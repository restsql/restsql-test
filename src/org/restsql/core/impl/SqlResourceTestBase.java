/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.core.impl;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.restsql.core.BaseTestCase;
import org.restsql.core.Factory;
import org.restsql.core.SqlResource;
import org.restsql.core.SqlResourceException;

public class SqlResourceTestBase extends BaseTestCase {
	protected static Connection connection;
	protected SqlResource sqlResource;

	@Before
	public void setUp() throws SQLException, SqlResourceException {
	}

	@After
	public void tearDown() throws SQLException {
	}

	@BeforeClass
	public static void classSetUp() throws SQLException {
		connection = Factory.getConnection("sakila");
	}

	@AfterClass
	public static void classTearDown() throws SQLException {
		connection.close();
	}
}