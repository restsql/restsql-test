/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.core;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.restsql.core.RequestValue.Operator;

/**
 * @author Mark Sawers
 */
public class RequestParamTest {
	@Test
	public void testParseInValues_Unescaped() {
		List<String> list = new ArrayList<String>();
		list.add("hello");
		assertEquals(list, RequestValue.parseInValues("(hello)"));
		list.add("goodbye");
		assertEquals(list, RequestValue.parseInValues("(hello,goodbye)"));
		list.add("it's me again");
		assertEquals(list, RequestValue.parseInValues("(hello,goodbye,it's me again)"));
	}
	
	@Test
	public void testParseInValues_EscapedDelimiter() {
		List<String> list = new ArrayList<String>();
		list.add("hel,lo");
		assertEquals(list, RequestValue.parseInValues("(,hel\\,lo)"));
		list.add("3");
		assertEquals(list, RequestValue.parseInValues("(,hel\\,lo,3)"));
		list.add("goodby,e");
		assertEquals(list, RequestValue.parseInValues("(,hel\\,lo,3,goodby\\,e)"));
		list.add("5");
		assertEquals(list, RequestValue.parseInValues("(,hel\\,lo,3,goodby\\,e,5)"));
		assertEquals(list, RequestValue.parseInValues("(,hel\\,lo,3,goodby\\,e,5)"));
	}
	
	/**
	 * Test method for {@link org.restsql.core.RequestValue#parseOperatorFromValue(java.lang.String)}.
	 */
	@Test
	public void testParseOperatorFromValue() {
		assertEquals(Operator.Equals, RequestValue.parseOperatorFromValue("hello"));
		assertEquals(Operator.Equals, RequestValue.parseOperatorFromValue("\\hello"));
		assertEquals(Operator.Escaped, RequestValue.parseOperatorFromValue("\\<hello"));
		assertEquals(Operator.Escaped, RequestValue.parseOperatorFromValue("\\(hello"));
		assertEquals(Operator.Escaped, RequestValue.parseOperatorFromValue("\\(hello)"));
		assertEquals(Operator.In, RequestValue.parseOperatorFromValue("(hello)"));
		assertEquals(Operator.LessThan, RequestValue.parseOperatorFromValue("<hello"));
		assertEquals(Operator.LessThanOrEqualTo, RequestValue.parseOperatorFromValue("<=hello"));
		assertEquals(Operator.GreaterThan, RequestValue.parseOperatorFromValue(">hello"));
		assertEquals(Operator.GreaterThanOrEqualTo, RequestValue.parseOperatorFromValue(">=hello"));
	}

	/**
	 * Test method for {@link org.restsql.core.RequestValue#stripOperatorFromValue(org.restsql.core.RequestValue.Operator operator, java.lang.String)}.
	 */
	@Test
	public void testStripOperatorFromValue() {
		assertEquals("<=hello", RequestValue.stripOperatorFromValue(Operator.Equals, "<=hello"));
		assertEquals("<hello", RequestValue.stripOperatorFromValue(Operator.Escaped, "\\<hello"));
		assertEquals("(hello", RequestValue.stripOperatorFromValue(Operator.Escaped, "\\(hello"));
		assertEquals("hello", RequestValue.stripOperatorFromValue(Operator.LessThan, "<hello"));
		assertEquals("hello", RequestValue.stripOperatorFromValue(Operator.LessThanOrEqualTo, "<=hello"));
		assertEquals("hello", RequestValue.stripOperatorFromValue(Operator.GreaterThan, ">hello"));
		assertEquals("hello", RequestValue.stripOperatorFromValue(Operator.GreaterThanOrEqualTo, ">=hello"));
	}
	
	/**
	 * Test method for {@link org.restsql.core.RequestValue#NameValuePair(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testNameValuePairStringString() {
		RequestValue pair = new RequestValue("param1", "hello");
		assertEquals("param1", pair.getName());
		assertEquals("hello", pair.getValue());
		assertEquals(Operator.Equals, pair.getOperator());
		
		pair = new RequestValue("param1", "\\hello");
		assertEquals("param1", pair.getName());
		assertEquals("\\hello", pair.getValue());
		assertEquals(Operator.Equals, pair.getOperator());

		pair = new RequestValue("param1", "\\<hello");
		assertEquals("param1", pair.getName());
		assertEquals("<hello", pair.getValue());
		assertEquals(Operator.Equals, pair.getOperator());

		pair = new RequestValue("param1", "\\(hello");
		assertEquals("param1", pair.getName());
		assertEquals("(hello", pair.getValue());
		assertEquals(Operator.Equals, pair.getOperator());

		pair = new RequestValue("param1", "(hello,goodbye)");
		assertEquals("param1", pair.getName());
		assertEquals("(hello,goodbye)", pair.getValue());
		assertEquals(Operator.In, pair.getOperator());
		List<String> list = new ArrayList<String>();
		list.add("hello");
		list.add("goodbye");
		assertEquals(list, pair.getInValues());

		pair = new RequestValue("param1", "<hello");
		assertEquals("param1", pair.getName());
		assertEquals("hello", pair.getValue());
		assertEquals(Operator.LessThan, pair.getOperator());

		pair = new RequestValue("param1", "<=hello");
		assertEquals("param1", pair.getName());
		assertEquals("hello", pair.getValue());
		assertEquals(Operator.LessThanOrEqualTo, pair.getOperator());

		pair = new RequestValue("param1", ">hello");
		assertEquals("param1", pair.getName());
		assertEquals("hello", pair.getValue());
		assertEquals(Operator.GreaterThan, pair.getOperator());

		pair = new RequestValue("param1", ">=hello");
		assertEquals("param1", pair.getName());
		assertEquals("hello", pair.getValue());
		assertEquals(Operator.GreaterThanOrEqualTo, pair.getOperator());
	}

	/**
	 * Test method for {@link org.restsql.core.RequestValue#NameValuePair(java.lang.String, java.lang.String, org.restsql.core.RequestValue.Operator)}.
	 */
	@Test
	public void testNameValuePairStringStringOperator() {
		RequestValue pair = new RequestValue("param1", "<=hello", Operator.Equals);
		assertEquals("param1", pair.getName());
		assertEquals("<=hello", pair.getValue());
		assertEquals(Operator.Equals, pair.getOperator());

		pair = new RequestValue("param1", "<=hello", Operator.GreaterThan);
		assertEquals("param1", pair.getName());
		assertEquals("<=hello", pair.getValue());
		assertEquals(Operator.GreaterThan, pair.getOperator());
}

}
