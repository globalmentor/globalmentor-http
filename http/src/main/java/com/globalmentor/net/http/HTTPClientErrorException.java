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

/**
 * Indicates that the client has erred. Corresponds to HTTP status codes 4xx.
 * @author Garret Wilson
 */
public abstract class HTTPClientErrorException extends HTTPException {

	/**
	 * Constructs a new exception with the specified status code.
	 * @param statusCode The HTTP status code to return in the request.
	 * @throws IllegalArgumentException if the status code is not a 4xx status code.
	 */
	public HTTPClientErrorException(final int statusCode) {
		this(statusCode, (String)null); //construct the exception with the status code and no message
	}

	/**
	 * Constructs a new exception with the specified status code and detail message.
	 * @param statusCode The HTTP status code to return in the request.
	 * @param message The detail message.
	 * @throws IllegalArgumentException if the status code is not a 4xx status code.
	 */
	public HTTPClientErrorException(final int statusCode, final String message) {
		this(statusCode, message, null); //construct the class with no cause
	}

	/**
	 * Constructs a new exception with the specified status code and cause, along with a detail message derived from the cause.
	 * @param statusCode The HTTP status code to return in the request.
	 * @param cause The cause, or <code>null</code> to indicate the cause is nonexistent or unknown.
	 * @throws IllegalArgumentException if the status code is not a 4xx status code.
	 */
	public HTTPClientErrorException(final int statusCode, final Throwable cause) {
		this(statusCode, cause != null ? cause.toString() : null, cause); //create an exception with a generated detail message
	}

	/**
	 * Constructs a new exception with the specified status code, detail message, and cause.
	 * @param statusCode The HTTP status code to return in the request.
	 * @param message The detail message.
	 * @param cause The cause, or <code>null</code> to indicate the cause is nonexistent or unknown.
	 * @throws IllegalArgumentException if the status code is not a 4xx status code.
	 */
	public HTTPClientErrorException(final int statusCode, final String message, final Throwable cause) {
		super(statusCode, message, cause); //construct the parent class
		if(statusCode < 400 || statusCode >= 500) { //if this is not a client error status code
			throw new IllegalArgumentException("Invalid client error status code " + statusCode);
		}
	}

}
