package com.garretwilson.net.http;

import java.util.*;

import static com.garretwilson.net.http.HTTPConstants.*;
import com.garretwilson.util.NameValuePair;
import static com.garretwilson.text.FormatUtilities.*;

/**An abstract encapsulation of a challenge to be sent back to client.
@author Garret Wilson
*/
public abstract class AbstractAuthenticateChallenge extends AbstractHTTPAuthentication implements AuthenticateChallenge
{

	/**Constructs a authentication challenge.
	@param scheme The authentication scheme with which to challenge the client.
	@param realm The realm for which authentication is requested.
	@exception NullPointerException if the authentication scheme and/or realm is <code>null</code>.
	*/
	public AbstractAuthenticateChallenge(final AuthenticationScheme scheme, final String realm)
	{
		super(scheme, realm);	//construct the parent class, which checks to see if the scheme is null
		if(realm==null)	//if the realm is null
		{
			throw new NullPointerException("Realm must be provided.");
		}
	}

	/**@return The authorization parameters for this challenge.
	This version only returns the realm parameter.
	Child classes may override this method and append other parameters to the list before returning it.
	*/
/*G***del
	public List<NameValuePair<String, String>> getParameters()
	{
		final List<NameValuePair<String, String>> parameterList=new ArrayList<NameValuePair<String, String>>();	//create the list of parameters
		parameterList.add(new NameValuePair<String, String>(REALM_PARAMETER, getRealm()));	//add the realm parameter
		return parameterList;	//return the list of parameters 
	}
*/

	/**@return A string representation of the challenge.*/
/*G***del
	public final String toString()
	{
		final StringBuilder stringBuilder=new StringBuilder();
		stringBuilder.append(getScheme()).append(SP);	//authentication scheme
		final List<NameValuePair<String, String>> parameters=getParameters();	//get the challenge parameters
		formatAttributes(stringBuilder, parameters.toArray(new NameValuePair[parameters.size()]));	//parameters
		return stringBuilder.toString();
	}
*/
}
