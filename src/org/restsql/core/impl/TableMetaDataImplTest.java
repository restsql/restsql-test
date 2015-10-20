/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.core.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.restsql.core.TableMetaData;

public class TableMetaDataImplTest extends TableMetaDataImpl {

	/**
	 * Test method for
	 * {@link org.restsql.core.impl.TableMetaDataImpl#setAliases(java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testSetAliases() {
		testChildAndParent(TableMetaData.TableRole.Parent);
		testChildAndParent(TableMetaData.TableRole.Child);

		// Should ignore with parent extension
		TableMetaDataImpl table = new TableMetaDataImplTest();
		table.setAttributes("table1", "qualifedTable1", "database1", TableMetaData.TableRole.ParentExtension);
		table.setAliases(null, null, "tablex");
		assertTableNameAndAliases(table, "table1", "table1", "table1", "table1s");

		// Should ignore with child extension
		table = new TableMetaDataImplTest();
		table.setAttributes("table1", "qualifedTable1", "database1", TableMetaData.TableRole.ChildExtension);
		table.setAliases(null, null, "tablex");
		assertTableNameAndAliases(table, "table1", "table1", "table1", "table1s");

		// Should ignore with join
		table = new TableMetaDataImplTest();
		table.setAttributes("table1", "qualifedTable1", "database1", TableMetaData.TableRole.Join);
		table.setAliases(null, null, "tablex");
		assertTableNameAndAliases(table, "table1", "table1", "table1", "table1s");
	}

	/**
	 * 
	 */
	private void testChildAndParent(final TableMetaData.TableRole tableRole) {
		TableMetaDataImpl table = new TableMetaDataImplTest();
		table.setAttributes("table1", "qualifedTable1", "database1", tableRole);
		assertTableNameAndAliases(table, "table1", "table1", "table1", "table1s");

		table = new TableMetaDataImplTest();
		table.setAttributes("table1", "qualifedTable1", "database1", tableRole);
		table.setAliases("table", null, null);
		assertTableNameAndAliases(table, "table1", "table", "table", "tables");

		table = new TableMetaDataImplTest();
		table.setAttributes("table1", "qualifedTable1", "database1", tableRole);
		table.setAliases("table", "tablex", null);
		assertTableNameAndAliases(table, "table1", "tablex", "tablex", "tablexs");

		table = new TableMetaDataImplTest();
		table.setAttributes("table1", "qualifedTable1", "database1", tableRole);
		table.setAliases("table", "tablex", "tablexd");
		assertTableNameAndAliases(table, "table1", "tablex", "tablex", "tablexd");

		table = new TableMetaDataImplTest();
		table.setAttributes("table1", "qualifedTable1", "database1", tableRole);
		table.setAliases(null, "tablex", null);
		assertTableNameAndAliases(table, "table1", "tablex", "tablex", "tablexs");

		table = new TableMetaDataImplTest();
		table.setAttributes("table1", "qualifedTable1", "database1", tableRole);
		table.setAliases(null, "tablex", "tablexd");
		assertTableNameAndAliases(table, "table1", "tablex", "tablex", "tablexd");

		table = new TableMetaDataImplTest();
		table.setAttributes("table1", "qualifedTable1", "database1", tableRole);
		table.setAliases(null, null, "tablex");
		assertTableNameAndAliases(table, "table1", "table1", "table1", "tablex");
	}

	// Asserts table name and aliases
	@SuppressWarnings("deprecation")
	private void assertTableNameAndAliases(final TableMetaData table, final String name, final String alias,
			final String rowAlias, final String rowSetAlias) {
		assertEquals("tableName", name, table.getTableName());
		assertEquals("alias", alias, table.getTableAlias());
		assertEquals("rowAlias", rowAlias, table.getRowAlias());
		assertEquals("rowSetAlias", rowSetAlias, table.getRowSetAlias());
	}
}
