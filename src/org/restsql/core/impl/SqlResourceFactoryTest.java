/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.core.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Test;
import org.restsql.core.BaseTestCase;
import org.restsql.core.Factory;
import org.restsql.core.Factory.SqlResourceFactoryException;
import org.restsql.core.SqlResource;
import org.restsql.core.SqlResourceException;

public class SqlResourceFactoryTest extends BaseTestCase {
	@Test
	public void testGetSqlResource() throws SqlResourceFactoryException, SqlResourceException {
		SqlResource sqlResource = Factory.getSqlResource("HierManyToMany");
		assertEquals("HierManyToMany", sqlResource.getName());
		assertEquals("sakila", sqlResource.getDefinition().getMetadata().getDatabase().getDefault());
		assertEquals("actor", sqlResource.getDefinition().getMetadata().getTable().get(0).getName());
		assertEquals("film", sqlResource.getDefinition().getMetadata().getTable().get(1).getName());
	}

	@Test
	public void testGetSqlResourceSubdirs() throws SqlResourceFactoryException, SqlResourceException {
		SqlResource sqlResource = Factory.getSqlResource("sub.SingleTable");
		assertEquals("sub.SingleTable", sqlResource.getName());
		assertEquals("sakila", sqlResource.getDefinition().getMetadata().getDatabase().getDefault());
		assertEquals("ACTOR", sqlResource.getDefinition().getMetadata().getTable().get(0).getName());
		assertNull(sqlResource.getDefinition().getMetadata().getTable().get(0).getAlias());

		sqlResource = Factory.getSqlResource("sub.sub.SingleTable");
		assertEquals("sub.sub.SingleTable", sqlResource.getName());
		assertEquals("sakila", sqlResource.getDefinition().getMetadata().getDatabase().getDefault());
		assertEquals("language", sqlResource.getDefinition().getMetadata().getTable().get(0).getName());
	}

	@Test
	public void testIsSqlResourceLoaded() throws SqlResourceFactoryException, SqlResourceException {
		SqlResourceFactoryImpl factory = new SqlResourceFactoryImpl();
		SqlResource sqlResource = factory.getSqlResource("HierManyToMany");
		assertEquals("HierManyToMany", sqlResource.getName());
		factory.isSqlResourceLoaded("HierManyToMany");
	}

	@Test
	public void testGetSqlResourceNames() throws SqlResourceFactoryException {
		List<String> resNames = Factory.getSqlResourceNames();
		assertTrue(resNames.contains("FlatManyToOne"));
		assertTrue(resNames.contains("FlatOneToOne"));
		assertTrue(resNames.contains("FlatOneToOneMulti"));
		assertTrue(resNames.contains("HierManyToMany"));
		assertTrue(resNames.contains("HierManyToManyExt"));
		assertTrue(resNames.contains("HierManyToManyMultiExt"));
		assertTrue(resNames.contains("HierOneToMany"));
		assertTrue(resNames.contains("HierOneToManyMultiExt"));
		assertTrue(resNames.contains("SingleTable"));
		assertTrue(resNames.contains("SingleTableAliased"));
		assertTrue(resNames.contains("SingleTable_FilmRating"));
		assertTrue(resNames.contains("SingleTable_MultiPK"));
		assertTrue(resNames.contains("DateTime"));
		assertTrue(resNames.contains("negative.ErrorInQuery"));
		assertTrue(resNames.contains("negative.MissingMetadata"));
		assertTrue(resNames.contains("sub.SingleTable"));
		assertTrue(resNames.contains("sub.sub.SingleTable"));
	}

	@Test(expected = SqlResourceFactoryException.class)
	public void testGetSqlResourceNames_WithInvalidDirectory() throws SqlResourceFactoryException {
		new SqlResourceFactoryImpl().getSqlResourceNames("/doesnotexist");
	}
	
	@Test
	public void testGetSqlResourceNames_WithEmptyDirectory() throws SqlResourceFactoryException {
		String dirName = SqlResourceFactoryTest.class.getName();
		File dir = new File(dirName);
		dir.mkdir();
		List<String> resNames = new SqlResourceFactoryImpl().getSqlResourceNames(dirName);
		dir.delete();
		assertEquals(0, resNames.size());
	}
	
	@Test
	public void testGetSqlResourceDefinition() throws SqlResourceFactoryException, IOException {
		InputStream inputStream = Factory.getSqlResourceDefinition("HierManyToMany");
		assertTrue(inputStream.available() > 0);
		inputStream.close();
	}
}
