/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Scanner;

import junit.framework.Test;

import org.restsql.core.Factory;
import org.restsql.core.SqlResourceException;
import org.restsql.service.testcase.ResetSequence;
import org.restsql.service.testcase.Step;

public class ServiceTestCaseHelper {
	static final int STATUS_NOT_APPLICABLE = -1;

	private static String getFileNameFromTestCaseName(final String testCaseName) {
		final String fileName = testCaseName.substring(0, testCaseName.lastIndexOf(".")) + ".log";
		return fileName;
	}

	static void printRunningStep(final Step step) {
		System.out.println("\t[" + step.getName() + "] " + step.getRequest().getMethod() + " "
				+ step.getRequest().getUri());
	}

	static void renameLog(final Test test, final String prefix) {
		final ServiceTestCase testCase = (ServiceTestCase) test;
		final String dirName = ServiceTestRunner.TEST_RESULTS_DIR + "/" + testCase.getTestCaseCategory();
		final String logFileName = getFileNameFromTestCaseName(testCase.getTestCaseName());
		final File oldFile = new File(dirName + "/" + logFileName);
		final File newFile = new File(dirName + "/" + prefix + logFileName);
		// renameTo doesn't work so open old and copy content to new, then delete old
		Scanner scan;
		try {
			scan = new Scanner(oldFile);
			scan.useDelimiter("\\Z");
			final String content = scan.next();
			scan.close();
			final FileOutputStream outputStream = new FileOutputStream(newFile, true);
			outputStream.write(content.getBytes());
			outputStream.close();
			oldFile.delete();
		} catch (final FileNotFoundException exception) {
			// System.out.println("Couldn't find log file: " + exception.toString());
		} catch (final IOException exception) {
			System.out.println("Couldn't read log file: " + exception.toString());
		}
	}

	private final File traceFile;
	private final String fullTestCaseFileName;

	public ServiceTestCaseHelper(final String testCaseCategory, final String testCaseFileName) {
		final File dir = new File(ServiceTestRunner.TEST_RESULTS_DIR + "/" + testCaseCategory);
		dir.mkdir();
		final String fileName = getFileNameFromTestCaseName(testCaseFileName);
		traceFile = new File(dir.getPath() + "/" + fileName);
		traceFile.delete();
		fullTestCaseFileName = testCaseCategory + "/" + testCaseFileName;
	}

	void executeSetupOrTeardownSql(final Connection connection, final String action, final List<String> sqls) {
		if (sqls != null && sqls.size() > 0) {
			String trace = null;
			try {
				final Statement statement = connection.createStatement();
				for (final String sql : sqls) {
					trace = "\t[" + action + "] " + sql;
					final int rowsAffected = statement.executeUpdate(sql);
					System.out.println(trace + " (rows=" + rowsAffected + ")");
				}
				statement.close();
			} catch (final SQLException exception) {
				System.out.println("SQLException on " + action + ": " + exception.getMessage() + " \n"
						+ trace);
			}
		}
	}

	void resetSequence(final Connection connection, final List<ResetSequence> resetSequences,
			boolean printAction) {
		if (resetSequences != null && resetSequences.size() > 0) {
			try {
				for (final ResetSequence resetSequence : resetSequences) {
					Factory.getSequenceManager().setNextValue(connection, resetSequence.getTable(),
							resetSequence.getName(), resetSequence.getNextval(), printAction);
				}
			} catch (final SqlResourceException exception) {
				System.out.println("SQLException on reset sequence: " + exception.getMessage());
			}
		}
	}

	void writeResponseTrace(final Step step, final int expectedStatus, final int actualStatus,
			final String expectedBody, final String actualBody) {
		try {
			boolean newFile = !traceFile.exists();
			final FileOutputStream outputStream = new FileOutputStream(traceFile, true);
			if (newFile) {
				outputStream.write(fullTestCaseFileName.getBytes());
			}
			outputStream.write("\n\nstep ".getBytes());
			outputStream.write(step.getName().getBytes());
			if (expectedStatus != STATUS_NOT_APPLICABLE) {
				outputStream.write(" expected status: ".getBytes());
				outputStream.write(String.valueOf(expectedStatus).getBytes());
				outputStream.write(" actual: ".getBytes());
				outputStream.write(String.valueOf(actualStatus).getBytes());
			}
			outputStream.write("\nexpected body:\n".getBytes());
			if (expectedBody != null) {
				outputStream.write(expectedBody.getBytes());
			}
			outputStream.write("\nactual:\n".getBytes());
			if (actualBody != null) {
				outputStream.write(actualBody.getBytes());
			}
			outputStream.close();
		} catch (final Exception exception) {
			exception.printStackTrace();
		}
	}
}
