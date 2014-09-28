/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.core.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SqlResourceImplTest {

	@Test
	public void testRemoveWhitespaceFromSql() {
		assertEquals("select * from dual", SqlResourceImpl.removeWhitespaceFromSql("select * from dual"));
		assertEquals("select * from dual", SqlResourceImpl.removeWhitespaceFromSql(" select * from dual"));
		assertEquals("select * from dual", SqlResourceImpl.removeWhitespaceFromSql("    select * from dual"));
		assertEquals("select * from dual", SqlResourceImpl.removeWhitespaceFromSql("  select * from dual   "));
		assertEquals("select *\n from dual",
				SqlResourceImpl.removeWhitespaceFromSql("  \n\nselect *\n from dual   "));
		assertEquals("select *\n from dual",
				SqlResourceImpl.removeWhitespaceFromSql("  \r\r\n\nselect *\r\n from dual   "));
		assertEquals("select *\n from dual\nwhere time=now",
				SqlResourceImpl.removeWhitespaceFromSql("  \r\r\n\nselect *\r\n from dual\nwhere time=now   "));
		assertEquals("select *\n from dual\n where time=now",
				SqlResourceImpl.removeWhitespaceFromSql("  \r\r\n\n\t\tselect *\r\n\tfrom dual\n\twhere time=now   \t"));
	}

}
