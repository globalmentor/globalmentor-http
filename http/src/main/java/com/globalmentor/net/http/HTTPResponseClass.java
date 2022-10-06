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
 * The class of the response according to a response code as defined by <a href="https://www.ietf.org/rfc/rfc2616.txt">RFC 2616</a>,
 * "Hypertext Transfer Protocol -- HTTP/1.1".
 * @author Garret Wilson
 */
public enum HTTPResponseClass {

	/** 1xx: Informational - Request received, continuing process. */
	INFORMATIONAL,

	/** 2xx: Success - The action was successfully received, understood, and accepted. */
	SUCCESS,

	/** 3xx: Redirection - Further action must be taken in order to complete the request. */
	REDIRECTION,

	/** 4xx: Client Error - The request contains bad syntax or cannot be fulfilled. */
	CLIENT_ERROR,

	/** 5xx: Server Error - The server failed to fulfill an apparently valid request. */
	SERVER_ERROR;

	/**
	 * @return <code>true</code> if the response class is a client or server error.
	 * @see #CLIENT_ERROR
	 * @see #SERVER_ERROR
	 */
	public boolean isError() {
		return this == CLIENT_ERROR || this == SERVER_ERROR; //see if this is one of the two error classes
	}

	/**
	 * Returns the response class of a given status code.
	 * @param statusCode The status code of a response.
	 * @return The class of a response with the given status code.
	 * @throws IllegalArgumentException if the given status code does not fall within one of the response classes defined by RFC 2616.
	 */
	public static HTTPResponseClass fromStatusCode(final int statusCode) {
		final int ordinal = (statusCode / 100) - 1; //get the first digit of the status code, which represents the ordinal plus one
		final HTTPResponseClass[] values = HTTPResponseClass.values(); //get the available values
		if(ordinal >= 0 && ordinal < values.length) { //if our ordinal is in the range of response classes
			return values[ordinal]; //return this response class			
		} else { //if there is no corresponding response class
			throw new IllegalArgumentException("Unrecognized status code " + statusCode + ".");
		}
	}
}
