/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.core;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Mark Sawers
 *
 */
public class FactoryTest {

	@Test
	public void testGetRequestDeserializer() throws SqlResourceException {
		String mediaType = "application/xml";
		assertEquals(mediaType, Factory.getRequestDeserializer(mediaType).getSupportedMediaType());

		mediaType = "application/json";
		assertEquals(mediaType, Factory.getRequestDeserializer(mediaType).getSupportedMediaType());
		
		try {
			Factory.getRequestDeserializer("bad");
			fail("Expected SqlResourceException for bad");
		} catch(SqlResourceException expected) {
		}
	}

	@Test
	public void testGetResponseSerializer() throws SqlResourceException {
		String acceptMediaType = "application/xml";
		assertEquals("application/xml", Factory.getResponseSerializer(acceptMediaType).getSupportedMediaType());
		
		acceptMediaType = "application/json";
		assertEquals("application/json", Factory.getResponseSerializer(acceptMediaType).getSupportedMediaType());
		
		try {
			Factory.getResponseSerializer("bad");
			fail("Expected SqlResourceException for bad");
		} catch(SqlResourceException expected) {
		}
	}
}
