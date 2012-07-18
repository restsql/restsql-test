/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.security;

import static org.junit.Assert.fail;

import java.security.Principal;

import org.restsql.core.BaseTestCase;
import org.restsql.core.InvalidRequestException;
import org.restsql.core.Request;
import org.restsql.core.RequestFactoryHelper;

/**
 * Base class for Authorizer tests.
 * 
 * @author Mark Sawers
 */
public class BaseAuthorizerTest extends BaseTestCase {

	protected void assertAuthorized(Authorizer authorizer, String roleName, Request.Type type, String sqlResource) {
		boolean authorized = authorizer.isAuthorized(new StubbedSecurityContext(roleName),
				type, sqlResource);
		if (!authorized) {
			fail("Expected " + type + " on " + sqlResource + " for " + roleName);
		}
	}

	protected void assertUnauthorized(Authorizer authorizer, String roleName, Request.Type type, String sqlResource) {
		boolean authorized = authorizer.isAuthorized(new StubbedSecurityContext(roleName),
				newRequest(type, sqlResource));
		if (authorized) {
			fail("Unexpected " + type + " on " + sqlResource + " for " + roleName);
		}
	}

	protected Request newRequest(Request.Type type, String sqlResource) {
		try {
			return RequestFactoryHelper.getRequest(type, sqlResource, null, new String[] { "name", "value" },
					null);
		} catch (InvalidRequestException exception) {
			// should never happen
			exception.printStackTrace();
			return null;
		}
	}

	/** Implements restSQL SecurityContext for this set of tests. */
	protected class StubbedSecurityContext implements SecurityContext {
		private String roleName;
	
		public StubbedSecurityContext(String roleName) {
			setRoleName(roleName);
		}
	
		public void setRoleName(String roleName) {
			this.roleName = roleName;
		}
	
		@Override
		public boolean isUserInRole(String roleName) {
			return this.roleName.equals(roleName);
		}
	
		@Override
		public Principal getUserPrincipal() {
			return null; // doesn't matter for this set of tests
		}
	}
}
