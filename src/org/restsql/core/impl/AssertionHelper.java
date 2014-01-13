/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.core.impl;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.restsql.core.ColumnMetaData;
import org.restsql.core.Request;
import org.restsql.core.ResponseValue;
import org.restsql.core.WriteResponse;

public class AssertionHelper {

	public static void assertColumnMetaData(final ColumnMetaData actual, final int columnNumber,
			final boolean primaryKey, final String catalogName, final String tableName,
			final String columnName, final String columnLabel, final int columnType, final boolean sequence,
			final String sequenceName) {
		assertEquals("column number", columnNumber, actual.getColumnNumber());
		assertEquals("primary key", primaryKey, actual.isPrimaryKey());
		assertEquals("catalog name", catalogName, actual.getDatabaseName());
		assertEquals("table name", tableName, actual.getTableName());
		assertEquals("column name", columnName, actual.getColumnName());
		assertEquals("column label", columnLabel, actual.getColumnLabel());
		assertEquals("column type", columnType, actual.getColumnType());
		assertEquals("column sequence", sequence, actual.isSequence());
		assertEquals("column seqeuence name", sequenceName, actual.getSequenceName());
	}

	static void assertActor(final boolean hierarchical, final Map<String, Object> row, final int actor_id,
			final String first_name, final String last_name) {
		assertEquals(3 + (hierarchical ? 1 : 0), row.size());
		assertEquals(new Integer(actor_id), (hierarchical ? row.get("actor_id") : row.get("id")));
		assertEquals(first_name, row.get("first_name"));
		assertEquals(last_name, (hierarchical ? row.get("last_name") : row.get("surname")));
	}

	static void assertColumnMetaData(final Map<String, ColumnMetaData> columns, final int columnNumber,
			final boolean primaryKey, final String catalogName, final String tableName,
			final String columnName, final String columnLabel, final int columnType, final boolean sequence,
			final String sequenceName) {
		final ColumnMetaData actual = columns.get(columnLabel);
		assertNotNull("column found in column list by label", actual);
		assertColumnMetaData(actual, columnNumber, primaryKey, catalogName, tableName, columnName,
				columnLabel, columnType, sequence, sequenceName);
	}

	static void assertFilmBasics(final List<Map<String, Object>> rows, final int film_id, final String title,
			final int year) {
		boolean rowFound = false;
		for (final Map<String, Object> row : rows) {
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
		assertEquals(new Integer(language_id), row.get("langId"));
		assertEquals(language, row.get("langName"));
	}

	static void assertDateTime(final Map<String, Object> row, final int id, final Date dateTime)
			throws ParseException {
		assertEquals(5, row.size());
		assertEquals(new Integer(id), row.get("id"));
		assertEquals(
				new SimpleDateFormat("HH:mm:ss").parse(new SimpleDateFormat("HH:mm:ss").format(dateTime)),
				row.get("time"));
		assertEquals(dateTime, row.get("timestamp"));
		assertEquals(
				new SimpleDateFormat("yyyy-MM-dd").parse(new SimpleDateFormat("yyyy-MM-dd").format(dateTime)),
				row.get("date"));
		assertEquals(dateTime, row.get("datetime"));
	}

	@SuppressWarnings("unchecked")
	static void assertResponse(Request expectedRequest, int expectedRowsAffected, Object[] expectedValues,
			WriteResponse response) {
		assertEquals("rows affected", expectedRowsAffected, response.getRowsAffected());
		assertNotNull("not null", response.getRows());
		assertEquals("size", 1, response.getRows().size());

		// assertEquals(i + ":size", expectedResult.length / 2, requestResult.getResponseValues().size());
		int i = 0;
		for (ResponseValue actualValue : response.getRows().get(0)) {
			assertEquals("name", expectedValues[i], actualValue.getName());
			if (actualValue.getValue() instanceof List<?>) {
				Object[][] expectedChildListValues = (Object[][]) expectedValues[i + 1];
				int j = 0;
				for (Set<ResponseValue> actualChildValues : ((List<Set<ResponseValue>>) actualValue.getValue())) {
					Object[] expectedChildValues = expectedChildListValues[j];
					int k = 0;
					for (ResponseValue actualChildValue : actualChildValues) {
						assertEquals("name", expectedChildValues[k], actualChildValue.getName());
						assertEquals("value", expectedChildValues[k + 1], actualChildValue.getValue());
						k += 2;
					}
					j++;
				}
			} else {
				assertEquals("value", expectedValues[i + 1], actualValue.getValue());
			}
			i += 2;
		}
	}

}