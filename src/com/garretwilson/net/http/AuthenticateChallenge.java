package com.garretwilson.net.http;

import java.util.*;

import static com.garretwilson.net.http.HTTPConstants.*;
import static com.garretwilson.text.CharacterConstants.COMMA_CHAR;
import static com.garretwilson.text.CharacterConstants.EQUALS_SIGN_CHAR;
import static com.garretwilson.text.CharacterConstants.QUOTATION_MARK_CHAR;
import static com.garretwilson.text.CharacterConstants.SPACE_CHAR;
import static com.garretwilson.text.FormatUtilities.*;
import com.garretwilson.util.NameValuePair;

/**An encapsulation of a challenge to be sent back to client.
@author Garret Wilson
*/
public abstract class AuthenticateChallenge
{

	/**The authentication scheme with which to challenge the client.*/ 
	private final AuthenticationScheme scheme;

		/**@return The authentication scheme with which to challenge the client.*/ 
		public AuthenticationScheme getScheme() {return scheme;}

	/**The realm for which authentication is requested.*/
	private final String realm;

		/**@return The realm for which authentication is requested.*/
		public String getRealm() {return realm;}

	/**Constructs a authentication challenge.
	@param scheme The authentication scheme with which to challenge the client.
	@param realm The realm for which authentication is requested.
	@exception NullPointerException if the authentication scheme and/or realm is <code>null</code>.
	*/
	public AuthenticateChallenge(final AuthenticationScheme scheme, final String realm)
	{
		if(scheme==null)	//if the authentication scheme is null
		{
			throw new NullPointerException("Authentication scheme must be provided.");
		}
		if(realm==null)	//if the realm is null
		{
			throw new NullPointerException("Realm must be provided.");
		}
		this.scheme=scheme;
		this.realm=realm;
	}

	/**@return The authorization parameters for this challenge.
	This version only returns the realm parameter.
	Child classes may override this method and append other parameters to the list before returning it.
	*/
	public List<NameValuePair<String, String>> getParameters()
	{
		final List<NameValuePair<String, String>> parameterList=new ArrayList<NameValuePair<String, String>>();	//create the list of parameters
		parameterList.add(new NameValuePair<String, String>(REALM_PARAMETER, realm));	//add the realm parameter
		return parameterList;	//return the list of parameters 
	}

	/**@return A string represntation of the challenge.*/
	public final String toString()
	{
		final StringBuilder stringBuilder=new StringBuilder();
		stringBuilder.append(getScheme()).append(SPACE_CHAR);	//authentication scheme
		final List<NameValuePair<String, String>> parameters=getParameters();	//get the challenge parameters
		formatAttributes(stringBuilder, parameters.toArray(new NameValuePair[parameters.size()]));	//parameters
		return stringBuilder.toString();
	}
}
