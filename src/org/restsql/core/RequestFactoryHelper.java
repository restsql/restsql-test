/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.core;

import java.util.ArrayList;
import java.util.List;

public class RequestFactoryHelper {
	public static Request getRequest(final Request.Type type, final String sqlResource, final String[] resourceIds,
			final String[] params) throws InvalidRequestException {
		return getRequest(type, sqlResource, resourceIds, params, null);
	}

	public static Request getRequest(final Request.Type type, final String sqlResource, final String[] resourceIds,
			final String[] params, final String[][] childrenParamsArray) throws InvalidRequestException {
		List<List<NameValuePair>> childrenParams = null;
		if (childrenParamsArray != null) {
			childrenParams = new ArrayList<List<NameValuePair>>(childrenParamsArray.length);
			for (final String[] childrenParam : childrenParamsArray) {
				childrenParams.add(getNameValuePairs(childrenParam));
			}
		}
		RequestLogger requestLogger = Factory.getRequestLogger("localhost", type.toString(), "?");
		return Factory.getRequest(type, sqlResource, getNameValuePairs(resourceIds), getNameValuePairs(params),
				childrenParams, requestLogger);
	}

	// Private utils

	private static List<NameValuePair> getNameValuePairs(final String[] array) throws InvalidRequestException {
		if (array != null) {
			final List<NameValuePair> pairs = new ArrayList<NameValuePair>(array.length / 2);
			for (int i = 0; i < array.length - 1; i = i + 2) {
				final NameValuePair pair = new NameValuePair(array[i], array[i + 1]);
				pairs.add(pair);
			}
			return pairs;
		} else {
			return null;
		}
	}
}
