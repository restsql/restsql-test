/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.core.impl;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restsql.core.BinaryObject;
import org.restsql.core.Factory;
import org.restsql.core.Request;
import org.restsql.core.RequestFactoryHelper;
import org.restsql.core.SqlResourceException;

public class SqlResourceFlatOneToOneBlobTest extends SqlResourceTestBase {
	private static final String IMAGE_FILENAME_DECODED = "card-decoded.jpg";
	private static final String IMAGE_FILENAME_ENCODED = "card.txt";
	private static final String IMAGE_FILENAME_INSERTED = "card-inserted.jpg";
	private static final String IMAGE_FILENAME_INSERTED_WITHENCODED = "card-inserted-encoded.jpg";
	private static final String IMAGE_FILENAME_UPDATED = "card-updated.jpg";
	private static final String IMAGE_PATH = "src/resources/img/card.jpg";
	private static final String IMAGE_PATH_ENCODED = "src/resources/img/card.txt";
	private static final String TEMP_DIRNAME = "obj/test/api";

	private static byte[] readFile(final String path) throws IOException {
		final File file = new File(path);
		final int length = (int) file.length();
		final FileInputStream inputStream = new FileInputStream(path);
		final byte[] bytes = new byte[length];
		inputStream.read(bytes, 0, length);
		inputStream.close();
		return bytes;
	}

	private static void writeFile(final String path, final byte[] bytes) throws IOException {
		final FileOutputStream outputStream = new FileOutputStream(path);
		outputStream.write(bytes);
		outputStream.close();
	}

	@Override
	@Before
	public void setUp() throws SQLException, SqlResourceException {
		super.setUp();
		final Statement statement = connection.createStatement();
		statement
				.execute("INSERT INTO film (film_id,title,release_year,language_id,rental_duration,rental_rate,replacement_cost)"
						+ " VALUES (5000,'ESCAPE FROM TOMORROW',2011,1,0,0,0)");
		statement.close();
		final PreparedStatement prepStatement = connection
				.prepareStatement("INSERT INTO film_image (film_id,image)" + " VALUES (5000,?)");
		prepStatement.setBytes(1, "test value".getBytes());
		prepStatement.executeUpdate();
		prepStatement.close();

		sqlResource = Factory.getSqlResource("FlatOneToOneBlob");

		final File tempDir = new File(TEMP_DIRNAME);
		tempDir.mkdirs();
	}

	@Override
	@After
	public void tearDown() throws SQLException {
		super.tearDown();
		final Statement statement = connection.createStatement();
		statement.execute("DELETE FROM film_image");
		statement.execute("DELETE FROM film WHERE film_id between 5000 and 5500");
		statement.close();
	}
	
	@Test
	public void testEncodeDecode() throws IOException {
		final BinaryObject image = new BinaryObject(readFile(IMAGE_PATH));
		final String encodedImage = image.toString();
		final BinaryObject decodedImage = BinaryObject.fromString(encodedImage);
		writeFile(TEMP_DIRNAME + "/" + IMAGE_FILENAME_DECODED, decodedImage.getBytes());
		writeFile(TEMP_DIRNAME + "/" + IMAGE_FILENAME_ENCODED, encodedImage.getBytes());
		assertEquals("length", image.getBytes().length, decodedImage.getBytes().length);
		assertEquals("uuencoded image", image.toString(), decodedImage.toString());
	}

	@Test
	public void testExecDelete() throws SqlResourceException {
		// Update test fixture
		Request request = RequestFactoryHelper.getRequest(Request.Type.DELETE, sqlResource.getName(),
				new String[] { "film_id", "5000" }, null);
		final int rowsAffected = sqlResource.write(request).getRowsAffected();
		assertEquals(2, rowsAffected);

		// Verify updates
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), new String[] {
				"film_id", "5000" }, null);
		final List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals(0, results.size());
	}

	@Test
	public void testExecInsert_WithBytes() throws SqlResourceException, IOException {
		final BinaryObject expectedImage = new BinaryObject(readFile(IMAGE_PATH));

		// Insert
		Request request = RequestFactoryHelper.getRequest(Request.Type.INSERT, sqlResource.getName(), null,
				new Object[] { "film_id", "5003", "title", "BLESSED SUN", "year", "2011", "language_id", "1",
						"rental_duration", "0", "rental_rate", "0", "replacement_cost", "0", "image",
						expectedImage });
		final int rowsAffected = sqlResource.write(request).getRowsAffected();
		assertEquals(2, rowsAffected);

		// Verify insert
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), new String[] {
				"film_id", "5003" }, null);
		final List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals(1, results.size());
		assertTrue("class type", results.get(0).get("image") instanceof BinaryObject);
		final BinaryObject actualImage = (BinaryObject) results.get(0).get("image");
		assertEquals("length", expectedImage.getBytes().length, actualImage.getBytes().length);
		assertEquals("uuencoded image", expectedImage.toString(), actualImage.toString());

		writeFile(TEMP_DIRNAME + "/" + IMAGE_FILENAME_INSERTED, actualImage.getBytes());
	}

	@Test
	public void testExecInsert_WithEncoded() throws SqlResourceException, IOException {
		final String encodedImage = new String(readFile(IMAGE_PATH_ENCODED));

		// Insert
		Request request = RequestFactoryHelper.getRequest(Request.Type.INSERT, sqlResource.getName(), null,
				new Object[] { "film_id", "5003", "title", "BLESSED SUN", "year", "2011", "language_id", "1",
						"rental_duration", "0", "rental_rate", "0", "replacement_cost", "0", "image",
						encodedImage });
		final int rowsAffected = sqlResource.write(request).getRowsAffected();
		assertEquals(2, rowsAffected);

		// Verify insert
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), new String[] {
				"film_id", "5003" }, null);
		final List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals(1, results.size());
		assertTrue("class type", results.get(0).get("image") instanceof BinaryObject);
		final BinaryObject actualImage = (BinaryObject) results.get(0).get("image");

		writeFile(TEMP_DIRNAME + "/" + IMAGE_FILENAME_INSERTED_WITHENCODED, actualImage.getBytes());

		assertEquals("encoded image", encodedImage, actualImage.toString());
	}

	@Test
	public void testExecUpdate() throws SqlResourceException, IOException {
		final BinaryObject expectedImage = new BinaryObject(readFile(IMAGE_PATH));

		// Update test fixture
		Request request = RequestFactoryHelper.getRequest(Request.Type.UPDATE, sqlResource.getName(),
				new Object[] { "film_id", "5000" }, new Object[] { "year", "2010", "title",
						"ESCAPE FROM YESTERDAY", "image", expectedImage });
		final int rowsAffected = sqlResource.write(request).getRowsAffected();
		assertEquals(2, rowsAffected);

		// Verify updates
		request = RequestFactoryHelper.getRequest(Request.Type.SELECT, sqlResource.getName(), new String[] {
				"film_id", "5000" }, null);
		final List<Map<String, Object>> results = sqlResource.read(request);
		assertEquals(1, results.size());
		assertEquals("id", 5000, results.get(0).get("film_id"));
		assertEquals("title", "ESCAPE FROM YESTERDAY", results.get(0).get("title"));

		assertTrue("class type", results.get(0).get("image") instanceof BinaryObject);
		final BinaryObject actualImage = (BinaryObject) results.get(0).get("image");
		assertEquals("length", expectedImage.getBytes().length, actualImage.getBytes().length);

		writeFile(TEMP_DIRNAME + "/" + IMAGE_FILENAME_UPDATED, actualImage.getBytes());
	}

}
