package com.garretwilson.net.http;

import java.util.*;
import static java.util.Collections.*;
import java.net.*;

import com.garretwilson.net.Authenticable;

/**Represents the identify of a group of related HTTP connections.
Keeps a cache of authentication information for visited domains and realms.
@author Garret Wilson
*/
public class HTTPClient
{

	/**The default instance of the HTTP client.*/
	private static HTTPClient instance=null;

	/**@return The default instance of the HTTP client.*/
	public static HTTPClient getInstance()
	{
		if(instance==null)	//if we have not yet created an instance
		{
			instance=new HTTPClient();	//create a new client			
		}
		return instance;	//return the shared instance of the client
	}

	private Authenticable authenticator=null;

	public void setAuthenticator(final Authenticable authenticable) {authenticator=authenticable;}	//G***testing; probably put this in the HTTPClient, when that is implemented
	
	/**@return The authenticator associated with this client or, if there is no authenticator defined,
	 	the authenticator associated with the default instance of the client.
	*/
	protected Authenticable getAuthenticator()
	{
		if(authenticator!=null)
			return authenticator;
		else if(getInstance()!=this)
			return getInstance().getAuthenticator();
		else
			return null;
	}


	//TODO make these maps synchronized
	/**The map of passwords keyed to users, keyed to realms, keyed to root URIs.*/
	private final Map<URI, Map<String, Map<String, char[]>>> authenticationMap=new HashMap<URI, Map<String, Map<String, char[]>>>();	//TODO later store digest H(A1) information rather than raw passwords

	/**Retrieves any users for which authentication information is stored for a given realm of a given root URI.
	@param rootURI The root URI of the domain governing the realms and user passwords.
	@param realm The realm of protection.
	@return A read-only set of usernames for which authentication information is stored in this for the given realm of the given root URI.
	*/
	public Set<String> getUsernames(final URI rootURI, final String realm)
	{
		final Map<String, Map<String, char[]>> realmMap=authenticationMap.get(rootURI);	//get the map with realm keys
		if(realmMap!=null)	//if we found this realm
		{
			final Map<String, char[]> userMap=realmMap.get(realm);	//get the map of users and passwords
			return unmodifiableSet(userMap.keySet());	//return the set of usernames for this domain and realm
		}
		return emptySet();	//we don't have authentication information for any users for this domain and realm
	}

	/**Retrieves the password stored for a given user in a given realm of a given root URI.
	@param rootURI The root URI of the domain governing the realms and user passwords.
	@param realm The realm of protection.
	@param username The user for which a password is stored.
	@return The password of the given user, or <code>null</code> if no password is present for the given root URI, realm, and user.
	*/
	public char[] getPassword(final URI rootURI, final String realm, final String username)
	{
		final Map<String, Map<String, char[]>> realmMap=authenticationMap.get(rootURI);	//get the map with realm keys
		if(realmMap!=null)	//if we found this realm
		{
			final Map<String, char[]> userMap=realmMap.get(realm);	//get the map of users and passwords
			if(userMap!=null)	//if there is a map for useres
			{
				final char[] password=userMap.get(username);	//get the password for this user
				return password;	//return the password
			}
		}
		return null;	//indicate that we don't have a password stored
	}

	/**Caches the password for a given user in a given realm of a given root URI.
	@param rootURI The root URI of the domain governing the realms and user passwords.
	@param realm The realm of protection.
	@param username The user for which a password is stored.
	@param password The password of the given user.
	*/
	public void putPassword(final URI rootURI, final String realm, final String username, final char[] password)
	{
		Map<String, Map<String, char[]>> realmMap=authenticationMap.get(rootURI);	//get the map with realm keys
		if(realmMap==null)	//if we don't have a map for this domain
		{
			realmMap=new HashMap<String, Map<String, char[]>>();	//create a new map keyed to realms
			authenticationMap.put(rootURI, realmMap);	//key the map of realms to the domain
		}
		Map<String, char[]> userMap=realmMap.get(realm);	//get the map of users and passwords
		if(userMap==null)	//if we don't have a map for this realm
		{
			userMap=new HashMap<String, char[]>();	//create a new map of users and passwords
			realmMap.put(realm, userMap);	//key the map of users to the realm
		}
		userMap.put(username, password);	//store the password in the map, keyed to the user
	}

	/**Removes the password stored for a given user in a given realm of a given root URI.
	@param rootURI The root URI of the domain governing the realms and user passwords.
	@param realm The realm of protection.
	@param username The user for which a password is stored.
	*/
	public void removePassword(final URI rootURI, final String realm, final String username)
	{
		final Map<String, Map<String, char[]>> realmMap=authenticationMap.get(rootURI);	//get the map with realm keys
		if(realmMap!=null)	//if we found this realm
		{
			final Map<String, char[]> userMap=realmMap.get(realm);	//get the map of users and passwords
			if(userMap!=null)	//if there is a map for useres
			{
				userMap.remove(username);	//remove any password stored for this user
				if(userMap.size()==0)	//if we removed the last password
				{
					realmMap.remove(userMap);	//remove the user map from the map of users keyed to realms
					if(realmMap.size()==0)	//if we removed the last realm
					{
						authenticationMap.remove(realmMap);	//remove the map of users keyed to realms
					}
				}
			}
		}
	}

}
