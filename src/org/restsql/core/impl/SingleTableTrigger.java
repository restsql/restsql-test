/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.core.impl;

import org.restsql.core.InvalidRequestException;
import org.restsql.core.RequestValue;
import org.restsql.core.Request;
import org.restsql.core.SqlResourceException;
import org.restsql.core.Trigger;

public class SingleTableTrigger implements Trigger {

	@Override
	public void afterDelete(final Request request) throws SqlResourceException {
	}

	@Override
	public void afterInsert(final Request request) throws SqlResourceException {
	}

	@Override
	public void afterSelect(final Request request) throws SqlResourceException {
	}

	@Override
	public void afterUpdate(final Request request) throws SqlResourceException {
	}

	@Override
	public void beforeDelete(final Request request) throws SqlResourceException {
	}

	@Override
	public void beforeInsert(final Request request) throws SqlResourceException {
		validateParams(request);
	}

	@Override
	public void beforeSelect(final Request request) throws SqlResourceException {
	}

	@Override
	public void beforeUpdate(final Request request) throws SqlResourceException {
		validateParams(request);
	}

	// Private utils
	
	private void validateParams(final Request request) throws SqlResourceException {
		for (final RequestValue param : request.getParameters()) {
			if (param.getName().equals("first_name")) {
				if (param.getValue() != null && param.getValue().toString().length() > 25) {
					throw new InvalidRequestException("First name length must be less or equal to 25 characters");
				}
			}
		}
	}
}
