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
 * An encapsulation of a basic authenticate challenge of HTTP Basic Access Authentication, <a href="https://www.ietf.org/rfc/rfc2617.txt">RFC 2617</a>,
 * "HTTP Authentication: Basic and Digest Access Authentication", which obsoletes <a href="https://www.ietf.org/rfc/rfc2069.txt">RFC 2069</a>,
 * "An Extension to HTTP : Digest Access Authentication".
 * @author Garret Wilson
 */
public class BasicAuthenticateChallenge extends AbstractAuthenticateChallenge {

	/**
	 * Constructs a basic authentication challenge.
	 * @param realm The realm for which authentication is requested.
	 * @throws NullPointerException if the authentication realm is <code>null</code>.
	 */
	public BasicAuthenticateChallenge(final String realm) {
		super(AuthenticationScheme.BASIC, realm); //construct the parent class
	}

}
