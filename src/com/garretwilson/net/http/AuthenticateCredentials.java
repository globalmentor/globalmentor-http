package com.garretwilson.net.http;

import static com.garretwilson.net.http.HTTPConstants.*;

/**An object encapsulating credentials in response to an authenticate challenge.
@author Garret Wilson
*/
public interface AuthenticateCredentials
{

	/**@return The authentication scheme of the credentials.*/ 
	public AuthenticationScheme getScheme();

}
