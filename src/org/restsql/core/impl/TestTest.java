/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.core.impl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import org.junit.Test;
import org.restsql.core.Factory;
import org.restsql.core.Factory.SqlResourceFactoryException;
import org.restsql.core.SqlResourceException;


/**
 * @author Mark Sawers
 *
 */
public class TestTest {

	@Test
	public void testIt() throws SqlResourceFactoryException, SqlResourceException {
		//final InputStream stream = Factory.getSqlResourceDefinition("SingleTable");
//		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\
//<rs:sqlResource xmlns:rs=\"http://restsql.org/schema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\
//	xsi:schemaLocation=\"http://restsql.org/schema ../../../../../restsql/src/resources/xml/SqlResource.xsd\">\
//	<query>\
//		select actor_id "id", first_name, last_name \"surname\"\
//		from actor\
//	</query>\
//	<metadata>\
//		<database default="sakila" />\
//		<table name="ACTOR" role="Parent" />\
//	</metadata>\
//</rs:sqlResource>;" 
		
	}
	
}
