/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.service;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.restsql.service.testcase.ObjectFactory;
import org.restsql.service.testcase.ServiceTestCaseDefinition;
import org.restsql.service.testcase.Step;

public class XmlHelper {

	@SuppressWarnings("unchecked")
	public static ServiceTestCaseDefinition unmarshallDefinition(final File file) throws JAXBException {
		final JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
		final Unmarshaller unmarshaller = context.createUnmarshaller();
		unmarshaller.setSchema(null);
		final ServiceTestCaseDefinition definition = ((JAXBElement<ServiceTestCaseDefinition>) unmarshaller
				.unmarshal(file)).getValue();
		for (Step step : definition.getStep()) {
			final String requestBody = step.getRequest().getBody();
			final String responseBody = step.getResponse().getBody();
			if (requestBody != null) {
				step.getRequest().setBody(requestBody.trim());
			}
			if (responseBody != null) {
				step.getResponse().setBody(responseBody.trim());
			}
		}
		return definition;
	}
}
