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

import static com.globalmentor.net.http.HTTP.*;

/**
 * Indicates that the request requires user authentication. Corresponds to HTTP status code 401.
 * @author Garret Wilson
 */
public class HTTPUnauthorizedException extends HTTPClientErrorException {

	/** The authenticate challenge to issue to the client. */
	private final AuthenticateChallenge challenge;

	/** @return The authenticate challenge to issue to the client. */
	public AuthenticateChallenge getAuthenticateChallenge() {
		return challenge;
	}

	/**
	 * Constructs a new unauthorized exception with a challenge.
	 * @param challenge The authenticate challenge to issue to the client.
	 * @throws NullPointerException if the authenticate challenge <code>null</code>.
	 */
	public HTTPUnauthorizedException(final AuthenticateChallenge challenge) {
		super(SC_UNAUTHORIZED); //construct parent class
		if(challenge == null) { //if the authenticate challenge is null
			throw new NullPointerException("Authenticate challenge must be provided.");
		}
		this.challenge = challenge;
	}

}