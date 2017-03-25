/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.tools.impl;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restsql.core.BaseTestCase;
import org.restsql.core.Config;
import org.restsql.core.Factory;
import org.restsql.core.Factory.SqlResourceFactoryException;
import org.restsql.core.Request;
import org.restsql.core.RequestFactoryHelper;
import org.restsql.core.SqlResource;
import org.restsql.core.SqlResourceException;
import org.restsql.tools.ResourceDefinitionGenerator;
import org.restsql.tools.ToolsFactory;

public class ResourceDefinitionGeneratorTest extends BaseTestCase {
	private static final int EXPECTED_DEFS_GENERATED = 22;
	private final static String databaseName = "sakila";
	private final static String sqlResourcesDir = Config.properties.getProperty(Config.KEY_SQLRESOURCES_DIR,
			Config.DEFAULT_SQLRESOURCES_DIR);
	private final static String subDirName = "auto-test";
	private final static File subDirObj = new File(sqlResourcesDir + "/" + subDirName);
	private ResourceDefinitionGenerator generator;

	@Before
	public void setUp() {
		generator = ToolsFactory.getResourceDefinitionGenerator();
	}

	/** Deletes subdirectory contents and folder. */
	@After
	public void tearDown() {
		if (subDirObj.exists()) {
			for (final File file : subDirObj.listFiles()) {
				file.delete();
			}
			if (!subDirObj.delete()) {
				System.out.println("could not delete " + subDirObj.getAbsolutePath());
			}
		}
	}

	@Test
	public void testGenerate() throws ResourceDefinitionGenerator.GenerationException,
			SqlResourceFactoryException, SqlResourceException {
		final int defs = generator.generate(subDirName, databaseName, getExclusionPattern());
		assertEquals("defs generated", EXPECTED_DEFS_GENERATED, defs);
		assertEquals("files created", EXPECTED_DEFS_GENERATED, subDirObj.listFiles().length);

		// Check out the actor resource
		SqlResource sqlResource = Factory.getSqlResource(subDirName + ".actor");
		Request request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(),
				new String[] { "actor_id", "1" }, null);
		List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals(1, results.size());
		Map<String, Object> row = results.get(0);
		assertEquals("actor_id", 1, row.get("actor_id"));
		assertEquals("first_name", "PENELOPE", row.get("first_name"));
		assertEquals("last_name", "GUINESS", row.get("last_name"));
		assertNotNull(row.get("last_update"));

		// Check out the store resource
		sqlResource = Factory.getSqlResource(subDirName + ".store");
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), new String[] {
				"store_id", "2" }, null);
		results = sqlResource.read(request);
		assertEquals(1, results.size());
		row = results.get(0);
		assertEquals("store_id", 2, row.get("store_id"));
		assertEquals("manager_staff_id", 2, row.get("manager_staff_id"));
		assertEquals("address_id", 2, row.get("address_id"));
		assertNotNull(row.get("last_update"));
	}

	@Test(expected = ResourceDefinitionGenerator.GenerationException.class)
	public void testGenerate_WithBlankDatabaseName() throws ResourceDefinitionGenerator.GenerationException {
		generator.generate(subDirName, "", null);
	}

	@Test
	public void testGenerate_WithEmptySubdir() throws ResourceDefinitionGenerator.GenerationException {
		subDirObj.mkdir();
		assertEquals("defs generated", EXPECTED_DEFS_GENERATED, generator.generate(subDirName, databaseName, getExclusionPattern()));
	}

	@Test(expected = ResourceDefinitionGenerator.GenerationException.class)
	public void testGenerate_WithNonEmptySubdir() throws ResourceDefinitionGenerator.GenerationException,
			IOException {
		subDirObj.mkdir();
		final File file = new File(subDirObj, "test.txt");
		file.createNewFile();
		generator.generate(subDirName, databaseName, null);
	}

	@Test(expected = ResourceDefinitionGenerator.GenerationException.class)
	public void testGenerate_WithNullDatabaseName() throws ResourceDefinitionGenerator.GenerationException {
		generator.generate(subDirName, null, null);
	}

	@Test
	public void testCreateSubDir_WithNullSubDir() throws ResourceDefinitionGenerator.GenerationException {
		File dir = ((AbstractResourceDefinitionGenerator)generator).createSubDir(null, sqlResourcesDir);
		assertEquals("sqlResourcesDir", sqlResourcesDir, dir.getAbsolutePath());
	}

	@Test
	public void testCreateSubDir_WithBlankSubDir() throws ResourceDefinitionGenerator.GenerationException {
		File dir = ((AbstractResourceDefinitionGenerator)generator).createSubDir("", sqlResourcesDir);
		assertEquals("sqlResourcesDir", sqlResourcesDir, dir.getAbsolutePath());
	}

	// Helper methods
	
	private String getExclusionPattern() {
		if (getDatabaseType() == DatabaseType.PostgreSql) {
			return "payment_p2007_0%";
		} else {
			return "film_text";
		}
	}
}
