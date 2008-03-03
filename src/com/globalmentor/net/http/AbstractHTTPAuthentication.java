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
	@exception NullPointerException if the authentication scheme is <code>null</code>.
	*/
	public AbstractHTTPAuthentication(final AuthenticationScheme scheme, final String realm)
	{
		this.scheme=checkInstance(scheme, "Authentication scheme must be provided.");
		this.realm=realm;
	}

}
