/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestListener;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.restsql.core.Config;
import org.restsql.core.Factory;
import org.restsql.service.ServiceTestCase.InterfaceStyle;
import org.restsql.service.testcase.ServiceTestCaseDefinition;

public class ServiceTestRunner {
	public final static String SCOPE_ALL = "%";
	public final static String TEST_CASE_DIR = "obj/bin/resources/xml/service/testcase";
	public final static String TEST_RESULTS_DIR = "obj/test";

	static {
		if (System.getProperty(Config.KEY_RESTSQL_PROPERTIES) == null) {
			System.setProperty(Config.KEY_RESTSQL_PROPERTIES, "/resources/properties/restsql-mysql.properties");
		}
		System.out.println("Using " + System.getProperty(Config.KEY_RESTSQL_PROPERTIES));
	}

	public static void main(final String[] args) throws SQLException, IOException {
		if (args.length < 2) {
			System.out
					.println("Usage: ServiceTestRunner style scope\n\tstyle=[java|http]\n\tscope=[%|path/to/test/list]");
			System.exit(4);
		}
		final InterfaceStyle interfaceStyle = InterfaceStyle.fromString(args[0]);
		final List<File> files = getDefinitionFiles(args[1]);

		cleanResultsDir();

		final TestSuite suite = new TestSuite();
		final TestResult result = new TestResult();
		final ServiceTestListener listener = new ServiceTestListener();
		result.addListener(listener);

		final Connection connection = Factory.getConnection("sakila");

		if (buildSuite(connection, suite, interfaceStyle, files)) {
			suite.run(result);
			connection.close();

			System.out.println("Tests run: " + result.runCount() + ", Failures: " + result.failureCount()
					+ ", Errors: " + result.errorCount() + ", Time elapsed: "
					+ (float) listener.getTotalElapsedTime() / 1000 + " sec");

			if (result.wasSuccessful()) {
				System.exit(0);
			} else {
				System.exit(1);
			}
		} else {
			System.exit(2);
		}
	}

	private static boolean buildSuite(final Connection connection, final TestSuite suite,
			final InterfaceStyle interfaceStyle, final List<File> files) {
		boolean success = true;
		for (final File file : files) {
			if (file != null && file.getPath().endsWith(".xml")) {
				if (interfaceStyle == InterfaceStyle.Java
						&& (file.getName().contains("ResourceNotFound") || file.getName().contains(
								"FormParam"))) {
					// exclude
				} else {
					try {
						final String category = file.getParentFile().getName();
						final ServiceTestCaseDefinition definition = XmlHelper.unmarshallDefinition(file);
						final ServiceTestCase testCase = new ServiceTestCase(interfaceStyle, category, file
								.getName(), connection, definition);
						suite.addTest(testCase);
					} catch (final Exception exception) {
						System.out.println("Error loading " + file);
						exception.printStackTrace();
						success = false;
					}
				}
			}
		}
		return success;
	}

	private static void cleanResultsDir() {
		// Clean results dir
		final File dir = new File(TEST_RESULTS_DIR);
		if (dir.exists()) {
			final File[] subDirs = dir.listFiles();
			if (subDirs != null) {
				for (final File subDir : subDirs) {
					final File[] files = subDir.listFiles();
					if (files != null) {
						for (final File file : files) {
							file.delete();
						}
					}
					subDir.delete();
				}
			}
		} else {
			dir.mkdir();
		}
	}

	private static List<File> getDefinitionFiles(final String arg) throws FileNotFoundException, IOException {
		final List<File> files = new ArrayList<File>(50);
		if (arg.equals(SCOPE_ALL)) {
			File dir = new File(TEST_CASE_DIR);
			for (final String subDir : dir.list()) {
				if (!subDir.endsWith(".txt") && !subDir.endsWith("*.xsd")) {
					dir = new File(TEST_CASE_DIR + "/" + subDir);
					final File[] subDirFiles = dir.listFiles();
					if (subDirFiles != null) {
						files.addAll(Arrays.asList(subDirFiles));
					}
				}
			}
		} else {
			final File listFile = new File(arg);
			if (listFile.exists()) {
				final BufferedReader reader = new BufferedReader(new FileReader(listFile));
				String fileName;
				final ArrayList<String> listFileNames = new ArrayList<String>();
				while ((fileName = reader.readLine()) != null) {
					if (!fileName.startsWith("#")) {
						listFileNames.add(fileName);
					}
				}
				for (int i = 0; i < listFileNames.size(); i++) {
					fileName = listFileNames.get(i);
					if (fileName.endsWith("/*")) {
						final File dir = new File(TEST_CASE_DIR + "/"
								+ fileName.substring(0, fileName.length() - 2));
						for (final File file : dir.listFiles()) {
							files.add(file);
						}
					} else {
						final File file = new File(TEST_CASE_DIR + "/" + fileName);
						if (!file.exists()) {
							System.out.println("Cannot find " + fileName + " - ignoring");
						} else {
							files.add(file);
						}
					}
				}
			} else {
				System.out.println("Cannot find test list " + arg);
				System.exit(3);
			}
		}
		return files;
	}

	static class ServiceTestListener implements TestListener {
		private long elapsedTime;
		private Throwable error, failure;
		private long startTime;
		private long totalElapsedTime;

		@Override
		public void addError(final Test test, final Throwable e) {
			error = e;
		}

		@Override
		public void addFailure(final Test test, final AssertionFailedError f) {
			failure = f;
		}

		@Override
		public void endTest(final Test test) {
			elapsedTime = System.currentTimeMillis() - startTime;
			totalElapsedTime += elapsedTime;
			// System.out.println(" ... " + (float) elapsedTime / 1000 + " sec");
			if (error != null) {
				System.out.println("---Error- " + error.toString());
				error.printStackTrace();
			} else if (failure != null) {
				System.out.println("---Failure- " + failure.toString());
			}
		}

		public long getElapsedTime() {
			return elapsedTime;
		}

		public long getTotalElapsedTime() {
			return totalElapsedTime;
		}

		@Override
		public void startTest(final Test test) {
			error = null;
			failure = null;
			startTime = System.currentTimeMillis();
			final ServiceTestCase testCase = (ServiceTestCase) test;
			System.out
					.println("Running " + testCase.getTestCaseCategory() + "/" + testCase.getTestCaseName());
		}
	}
}
