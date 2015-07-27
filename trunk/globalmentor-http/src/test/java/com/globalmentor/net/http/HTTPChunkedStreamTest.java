/*
 * Copyright © 1996-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.globalmentor.net.http;

import java.io.*;
import java.util.Arrays;

import com.globalmentor.io.Streams;

import static junit.framework.Assert.*;

import org.junit.Test;

/**
 * Tests for streams processing HTTP chunked encoding.
 * @author Garret Wilson
 */
public class HTTPChunkedStreamTest {

	/**
	 * Generates a buffer of data for testing using sequential bytes.
	 * @param length The number of bytes to return.
	 * @return A buffer of data for use in testing.
	 */
	public static byte[] generateSequentialTestData(final int length) {
		final byte[] testData = new byte[length];
		short b = 0;
		for(int i = 0; i < testData.length; ++i) {
			testData[i] = (byte)b++; //store this byte and increment the value
			if(b > 0xff) { //if we go over the maximum byte size
				b = 0; //start over for byte values
			}
		}
		return testData;
	}

	/** Tests that two HTTP chunked streams can transfer data between each other. */
	@Test
	public void testHTTPChunkedRoundTrip() throws IOException {
		final byte[] testData = generateSequentialTestData(HTTPChunkedOutputStream.DEFAULT_CHUNK_SIZE * 5 / 2); //use a buffer size that tests around two and a half chunks
		final ByteArrayOutputStream temp = new ByteArrayOutputStream();
		final OutputStream outputStream = new HTTPChunkedOutputStream(temp); //write the test data to a temporary buffer chunk-encoded
		outputStream.write(testData);
		outputStream.close();
		final InputStream inputStream = new HTTPChunkedInputStream(new ByteArrayInputStream(temp.toByteArray())); //read the chunked data back back out
		final ByteArrayOutputStream copy = new ByteArrayOutputStream();
		Streams.copy(inputStream, copy);
		inputStream.close();
		assertTrue("HTTP chunked output stream did not correctly write data.", Arrays.equals(testData, copy.toByteArray()));
	}

}
