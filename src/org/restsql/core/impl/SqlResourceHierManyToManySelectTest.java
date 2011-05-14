/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.core.impl;

import static junit.framework.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.restsql.core.BaseTestCase;
import org.restsql.core.Factory;
import org.restsql.core.Request;
import org.restsql.core.RequestFactoryHelper;
import org.restsql.core.SqlResource;
import org.restsql.core.SqlResourceException;

public class SqlResourceHierManyToManySelectTest extends BaseTestCase {

	@SuppressWarnings("unchecked")
	@Test
	public void testExecSelectCollection_Hierarchical() throws SqlResourceException {
		SqlResource sqlResource = Factory.getSqlResource("HierManyToMany");
		Request request = RequestFactoryHelper.getRequest(Request.Type.SELECT, "HierManyToMany", null, new String[] {
				Request.PARAM_NAME_LIMIT, "100", Request.PARAM_NAME_OFFSET,
				"0" });
		List<Map<String, Object>> results = sqlResource.readCollection(request);
		assertEquals(5, results.size());
		AssertionHelper.assertActor(true, results.get(0), 1, "PENELOPE", "GUINESS");
		AssertionHelper.assertActor(true, results.get(1), 2, "NICK", "WAHLBERG");
		AssertionHelper.assertActor(true, results.get(2), 3, "ED", "CHASE");
		List<Map<String, Object>> childRows = (List<Map<String, Object>>) results.get(0).get("films");
		assertEquals(19, childRows.size());
		AssertionHelper.assertFilmBasics(childRows.get(0), 1, "ACADEMY DINOSAUR", 2006);
		AssertionHelper.assertFilmBasics(childRows.get(1), 23, "ANACONDA CONFESSIONS", 2006);
		AssertionHelper.assertFilmBasics(childRows.get(18), 980, "WIZARD COLDBLOODED", 2006);
	}

}
