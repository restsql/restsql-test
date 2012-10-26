/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.core;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.restsql.core.NameValuePair.Operator;

/**
 * @author Mark Sawers
 */
public class NameValuePairTest {
	@Test
	public void testParseInValues_Unescaped() {
		List<String> list = new ArrayList<String>();
		list.add("hello");
		assertEquals(list, NameValuePair.parseInValues("(hello)"));
		list.add("goodbye");
		assertEquals(list, NameValuePair.parseInValues("(hello,goodbye)"));
		list.add("it's me again");
		assertEquals(list, NameValuePair.parseInValues("(hello,goodbye,it's me again)"));
	}
	
	@Test
	public void testParseInValues_EscapedDelimiter() {
		List<String> list = new ArrayList<String>();
		list.add("hel,lo");
		assertEquals(list, NameValuePair.parseInValues("(,hel\\,lo)"));
		list.add("3");
		assertEquals(list, NameValuePair.parseInValues("(,hel\\,lo,3)"));
		list.add("goodby,e");
		assertEquals(list, NameValuePair.parseInValues("(,hel\\,lo,3,goodby\\,e)"));
		list.add("5");
		assertEquals(list, NameValuePair.parseInValues("(,hel\\,lo,3,goodby\\,e,5)"));
		assertEquals(list, NameValuePair.parseInValues("(,hel\\,lo,3,goodby\\,e,5)"));
	}
	
	/**
	 * Test method for {@link org.restsql.core.NameValuePair#parseOperatorFromValue(java.lang.String)}.
	 */
	@Test
	public void testParseOperatorFromValue() {
		assertEquals(Operator.Equals, NameValuePair.parseOperatorFromValue("hello"));
		assertEquals(Operator.Equals, NameValuePair.parseOperatorFromValue("\\hello"));
		assertEquals(Operator.Escaped, NameValuePair.parseOperatorFromValue("\\<hello"));
		assertEquals(Operator.Escaped, NameValuePair.parseOperatorFromValue("\\(hello"));
		assertEquals(Operator.Escaped, NameValuePair.parseOperatorFromValue("\\(hello)"));
		assertEquals(Operator.In, NameValuePair.parseOperatorFromValue("(hello)"));
		assertEquals(Operator.LessThan, NameValuePair.parseOperatorFromValue("<hello"));
		assertEquals(Operator.LessThanOrEqualTo, NameValuePair.parseOperatorFromValue("<=hello"));
		assertEquals(Operator.GreaterThan, NameValuePair.parseOperatorFromValue(">hello"));
		assertEquals(Operator.GreaterThanOrEqualTo, NameValuePair.parseOperatorFromValue(">=hello"));
	}

	/**
	 * Test method for {@link org.restsql.core.NameValuePair#stripOperatorFromValue(org.restsql.core.NameValuePair.Operator operator, java.lang.String)}.
	 */
	@Test
	public void testStripOperatorFromValue() {
		assertEquals("<=hello", NameValuePair.stripOperatorFromValue(Operator.Equals, "<=hello"));
		assertEquals("<hello", NameValuePair.stripOperatorFromValue(Operator.Escaped, "\\<hello"));
		assertEquals("(hello", NameValuePair.stripOperatorFromValue(Operator.Escaped, "\\(hello"));
		assertEquals("hello", NameValuePair.stripOperatorFromValue(Operator.LessThan, "<hello"));
		assertEquals("hello", NameValuePair.stripOperatorFromValue(Operator.LessThanOrEqualTo, "<=hello"));
		assertEquals("hello", NameValuePair.stripOperatorFromValue(Operator.GreaterThan, ">hello"));
		assertEquals("hello", NameValuePair.stripOperatorFromValue(Operator.GreaterThanOrEqualTo, ">=hello"));
	}
	
	/**
	 * Test method for {@link org.restsql.core.NameValuePair#NameValuePair(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testNameValuePairStringString() {
		NameValuePair pair = new NameValuePair("param1", "hello");
		assertEquals("param1", pair.getName());
		assertEquals("hello", pair.getValue());
		assertEquals(Operator.Equals, pair.getOperator());
		
		pair = new NameValuePair("param1", "\\hello");
		assertEquals("param1", pair.getName());
		assertEquals("\\hello", pair.getValue());
		assertEquals(Operator.Equals, pair.getOperator());

		pair = new NameValuePair("param1", "\\<hello");
		assertEquals("param1", pair.getName());
		assertEquals("<hello", pair.getValue());
		assertEquals(Operator.Equals, pair.getOperator());

		pair = new NameValuePair("param1", "\\(hello");
		assertEquals("param1", pair.getName());
		assertEquals("(hello", pair.getValue());
		assertEquals(Operator.Equals, pair.getOperator());

		pair = new NameValuePair("param1", "(hello,goodbye)");
		assertEquals("param1", pair.getName());
		assertEquals("(hello,goodbye)", pair.getValue());
		assertEquals(Operator.In, pair.getOperator());
		List<String> list = new ArrayList<String>();
		list.add("hello");
		list.add("goodbye");
		assertEquals(list, pair.getInValues());

		pair = new NameValuePair("param1", "<hello");
		assertEquals("param1", pair.getName());
		assertEquals("hello", pair.getValue());
		assertEquals(Operator.LessThan, pair.getOperator());

		pair = new NameValuePair("param1", "<=hello");
		assertEquals("param1", pair.getName());
		assertEquals("hello", pair.getValue());
		assertEquals(Operator.LessThanOrEqualTo, pair.getOperator());

		pair = new NameValuePair("param1", ">hello");
		assertEquals("param1", pair.getName());
		assertEquals("hello", pair.getValue());
		assertEquals(Operator.GreaterThan, pair.getOperator());

		pair = new NameValuePair("param1", ">=hello");
		assertEquals("param1", pair.getName());
		assertEquals("hello", pair.getValue());
		assertEquals(Operator.GreaterThanOrEqualTo, pair.getOperator());
	}

	/**
	 * Test method for {@link org.restsql.core.NameValuePair#NameValuePair(java.lang.String, java.lang.String, org.restsql.core.NameValuePair.Operator)}.
	 */
	@Test
	public void testNameValuePairStringStringOperator() {
		NameValuePair pair = new NameValuePair("param1", "<=hello", Operator.Equals);
		assertEquals("param1", pair.getName());
		assertEquals("<=hello", pair.getValue());
		assertEquals(Operator.Equals, pair.getOperator());
	}

}
