package com.garretwilson.net.http;

/**An authentication challenge or response.
@author Garret Wilson
*/
public interface Authentication
{

	/**@return The authentication scheme.*/ 
	public AuthenticationScheme getScheme();

	/**@return The authentication realm, or <code>null</code> if not known.*/
	public String getRealm();

}
