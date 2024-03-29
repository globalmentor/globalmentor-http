/*
 * Copyright © 1996-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.globalmentor.net.http;

/**
 * An indication of the status an HTTP response as defined by <a href="https://www.ietf.org/rfc/rfc2616.txt">RFC 2616</a>,
 * "Hypertext Transfer Protocol -- HTTP/1.1".
 * @author Garret Wilson
 */
public class HTTPStatus {

	/** The HTTP version. */
	private final HTTPVersion version;

	/** @return The HTTP version. */
	public HTTPVersion getVersion() {
		return version;
	}

	/** The status code. */
	private final int statusCode;

	/** @return The status code. */
	public int getStatusCode() {
		return statusCode;
	}

	/** The provided textual representation of the status code. */
	private final String reasonPhrase;

	/** @return The provided textual representation of the status code. */
	public String getReasonPhrase() {
		return reasonPhrase;
	}

	/**
	 * Constructs a status with a version, status code, and reason phrase.
	 * @param version The HTTP version being used.
	 * @param statusCode The status code.
	 * @param reasonPhrase The provided textual representation of the status code.
	 */
	public HTTPStatus(final HTTPVersion version, final int statusCode, final String reasonPhrase) {
		this.version = version;
		this.statusCode = statusCode;
		this.reasonPhrase = reasonPhrase;
	}

	/** @return A string representation of the status useful for debugging. */
	public String toString() {
		return new StringBuilder(getVersion().toString()).append(' ').append(getStatusCode()).append(' ').append(getReasonPhrase()).toString(); //version statusCode reasonPhrase
	}
}
