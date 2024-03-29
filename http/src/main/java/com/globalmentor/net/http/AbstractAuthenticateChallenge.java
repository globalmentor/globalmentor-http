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
 * An abstract encapsulation of a challenge to be sent back to client.
 * @author Garret Wilson
 */
public abstract class AbstractAuthenticateChallenge extends AbstractHTTPAuthentication implements AuthenticateChallenge {

	/**
	 * Constructs a authentication challenge.
	 * @param scheme The authentication scheme with which to challenge the client.
	 * @param realm The realm for which authentication is requested.
	 * @throws NullPointerException if the authentication scheme and/or realm is <code>null</code>.
	 */
	public AbstractAuthenticateChallenge(final AuthenticationScheme scheme, final String realm) {
		super(scheme, realm); //construct the parent class, which checks to see if the scheme is null
		if(realm == null) { //if the realm is null
			throw new NullPointerException("Realm must be provided.");
		}
	}

}
