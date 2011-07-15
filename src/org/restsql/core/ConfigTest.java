/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class ConfigTest {

	@Test
	public void testDumpConfig() {
		String dump = Config.dumpConfig(true);
		assertNotNull(dump);
		//System.out.println(dump);
	}

	@Test
	public void testDumpLoggingConfig() {
		String dump = Config.dumpLoggingConfig();
		assertNotNull(dump);
		//System.out.println(dump);
	}

	@Test
	public void testException() {
		SqlResourceException e = new SqlResourceException(new SqlResourceException("blah"), "select * from temp");
		assertEquals("org.restsql.core.SqlResourceException: blah :: select * from temp", e.toString());
	}
}
