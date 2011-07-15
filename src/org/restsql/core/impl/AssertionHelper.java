/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.core.impl;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import java.util.List;
import java.util.Map;

import org.restsql.core.ColumnMetaData;

public class AssertionHelper {

	static void assertActor(final boolean hierarchical, final Map<String, Object> row, final int actor_id,
			final String first_name, final String last_name) {
		assertEquals(3 + (hierarchical ? 1 : 0), row.size());
		assertEquals(new Integer(actor_id), row.get("actor_id"));
		assertEquals(first_name, row.get("first_name"));
		assertEquals(last_name, row.get("last_name"));
	}

	static void assertColumnMetaData(final Map<String, ColumnMetaData> columns, final int columnNumber,
			final boolean primaryKey, final String catalogName, final String tableName,
			final String columnName, final String columnLabel, final int columnType) {
		final ColumnMetaData actual = columns.get(columnLabel);
		assertNotNull("actual column found", actual);
		assertColumnMetaData(actual, columnNumber, primaryKey, catalogName, tableName, columnName,
				columnLabel, columnType);
	}

	public static void assertColumnMetaData(ColumnMetaData actual, int columnNumber, boolean primaryKey,
			String catalogName, String tableName, String columnName, String columnLabel, int columnType) {
		assertEquals("column number", columnNumber, actual.getColumnNumber());
		assertEquals("primary key", primaryKey, actual.isPrimaryKey());
		assertEquals("catalog name", catalogName, actual.getDatabaseName());
		assertEquals("table name", tableName, actual.getTableName());
		assertEquals("column name", columnName, actual.getColumnName());
		assertEquals("column label", columnLabel, actual.getColumnLabel());
		assertEquals("column type", columnType, actual.getColumnType());
	}

	static void assertFilmBasics(final List<Map<String, Object>> rows, final int film_id, final String title,
			final int year) {
		boolean rowFound = false;
		for (Map<String, Object> row : rows) {
			if (row.get("film_id").equals(film_id)) {
				assertFilmBasics(row, film_id, title, year);
				rowFound = true;
			}
		}
		if (!rowFound) {
			fail("film_id " + film_id + " not found");
		}
	}

	static void assertFilmBasics(final Map<String, Object> row, final int film_id, final String title,
			final int year) {
		assertEquals(3, row.size());
		assertEquals(new Integer(film_id), row.get("film_id"));
		assertEquals(title, row.get("title"));
		assertEquals(year, row.get("year"));
	}

	static void assertFilmBasicsHierarchical(final Map<String, Object> row, final int film_id,
			final String title, final int year) {
		assertEquals(4, row.size());
		assertEquals(new Integer(film_id), row.get("film_id"));
		assertEquals(title, row.get("title"));
		assertEquals(year, row.get("year"));
	}

	static void assertFilmLanguageFlat(final Map<String, Object> row, final int film_id, final String title,
			final int year, final int language_id, final String language) {
		assertEquals(5, row.size());
		assertEquals(new Integer(film_id), row.get("film_id"));
		assertEquals(title, row.get("title"));
		assertEquals(year, row.get("year"));
		assertEquals(new Integer(language_id), row.get("language_id"));
		assertEquals(language, row.get("name"));
	}

	static void assertFilmRating(final Map<String, Object> row, final int film_id, final String title,
			final int year, final int film_rating_id, final int stars) {
		assertEquals(9, row.size());
		assertEquals(new Integer(film_id), row.get("film_id"));
		assertEquals(title, row.get("title"));
		assertEquals(year, row.get("year"));
		assertEquals(film_rating_id, row.get("film_rating_id"));
		assertEquals(stars, row.get("stars"));
	}

	static void assertLanguage(final Map<String, Object> row, final int language_id, final String language) {
		assertEquals(2, row.size());
		assertEquals(new Integer(language_id), row.get("language_id"));
		assertEquals(language, row.get("name"));
	}

	static void assertLanguageHierarchical(final Map<String, Object> row, final int language_id,
			final String language) {
		assertEquals(3, row.size());
		assertEquals(new Integer(language_id), row.get("language_id"));
		assertEquals(language, row.get("name"));
	}

}
