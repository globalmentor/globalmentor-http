package com.garretwilson.net.http;

import static com.garretwilson.lang.Objects.*;

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
	@exception NullPointerException if the username, realm, and/or password <code>null</code>.
	*/
	protected BasicAuthenticateCredentials(final String username, final String realm, final char[] password)
	{
		super(AuthenticationScheme.BASIC, checkInstance(realm, "Realm must be provided."));	//construct the parent class
		this.username=checkInstance(username, "Username must be provided.");
		this.password=checkInstance(password, "Password must be provided.");
	}

}
