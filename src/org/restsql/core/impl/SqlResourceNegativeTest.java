/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.core.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.restsql.core.Factory;
import org.restsql.core.Request;
import org.restsql.core.RequestFactoryHelper;
import org.restsql.core.SqlResource;
import org.restsql.core.SqlResourceException;

public class SqlResourceNegativeTest extends SqlResourceTestBase {

	@Test
	public void testMissingDatabase() {
		try {
			final SqlResource sqlResource = Factory.getSqlResource("negative.MissingDatabase");
			@SuppressWarnings("unused")
			final Request request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource
					.getName(), null, new String[] {});
			fail("expected SqlResourceException");
		} catch (final SqlResourceException exception) {
			assertEquals("Definition requires one database element with default name", exception.getMessage());
		}
	}

	@Test
	public void testMissingMetadata() {
		try {
			final SqlResource sqlResource = Factory.getSqlResource("negative.MissingMetadata");
			@SuppressWarnings("unused")
			final Request request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource
					.getName(), null, new String[] {});
			fail("expected SqlResourceException");
		} catch (final SqlResourceException exception) {
			assertEquals("Definition requires one metadata element", exception.getMessage());
		}
	}

	@Test
	public void testMissingParent() {
		try {
			final SqlResource sqlResource = Factory.getSqlResource("negative.MissingParent");
			@SuppressWarnings("unused")
			final Request request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource
					.getName(), null, new String[] {});
			fail("expected SqlResourceException");
		} catch (final SqlResourceException exception) {
			assertEquals("Definition requires one table element with role Parent", exception.getMessage());
		}
	}

	@Test
	public void testMissingQuery() {
		try {
			final SqlResource sqlResource = Factory.getSqlResource("negative.MissingQuery");
			@SuppressWarnings("unused")
			final Request request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource
					.getName(), null, new String[] {});
			fail("expected SqlResourceException");
		} catch (final SqlResourceException exception) {
			assertEquals("Definition requires one query element", exception.getMessage());
		}
	}

	@Test
	public void testTwoParents() {
		try {
			final SqlResource sqlResource = Factory.getSqlResource("negative.TwoParents");
			@SuppressWarnings("unused")
			final Request request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource
					.getName(), null, new String[] {});
			fail("expected SqlResourceException");
		} catch (final SqlResourceException exception) {
			assertEquals("Definition requires one table element with role Parent", exception.getMessage());
		}
	}

	@Test
	public void testWrongParent() {
		try {
			final SqlResource sqlResource = Factory.getSqlResource("negative.WrongParent");
			@SuppressWarnings("unused")
			final Request request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource
					.getName(), null, new String[] {});
			fail("expected SqlResourceException");
		} catch (final SqlResourceException exception) {
			assertEquals("Definition requires table element for actor, referenced by column actor_id",
					exception.getMessage());
		}
	}
}
