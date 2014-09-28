/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.core;

import java.util.ArrayList;
import java.util.List;

public class RequestFactoryHelper {
	private static RequestLogger requestLogger;

	public static Request getRequest(final Request.Type type, final String sqlResource,
			final Object[] resourceIds, final Object[] params) throws InvalidRequestException {
		return getRequest(type, sqlResource, resourceIds, params, null);
	}

	public static Request getRequest(final Request.Type type, final String sqlResource,
			final Object[] resourceIds, final Object[] params, final Object[][] childrenParamsArray)
			throws InvalidRequestException {
		List<List<RequestValue>> childrenParams = null;
		if (childrenParamsArray != null) {
			childrenParams = new ArrayList<List<RequestValue>>(childrenParamsArray.length);
			for (final Object[] childrenParam : childrenParamsArray) {
				childrenParams.add(getNameValuePairs(childrenParam));
			}
		}
		requestLogger = Factory.getRequestLogger();
		HttpRequestAttributes httpAttributes = Factory.getHttpRequestAttributes("localhost", type.toString(),
				"?", null, HttpRequestAttributes.DEFAULT_MEDIA_TYPE, HttpRequestAttributes.DEFAULT_MEDIA_TYPE);
		Request request = Factory.getRequest(httpAttributes, type, sqlResource,
				getNameValuePairs(resourceIds), getNameValuePairs(params), childrenParams, requestLogger);
		return request;
	}

	public static RequestLogger getRequestLogger() {
		return requestLogger;
	}

	public static void logRequest() {
		requestLogger.log("OK");
	}

	// Private utils

	private static List<RequestValue> getNameValuePairs(final Object[] array) throws InvalidRequestException {
		if (array != null) {
			final List<RequestValue> pairs = new ArrayList<RequestValue>(array.length / 2);
			for (int i = 0; i < array.length - 1; i = i + 2) {
				final RequestValue pair = new RequestValue(array[i].toString(), array[i + 1]);
				pairs.add(pair);
			}
			return pairs;
		} else {
			return null;
		}
	}
}