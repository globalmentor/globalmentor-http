package com.garretwilson.net.http;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**An encapsulation of digest authenticate credentials of HTTP Digest Access Authentication,
<a href="http://www.ietf.org/rfc/rfc2617.txt">RFC 2617</a>,
	"HTTP Authentication: Basic and Digest Access Authentication", which obsoletes
<a href="http://www.ietf.org/rfc/rfc2069.txt">RFC 2069</a>,
	"An Extension to HTTP : Digest Access Authentication".
@author Garret Wilson
*/
public class DigestAuthenticateCredentials //TODO fix extends DigestAuthenticateChallenge implements AuthenticateCredentials
{

	/**Constructs digest authentication credentials.
	@param realm The realm for which authentication is requested.
	@param nonce The un-hashed server-specific data unique for each challenge.
	@param opaque The opaque challenge data, or <code>null</code> for no opaque data.
	@param algorithm The standard name of the digest algorithm. 
	@exception NoSuchAlgorithmException if the given algorithm is not recognized.
	@exception NullPointerException if the realm or the nonce is <code>null</code>.
	*/
/*TODO fix
	public DigestAuthenticateCredentials(final String username, final String realm, final String nonce, final int nonceCount, final String opaque,
			final String messageQOP, final String cnonce, final String digestURI, final String response, final String algorithm) throws NoSuchAlgorithmException
	{
		super(AuthenticationScheme.DIGEST, realm);	//construct the parent class
		if(nonce==null)	//if the nonce is null
		{
			throw new NullPointerException("Nonce must be provided.");
		}
		this.nonce=nonce;
		this.opaque=opaque;
		messageDigest=MessageDigest.getInstance(algorithm);	//construct the message digest
	}
*/

}
