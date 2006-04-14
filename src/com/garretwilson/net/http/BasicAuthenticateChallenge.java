package com.garretwilson.net.http;

/**An encapsulation of a basic authenticate challenge of HTTP Basic Access Authentication,
<a href="http://www.ietf.org/rfc/rfc2617.txt">RFC 2617</a>,
	"HTTP Authentication: Basic and Digest Access Authentication", which obsoletes
<a href="http://www.ietf.org/rfc/rfc2069.txt">RFC 2069</a>,
	"An Extension to HTTP : Digest Access Authentication".
@author Garret Wilson
*/
public class BasicAuthenticateChallenge extends AbstractAuthenticateChallenge
{

	/**Constructs a basic authentication challenge.
	@param realm The realm for which authentication is requested.
	@exception NullPointerException if the authentication realm is <code>null</code>.
	*/
	public BasicAuthenticateChallenge(final String realm)
	{
		super(AuthenticationScheme.BASIC, realm);	//construct the parent class
	}

}
