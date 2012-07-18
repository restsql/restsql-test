/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.core.impl;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;
import org.restsql.core.BaseTestCase;
import org.restsql.core.Factory;
import org.restsql.core.HttpRequestAttributes;
import org.restsql.core.InvalidRequestException;
import org.restsql.core.Request;
import org.restsql.core.RequestFactoryHelper;
import org.restsql.core.SqlResource;
import org.restsql.core.SqlResourceException;
import org.restsql.core.Trigger;
import org.restsql.core.Factory.SqlResourceFactoryException;
import org.restsql.core.Request.Type;

public class MockedTriggerTest extends BaseTestCase {

	@Test
	public void testExecuteTriggers() throws InvalidRequestException, SqlResourceException,
			SqlResourceFactoryException {
		SqlResource sqlResource = Factory.getSqlResource("SingleTable");

		Mockery context = new Mockery();
		final Trigger trigger = context.mock(Trigger.class);

		TriggerManager.addTrigger(trigger, sqlResource.getName());

		final Request insertRequest = RequestFactoryHelper.getRequest(Request.Type.INSERT, sqlResource.getName(), null,
				new String[] { "id", "1003", "first_name", "Patty", "surname", "White" });
		final Request selectRequest = RequestFactoryHelper.getRequest(Type.SELECT, sqlResource.getName(), new String[] {
				"id", "1003" }, null);
		final Request updateRequest = RequestFactoryHelper.getRequest(Type.UPDATE, sqlResource.getName(), new String[] {
				"id", "1003" }, new String[] { "surname", "Black" });
		final Request deleteRequest = RequestFactoryHelper.getRequest(Type.DELETE, sqlResource.getName(), new String[] {
				"id", "1003" }, null);

		context.checking(new Expectations() {
			{
				oneOf(trigger).beforeInsert(insertRequest);
				oneOf(trigger).afterInsert(insertRequest);
				oneOf(trigger).beforeSelect(selectRequest);
				oneOf(trigger).afterSelect(selectRequest);
				oneOf(trigger).beforeUpdate(updateRequest);
				oneOf(trigger).afterUpdate(updateRequest);
				oneOf(trigger).beforeDelete(deleteRequest);
				oneOf(trigger).afterDelete(deleteRequest);
			}
		});

		sqlResource.write(insertRequest);
		sqlResource.read(selectRequest, HttpRequestAttributes.DEFAULT_MEDIA_TYPE);
		sqlResource.write(updateRequest);
		sqlResource.write(deleteRequest);
	}
}
