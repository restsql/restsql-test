/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.core.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Properties;

import org.junit.Test;
import org.restsql.core.AbstractTrigger;
import org.restsql.core.BaseTestCase;
import org.restsql.core.Factory;
import org.restsql.core.Request;
import org.restsql.core.RequestFactoryHelper;
import org.restsql.core.SqlResource;
import org.restsql.core.SqlResourceException;
import org.restsql.core.Request.Type;

public class TriggerManagerTest extends BaseTestCase {
	static ArrayList<Request> allRequests = new ArrayList<Request>();
	static ArrayList<Request> singleTableRequests = new ArrayList<Request>();
	static ArrayList<Request> multiRequests = new ArrayList<Request>();

	@Test
	public void testLoadingAndExecutingTriggers() throws SqlResourceException {

		Properties definitions = new Properties();
		definitions.setProperty(AllStubbedTrigger.class.getName(), TriggerManager.TOKEN_WILDCARD);
		definitions.setProperty(SingleTableStubbedTrigger.class.getName(), "SingleTable");
		definitions.setProperty(MultiStubbedTrigger.class.getName(), "SingleTable,SingleTable_MultiPK");
		TriggerManager.loadTriggers(definitions);

		SqlResource sqlResource = Factory.getSqlResource("SingleTable");
		Request request = RequestFactoryHelper.getRequest(Type.SELECT, sqlResource.getName(), new String[] { "actor_id",
				"1" }, null);
		sqlResource.readXml(request);
		assertEquals("size all", 1, allRequests.size());
		assertEquals("size singleTable", 1, singleTableRequests.size());
		assertEquals("size multi", 1, multiRequests.size());

		sqlResource = Factory.getSqlResource("SingleTable_MultiPK");
		request = RequestFactoryHelper.getRequest(Type.SELECT, sqlResource.getName(), new String[] { "actorId", "1" },
				null);
		sqlResource.readXml(request);
		assertEquals("size all", 2, allRequests.size());
		assertEquals("size singleTable", 1, singleTableRequests.size());
		assertEquals("size multi", 2, multiRequests.size());

		sqlResource = Factory.getSqlResource("FlatManyToOne");
		request = RequestFactoryHelper.getRequest(Type.SELECT, sqlResource.getName(), new String[] { "film_id", "1" },
				null);
		sqlResource.readXml(request);
		assertEquals("size all", 3, allRequests.size());
		assertEquals("size singleTable", 1, singleTableRequests.size());
		assertEquals("size multi", 2, multiRequests.size());
	}

	public static class SingleTableStubbedTrigger extends AbstractTrigger {
		@Override
		public void beforeSelect(Request request) throws SqlResourceException {
			singleTableRequests.add(request);
		}
	}

	public static class AllStubbedTrigger extends AbstractTrigger {
		@Override
		public void beforeSelect(Request request) throws SqlResourceException {
			allRequests.add(request);
		}
	}

	public static class MultiStubbedTrigger extends AbstractTrigger {
		@Override
		public void beforeSelect(Request request) throws SqlResourceException {
			multiRequests.add(request);
		}
	}
}
