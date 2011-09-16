/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.security;



import org.junit.BeforeClass;
import org.junit.Test;
import org.restsql.core.Request;

/**
 * Tests default implementation of authorizer.
 * 
 * @author Mark Sawers
 */
public class AuthorizerTest extends BaseAuthorizerTest {
	private static Authorizer authorizer;
	
	@BeforeClass
	public static void classSetUp() {
		authorizer = SecurityFactory.getAuthorizer();
	}

	@Test
	public void testIsAuthorized_ForWildcardResourceWithAuthorizedAction() {
		assertAuthorized(authorizer, "all", Request.Type.SELECT, "SingleTable");
		assertAuthorized(authorizer, "all", Request.Type.DELETE, "SingleTable");
		assertAuthorized(authorizer, "all", Request.Type.UPDATE, "HierManyToMany");
		assertAuthorized(authorizer, "readonly", Request.Type.SELECT, "Fabricated");
	}

	@Test
	public void testIsAuthorized_ForWildcardResourceWithUnauthorizedAction() {
		assertUnauthorized(authorizer, "limited", Request.Type.DELETE, "SingleTable");
		assertUnauthorized(authorizer, "limited", Request.Type.UPDATE, "Fabricated");
	}

	@Test
	public void testIsAuthorized_ForSpecificResourceWithAuthorizedAction() {
		assertAuthorized(authorizer, "limited", Request.Type.INSERT, "SingleTable");
		assertAuthorized(authorizer, "limited", Request.Type.UPDATE, "SingleTable");
		assertAuthorized(authorizer, "all", Request.Type.UPDATE, "FlatOneToOne");
	}

	@Test
	public void testIsAuthorized_ForSpecificResourceWithUnauthorizedRole() {
		assertUnauthorized(authorizer, "InvalidRole", Request.Type.DELETE, "SingleTable");
	}

	@Test
	public void testIsAuthorized_ForSpecificResourceWithUnauthorizedAction() {
		assertUnauthorized(authorizer, "limited", Request.Type.DELETE, "SingleTable");
		assertUnauthorized(authorizer, "readonly", Request.Type.INSERT, "FlatOneToOne");
	}

	@Test
	public void testIsAuthorized_ForSpecificResourceNotFound() {
		assertUnauthorized(authorizer, "limited", Request.Type.INSERT, "InvalidResource");
	}
}
