/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.security.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.restsql.core.Config;
import org.restsql.core.Request;
import org.restsql.security.Authorizer;
import org.restsql.security.BaseAuthorizerTest;
import org.restsql.security.SecurityFactory;

/**
 * Tests default implementation of authorizer.
 * 
 * @author Mark Sawers
 */
public class AuthorizerImplTest extends BaseAuthorizerTest {

	@Test
	public void testValidPrivileges() {
		Authorizer authorizer = SecurityFactory.getAuthorizer();
		String dump = authorizer.dumpConfig();
		assertTrue("enabled", authorizer.isAuthorizationEnabled());
		String expected = "Authorization enabled -- Loaded privileges from /etc/opt/restsql/privileges.properties\n"
				+ "\n"
				+ "[SqlResource,*].[requestType,*]=role\n"
				+ "---------------------------------------------------------------------------\n"
				+ "*.*=all\n"
				+ "*.SELECT=limited\n"
				+ "*.SELECT=readonly\n"
				+ "FlatOneToOne.*=limited\n"
				+ "FlatOneToOne.UPDATE=all\n"
				+ "SingleTable.INSERT,UPDATE=limited\n";
		assertEquals(expected, dump);
	}

	@Test
	public void testNoConfigDefinied() {
		AuthorizerImpl authorizer = new AuthorizerImpl(null);
		assertFalse("enabled", authorizer.isAuthorizationEnabled());
		assertEquals("Authorization disabled -- No privileges defined. Use " + Config.KEY_SECURITY_PRIVILEGES
				+ " in restsql properties.", authorizer.dumpConfig());
		assertAuthorized(authorizer, "readonly", Request.Type.DELETE, "SingleTable");
	}

	@Test
	public void testInvalidPrivileges_WithEmptyConfig() {
		String fileName = "src/resources/properties/empty-privileges.properties";
		AuthorizerImpl authorizer = new AuthorizerImpl(fileName);
		assertTrue("enabled", authorizer.isAuthorizationEnabled());
		assertEquals("Authorization enabled -- But no privileges valid in " + fileName, authorizer
				.dumpConfig());
		assertUnauthorized(authorizer, "readonly", Request.Type.DELETE, "SingleTable");
	}

	@Test
	public void testInvalidPrivileges_WithInvalidConfig() {
		String fileName = "src/resources/properties/invalid-privileges.properties";
		AuthorizerImpl authorizer = new AuthorizerImpl(fileName);
		assertTrue("enabled", authorizer.isAuthorizationEnabled());
		assertEquals("Authorization enabled -- But no privileges valid in " + fileName, authorizer
				.dumpConfig());
		assertUnauthorized(authorizer, "readonly", Request.Type.DELETE, "SingleTable");
	}
}
