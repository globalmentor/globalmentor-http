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

import static com.globalmentor.java.Objects.*;

/**An authentication challenge or response.
@author Garret Wilson
*/
public abstract class AbstractHTTPAuthentication implements HTTPAuthentication
{

	/**The authentication scheme.*/ 
	private final AuthenticationScheme scheme;

		/**@return The authentication scheme.*/ 
		public AuthenticationScheme getScheme() {return scheme;}

	/**The authentication realm, or <code>null</code> if not known.*/
	private final String realm;

		/**@return The authentication realm, or <code>null</code> if not known.*/
		public String getRealm() {return realm;}

	/**Constructs authentication.
	@param scheme The authentication scheme with which to challenge the client.
	@param realm The authentication realm, or <code>null</code> if not known.
	@throws NullPointerException if the authentication scheme is <code>null</code>.
	*/
	public AbstractHTTPAuthentication(final AuthenticationScheme scheme, final String realm)
	{
		this.scheme=checkInstance(scheme, "Authentication scheme must be provided.");
		this.realm=realm;
	}

}
