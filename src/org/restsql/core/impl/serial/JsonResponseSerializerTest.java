/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.core.impl.serial;

import static org.junit.Assert.*;

import org.junit.Test;
import org.restsql.core.impl.serial.JsonResponseSerializer;

/**
 * @author Dan Moore
 */
public class JsonResponseSerializerTest {

	@Test
	public void testAppendNameValuePairNormal() {
		JsonResponseSerializer jrs = new JsonResponseSerializer();
		assertNotNull(jrs);
		StringBuilder sb = new StringBuilder();
		jrs.addAttribute(true, sb, "bar", "baz");

		jrs.addAttribute(false, sb, "bar2", "baz2");

		assertEquals("\"bar\": \"baz\", \"bar2\": \"baz2\"", sb.toString());
	}

	@Test
	public void testAppendNameValuePairOne() {
		JsonResponseSerializer jrs = new JsonResponseSerializer();
		assertNotNull(jrs);
		StringBuilder sb = new StringBuilder();
		jrs.addAttribute(true, sb, "bar", "baz");
		assertFalse(false);

		assertEquals("\"bar\": \"baz\"", sb.toString());
	}

	@Test
	public void testAppendNameValuePairOneNull() {
		JsonResponseSerializer jrs = new JsonResponseSerializer();
		assertNotNull(jrs);
		StringBuilder sb = new StringBuilder();
		jrs.addAttribute(true, sb, "bar", null);

		assertEquals("\"bar\": null", sb.toString());
	}

	@Test
	public void testAppendNameValuePairNone() {
		JsonResponseSerializer jrs = new JsonResponseSerializer();
		assertNotNull(jrs);
		StringBuilder sb = new StringBuilder();

		assertEquals("", sb.toString());
	}

	@Test
	public void testAppendNameValuePairNullFirst() {
		JsonResponseSerializer jrs = new JsonResponseSerializer();
		assertNotNull(jrs);
		StringBuilder sb = new StringBuilder();
		jrs.addAttribute(true, sb, "bar", null);

		jrs.addAttribute(false, sb, "bar2", "baz2");

		assertEquals("\"bar\": null, \"bar2\": \"baz2\"", sb.toString());
	}

	@Test
	public void testAppendNameValuePairNullInMiddle() {
		JsonResponseSerializer jrs = new JsonResponseSerializer();
		assertNotNull(jrs);
		StringBuilder sb = new StringBuilder();
		jrs.addAttribute(true, sb, "bar", "baz");

		jrs.addAttribute(false, sb, "bar3", null);

		jrs.addAttribute(false, sb, "bar2", "baz2");

		assertEquals("\"bar\": \"baz\", \"bar3\": null, \"bar2\": \"baz2\"", sb.toString());
	}

	@Test
	public void testAppendNameValuePairNullAtEnd() {
		JsonResponseSerializer jrs = new JsonResponseSerializer();
		assertNotNull(jrs);
		StringBuilder sb = new StringBuilder();
		jrs.addAttribute(true, sb, "bar", "baz");

		jrs.addAttribute(false, sb, "bar2", "baz2");

		jrs.addAttribute(false, sb, "bar3", null);

		assertEquals("\"bar\": \"baz\", \"bar2\": \"baz2\", \"bar3\": null", sb.toString());
	}
}