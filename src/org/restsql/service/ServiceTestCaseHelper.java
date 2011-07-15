/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.service;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.restsql.service.testcase.Step;

public class ServiceTestCaseHelper {
	public static final int STATUS_NOT_APPLICABLE = -1;
	private static final String TRACE_OUTPUT_DIR = "obj/test";

	static void executeSetupOrTeardownSql(final Connection connection, final String action,
			final List<String> sqls) {
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

	static void printRunningStep(final Step step) {
		System.out.println("\t[" + step.getName() + "] " + step.getRequest().getMethod() + " "
				+ step.getRequest().getUri());
	}

	private final File traceFile;

	ServiceTestCaseHelper(final String testCaseName, final String testCaseCategory) {
		File dir = new File(TRACE_OUTPUT_DIR + "/" + testCaseCategory);
		dir.mkdir();
		String fileName = testCaseName.substring(0, testCaseName.indexOf(".")) + ".log";
		traceFile = new File(dir.getPath() + "/" + fileName);
		traceFile.delete();
	}

	void writeResponseTrace(final Step step, final int expectedStatus, final int actualStatus,
			final String expectedBody, final String actualBody) {
		try {
			final FileOutputStream outputStream = new FileOutputStream(traceFile, true);
			outputStream.write("\nstep ".getBytes());
			outputStream.write(step.getName().getBytes());
			if (expectedStatus != STATUS_NOT_APPLICABLE) {
				outputStream.write(" exected status: ".getBytes());
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

	void writeSuccess() {
		try {
			final FileOutputStream outputStream = new FileOutputStream(traceFile, true);
			outputStream.write("\n\n--- TEST PASSED ---".getBytes());
			outputStream.close();
		} catch (final Exception exception) {
			exception.printStackTrace();
		}
	}

}
