package com.garretwilson.net.http;

import java.util.*;

import static com.garretwilson.net.http.HTTPConstants.*;
import static com.garretwilson.text.CharacterConstants.*;
import static com.garretwilson.text.FormatUtilities.*;
import com.garretwilson.util.NameValuePair;

/**An authentication challenge or response.
@author Garret Wilson
*/
public abstract class AbstractAuthentication implements Authentication
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
	public AbstractAuthentication(final AuthenticationScheme scheme, final String realm)
	{
		if(scheme==null)	//if the authentication scheme is null
		{
			throw new NullPointerException("Authentication scheme must be provided.");
		}
		this.scheme=scheme;
		this.realm=realm;
	}

}
