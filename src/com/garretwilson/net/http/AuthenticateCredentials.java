package com.garretwilson.net.http;

/**An object encapsulating credentials in response to an authenticate challenge.
@author Garret Wilson
*/
public interface AuthenticateCredentials extends Authentication
{

	/**@return The ID of the principal for which the credentials purport to provide authentication.*/
	public String getPrincipalID();
}
