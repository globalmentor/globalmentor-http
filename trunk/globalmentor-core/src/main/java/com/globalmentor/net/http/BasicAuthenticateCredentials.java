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

/**An encapsulation of basic authenticate credentials of HTTP Basic Access Authentication,
<a href="http://www.ietf.org/rfc/rfc2617.txt">RFC 2617</a>,
	"HTTP Authentication: Basic and Digest Access Authentication", which obsoletes
<a href="http://www.ietf.org/rfc/rfc2069.txt">RFC 2069</a>,
	"An Extension to HTTP : Digest Access Authentication".
@author Garret Wilson
*/
public class BasicAuthenticateCredentials extends AbstractHTTPAuthentication implements AuthenticateCredentials
{

	/**The username.*/
	private final String username;

		/**@return The username.*/
		public String getUsername() {return username;}

	/**The password.*/
	private char[] password;
	
		/**@return The password.*/
		public char[] getPassword() {return password;}

	/**@return The ID of the principal for which the credentials purport to provide authentication.*/
	public String getPrincipalID()
	{
		return getUsername();	//return the username
	}

	/**Full credential constructor.
	@param username The username of the principal submitting the credentials
	@param realm The realm for which authentication is requested.
	@param password The user password.
	@throws NullPointerException if the username, realm, and/or password <code>null</code>.
	*/
	protected BasicAuthenticateCredentials(final String username, final String realm, final char[] password)
	{
		super(AuthenticationScheme.BASIC, checkInstance(realm, "Realm must be provided."));	//construct the parent class
		this.username=checkInstance(username, "Username must be provided.");
		this.password=checkInstance(password, "Password must be provided.");
	}

}
