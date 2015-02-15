/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.service;

import java.io.IOException;
import java.sql.Connection;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.json.simple.parser.ParseException;
import org.restsql.core.Factory;
import org.restsql.core.Factory.SqlResourceFactoryException;
import org.restsql.core.HttpRequestAttributes;
import org.restsql.core.InvalidRequestException;
import org.restsql.core.Request;
import org.restsql.core.Request.Type;
import org.restsql.core.RequestLogger;
import org.restsql.core.SqlResource;
import org.restsql.core.SqlResourceException;
import org.restsql.core.WriteResponse;
import org.restsql.security.SecurityFactory;
import org.restsql.service.testcase.Header;
import org.restsql.service.testcase.ServiceTestCaseDefinition;
import org.restsql.service.testcase.Step;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

public class ServiceTestCase extends TestCase {
	private static final String DEFAULT_BASE_URI = "http://localhost:8080/restsql/";

	static void assertEquals(final Step step, final String element, final int expected, final int actual) {
		assertEquals("step " + step.getName() + ", element " + element, expected, actual);
	}

	static void assertEquals(final Step step, final String element, final String expected, final String actual) {
		assertEquals("step " + step.getName() + ", element " + element, expected, actual);
	}

	static void assertTrue(final Step step, final String element, final boolean actual) {
		assertTrue("step " + step.getName() + ", element " + element, actual);
	}

	private final Connection connection;
	private final ServiceTestCaseDefinition definition;
	private final ServiceTestCaseHelper helper;
	private final String testCaseCategory;
	private final String testCaseName;

	// For testing with ServiceTestRunner
	public ServiceTestCase(final InterfaceStyle interfaceStyle, final String testCaseCategory,
			final String testCaseFileName, final Connection connection,
			final ServiceTestCaseDefinition definition) {
		this.connection = connection;
		this.definition = definition;
		this.testCaseName = testCaseFileName;
		this.testCaseCategory = testCaseCategory;
		if (interfaceStyle == InterfaceStyle.HTTP) {
			setName("testHttpInterface");
		} else {
			setName("testJavaInterface");
		}

		// Load helper specified in config
		helper = new ServiceTestCaseHelper(testCaseCategory, testCaseFileName);
	}

	public ServiceTestCaseDefinition getDefinition() {
		return definition;
	}

	public String getTestCaseCategory() {
		return testCaseCategory;
	}

	public String getTestCaseName() {
		return testCaseName;
	}

	@Override
	public void setUp() throws Exception {
		if (definition.getSetup() != null) {
			helper.resetSequence(connection, definition.getSetup().getResetSequence(), true);
			helper.executeSetupOrTeardownSql(connection, "setUp", definition.getSetup().getSql());
		}
	}

	@Override
	public void tearDown() throws Exception {
		if (definition.getTeardown() != null) {
			helper.executeSetupOrTeardownSql(connection, "tearDown", definition.getTeardown().getSql());
		}
	}

	// Private utils

	public void testHttpInterface() {
		final Client client = Client.create();
		for (final Step step : definition.getStep()) {
			ServiceTestCaseHelper.printRunningStep(step);
			testHttpStep(client, step);
		}
		client.destroy();
	}

	public void testJavaInterface() throws SqlResourceException, ParserConfigurationException, SAXException,
			IOException, JAXBException, InvalidRequestException, ParseException {
		for (final Step step : definition.getStep()) {
			ServiceTestCaseHelper.printRunningStep(step);
			testJavaStep(step);
		}
	}

	private void testHttpStep(final Client client, final Step step) {
		final WebResource resource = client.resource(System.getProperty("org.restsql.baseUri",
				DEFAULT_BASE_URI) + step.getRequest().getUri());
		final String accept = step.getRequest().getAccept();
		final String contentType = step.getRequest().getContentType();
		String requestBody = null;
		Builder builder = null;

		// Add authentication credentials if authorization enabled
		if (SecurityFactory.getAuthorizer().isAuthorizationEnabled()) {
			resource.addFilter(new HTTPBasicAuthFilter(step.getRequest().getUser(), step.getRequest()
					.getPassword()));
		}

		// Get the body and if method = delete, use override header -- jersey client doesn't allow bodies with delete
		if (step.getRequest().getBody() != null) {
			requestBody = step.getRequest().getBody().toString();
			if (step.getRequest().getMethod().equals("DELETE")) {
				builder = resource.header("X-HTTP-Method-Override", "DELETE");
				step.getRequest().setMethod("POST");
			}
		}
		if (builder == null) {
			builder = resource.getRequestBuilder();
		}

		// Add headers
		for (final Header header : step.getRequest().getHeader()) {
			builder = builder.header(header.getName(), header.getValue());
		}

		// Send the request, get the response body, and log results
		final ClientResponse response = builder.accept(accept).type(contentType)
				.method(step.getRequest().getMethod(), ClientResponse.class, requestBody);
		String actualResponseBody = response.getEntity(String.class);
		helper.writeResponseTrace(step, step.getResponse().getStatus(), response.getStatus(), step
				.getResponse().getBody(), actualResponseBody);

		// Assert status
		ServiceTestCase.assertEquals(step, "status", step.getResponse().getStatus(), response.getStatus());

		// Assert body if declared
		if (step.getResponse().getBody() != null) {
			if (step.getResponse().getBody().length() == 0) {
				ServiceTestCase.assertEquals(step, "body", null, actualResponseBody);
			} else {
				if (actualResponseBody.endsWith("\n")) {
					actualResponseBody = actualResponseBody.substring(0, actualResponseBody.length() - 1);
				}
				ServiceTestCase.assertEquals(step, "body", step.getResponse().getBody(), actualResponseBody);
			}
		}

		// Assert headers
		for (final Header header : step.getResponse().getHeader()) {
			ServiceTestCase.assertTrue(step, "expected header " + header.getName(), response.getHeaders()
					.containsKey(header.getName()));
			final StringBuffer actualValue = new StringBuffer(100);
			for (final String value : response.getHeaders().get(header.getName())) {
				if (actualValue.length() > 0) {
					actualValue.append(",");
				}
				actualValue.append(value);
			}
			String expectedValue = header.getValue();
			if (SecurityFactory.getAuthorizer().isAuthorizationEnabled()) {
				expectedValue += ",private";
			}
			ServiceTestCase.assertEquals(step, "header " + header.getName() + " value", expectedValue,
					actualValue.toString());
		}
	}

	private void testJavaStep(final Step step) throws SqlResourceException, ParserConfigurationException,
			SAXException, JAXBException, IOException, ParseException {
		if (step.getRequest().getUri().startsWith("res")) {
			final HttpRequestAttributes httpAttributes = Factory.getHttpRequestAttributes("localhost", step
					.getRequest().getMethod(), step.getRequest().getUri(), step.getRequest().getBody(), step
					.getRequest().getContentType(), step.getRequest().getAccept());
			try {
				final Request request = Factory.getRequest(httpAttributes);
				if (request.getType() == Type.SELECT) {
					try {
						final SqlResource sqlResource = Factory.getSqlResource(request.getSqlResource());
						String actualBody = sqlResource.read(request, httpAttributes.getResponseMediaType());
						String expectedBody = null;
						if (step.getResponse().getBody() != null) {
							expectedBody = step.getResponse().getBody();
						} else {
							actualBody = null;
						}
						helper.writeResponseTrace(step, ServiceTestCaseHelper.STATUS_NOT_APPLICABLE,
								ServiceTestCaseHelper.STATUS_NOT_APPLICABLE, expectedBody, actualBody);
						request.getLogger().log(actualBody);
						ServiceTestCase.assertEquals(step, "body", expectedBody, actualBody);
					} catch (Exception exception) {
						handleJavaStepException(request.getLogger(), step, exception);
					}
				} else {
					try {
						final SqlResource sqlResource = Factory.getSqlResource(request.getSqlResource());
						WriteResponse response = null;
						if (step.getRequest().getBody() != null) {
							response = Factory.getRequestDeserializer(httpAttributes.getRequestMediaType())
									.execWrite(httpAttributes, request.getType(),
											request.getResourceIdentifiers(), sqlResource,
											step.getRequest().getBody(), request.getLogger());
						} else {
							response = sqlResource.write(request);
						}

						String actualBody = Factory.getResponseSerializer(
								httpAttributes.getResponseMediaType()).serializeWrite(sqlResource, response);
						request.getLogger().log(actualBody);
						if (step.getResponse().getStatus() == 200) {
							String expectedBody = null;
							if (step.getResponse().getBody() != null) {
								expectedBody = step.getResponse().getBody();
							}
							helper.writeResponseTrace(step, 200, 200, expectedBody, actualBody);
							ServiceTestCase.assertEquals(step, "body", expectedBody, actualBody);
						} else {
							helper.writeResponseTrace(step, 200, 200, "", "");
							assertEquals("status", step.getResponse().getStatus(), 200);
						}
					} catch (Exception exception) {
						handleJavaStepException(request.getLogger(), step, exception);
					}
				}
			} catch (Exception exception) {
				handleJavaStepException(Factory.getRequestLogger(), step, exception);
			}
		}
	}

	private void handleJavaStepException(RequestLogger requestLogger, Step step, Exception exception) {
		int actualStatus;
		if (exception instanceof InvalidRequestException) {
			actualStatus = 400;
		} else if (exception instanceof SqlResourceFactoryException) {
			actualStatus = 404;
		} else if (exception instanceof SqlResourceException) {
			actualStatus = 500;
		} else {
			exception.printStackTrace();
			actualStatus = 500;
		}
		String expectedBody = null, actualBody = null;
		if (step.getResponse().getBody() != null) {
			expectedBody = step.getResponse().getBody();
			actualBody = exception.getMessage();
		}
		helper.writeResponseTrace(step, step.getResponse().getStatus(), actualStatus, expectedBody,
				actualBody);
		requestLogger.log(actualStatus, exception);
		assertEquals(step.getResponse().getStatus(), actualStatus);
		assertEquals("response body", expectedBody, actualBody);
	}

	public enum InterfaceStyle {
		HTTP, Java;

		public static InterfaceStyle fromString(final String string) {
			if (string.equalsIgnoreCase("http")) {
				return HTTP;
			} else {
				return Java;
			}
		}
	}
}
