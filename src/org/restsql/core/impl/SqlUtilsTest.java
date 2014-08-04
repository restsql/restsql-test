/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.core.impl;

import static org.junit.Assert.*;

import org.junit.Test;

public class SqlUtilsTest {

	@Test
	public void testRemoveWhitespaceFromSql() {
		assertEquals("select * from dual", SqlUtils.removeWhitespaceFromSql("select * from dual"));
		assertEquals("select * from dual", SqlUtils.removeWhitespaceFromSql(" select * from dual"));
		assertEquals("select * from dual", SqlUtils.removeWhitespaceFromSql("    select * from dual"));
		assertEquals("select * from dual", SqlUtils.removeWhitespaceFromSql("  select * from dual   "));
		assertEquals("select *\n from dual",
				SqlUtils.removeWhitespaceFromSql("  \n\nselect *\n from dual   "));
		assertEquals("select *\n from dual",
				SqlUtils.removeWhitespaceFromSql("  \r\r\n\nselect *\r\n from dual   "));
		assertEquals("select *\n from dual\nwhere time=now",
				SqlUtils.removeWhitespaceFromSql("  \r\r\n\nselect *\r\n from dual\nwhere time=now   "));
		assertEquals("select *\n from dual\n where time=now",
				SqlUtils.removeWhitespaceFromSql("  \r\r\n\n\t\tselect *\r\n\tfrom dual\n\twhere time=now   \t"));
	}

}
