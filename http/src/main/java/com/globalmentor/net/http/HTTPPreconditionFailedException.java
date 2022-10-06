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

import static com.globalmentor.net.HTTP.*;

/**
 * Indicates that precondition given in one or more of the request-header fields evaluated to false when it was tested on the server. Corresponds to HTTP status
 * code 412.
 * @author Garret Wilson
 */
public class HTTPPreconditionFailedException extends HTTPClientErrorException {

	private static final long serialVersionUID = 1L;

	/** Constructs a new exception. */
	public HTTPPreconditionFailedException() {
		this((String)null); //construct the exception with no message
	}

	/**
	 * Constructs a new exception with the specified detail message.
	 * @param message The detail message.
	 */
	public HTTPPreconditionFailedException(final String message) {
		this(message, null); //construct the class with no cause
	}

	/**
	 * Constructs a new exception with the specified cause, along with a detail message derived from the cause.
	 * @param cause The cause, or <code>null</code> to indicate the cause is nonexistent or unknown.
	 */
	public HTTPPreconditionFailedException(final Throwable cause) {
		this(cause != null ? cause.toString() : null, cause); //create an exception with a generated detail message
	}

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 * @param message The detail message.
	 * @param cause The cause, or <code>null</code> to indicate the cause is nonexistent or unknown.
	 */
	public HTTPPreconditionFailedException(final String message, final Throwable cause) {
		super(SC_PRECONDITION_FAILED, message, cause); //construct the parent class
	}
}
