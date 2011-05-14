/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Test;
import org.restsql.core.Factory.SqlResourceFactoryException;
import org.restsql.core.impl.SqlResourceFactoryImpl;

public class SqlResourceFactoryTest extends BaseTestCase {
	@Test
	public void testGetSqlResource() throws SqlResourceFactoryException, SqlResourceException {
		SqlResource sqlResource = Factory.getSqlResource("HierManyToMany");
		assertEquals("HierManyToMany", sqlResource.getName());
		assertEquals("sakila", sqlResource.getDefinition().getDefaultDatabase());
		assertEquals("actor", sqlResource.getDefinition().getParent());
		assertEquals("film", sqlResource.getDefinition().getChild());
	}

	@Test
	public void testIsSqlResourceLoaded() throws SqlResourceFactoryException, SqlResourceException {
		SqlResourceFactoryImpl factory = new SqlResourceFactoryImpl();
		SqlResource sqlResource = factory.getSqlResource("HierManyToMany");
		assertEquals("HierManyToMany", sqlResource.getName());
		factory.isSqlResourceLoaded("HierManyToMany");
	}

	@Test
	public void testGetSqlResourceNames() {
		List<String> resNames = Factory.getSqlResourceNames();
		assertTrue(resNames.size() > 1);
	}

	@Test
	public void testGetSqlResourceDefinition() throws SqlResourceFactoryException, IOException {
		InputStream inputStream = Factory.getSqlResourceDefinition("HierManyToMany");
		assertTrue(inputStream.available() > 0);
		inputStream.close();
	}
}
