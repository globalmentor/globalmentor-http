package com.garretwilson.net.http;

import static com.garretwilson.net.http.HTTPConstants.*;
import com.garretwilson.util.NameValuePair;

/**Indicates that the request requires user authentication. 
Corresponds to HTTP status code 401.
@author Garret Wilson
*/
public class HTTPUnauthorizedException extends HTTPClientErrorException
{

	/**The authenticate challenge to issue to the client.*/
	private final AuthenticateChallenge challenge;

		/**@return The authenticate challenge to issue to the client.*/
		public AuthenticateChallenge getAuthenticateChallenge() {return challenge;}

	/**Constructs a new unauthorized exception with a challenge.
	@param challenge The authenticate challenge to issue to the client.
	@exception NullPointerException if the authenticate challenge <code>null</code>.
	*/
	public HTTPUnauthorizedException(final AuthenticateChallenge challenge)
	{
		super(SC_UNAUTHORIZED);	//construct parent class
		if(challenge==null)	//if the authenticate challenge is null
		{
			throw new NullPointerException("Authenticate challenge must be provided.");
		}
		this.challenge=challenge;
	}

}
