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
	public void testGetSqlResourceSubdirs() throws SqlResourceFactoryException, SqlResourceException {
		SqlResource sqlResource = Factory.getSqlResource("sub.SingleTable");
		assertEquals("sub.SingleTable", sqlResource.getName());
		assertEquals("sakila", sqlResource.getDefinition().getDefaultDatabase());
		assertEquals("film", sqlResource.getDefinition().getParent());
		assertEquals("pelicula", sqlResource.getDefinition().getParentAlias());

		sqlResource = Factory.getSqlResource("sub.sub.SingleTable");
		assertEquals("sub.sub.SingleTable", sqlResource.getName());
		assertEquals("sakila", sqlResource.getDefinition().getDefaultDatabase());
		assertEquals("language", sqlResource.getDefinition().getParent());
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
		assertEquals(13, resNames.size());
		assertEquals("ErrorInQuery", resNames.get(0));
		assertEquals("FlatManyToOne", resNames.get(1));
		assertEquals("FlatOneToOne", resNames.get(2));
		assertEquals("HierManyToMany", resNames.get(3));
		assertEquals("HierManyToManyExt", resNames.get(4));
		assertEquals("HierOneToMany", resNames.get(5));
		assertEquals("SingleTable", resNames.get(6));
		assertEquals("SingleTableAliased", resNames.get(7));
		assertEquals("SingleTable_FilmRating", resNames.get(8));
		assertEquals("SingleTable_MultiPK", resNames.get(9));
		assertEquals("TestTimestamp", resNames.get(10));
		assertEquals("sub.SingleTable", resNames.get(11));
		assertEquals("sub.sub.SingleTable", resNames.get(12));
	}

	@Test
	public void testGetSqlResourceDefinition() throws SqlResourceFactoryException, IOException {
		InputStream inputStream = Factory.getSqlResourceDefinition("HierManyToMany");
		assertTrue(inputStream.available() > 0);
		inputStream.close();
	}
}
