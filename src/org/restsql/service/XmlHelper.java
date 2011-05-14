/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.restsql.service.testcase.ObjectFactory;
import org.restsql.service.testcase.ServiceTestCaseDefinition;
import org.restsql.service.testcase.Step;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

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

	public static int unmarshallWriteResponse(final String doc) throws JAXBException, ParserConfigurationException,
			SAXException, IOException {
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		WriteResponseHandler handler = new WriteResponseHandler(); 
		parser.parse(new ByteArrayInputStream(doc.getBytes()), handler);
		return handler.getRowsAffected();
	}

	static class WriteResponseHandler extends DefaultHandler {
		private int rowsAffected;
	
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			rowsAffected = Integer.valueOf(attributes.getValue("rowsAffected")).intValue();
		}
		
		public int getRowsAffected() {
			return rowsAffected;
		}
	}

}
