package com.garretwilson.net.http;

import java.util.*;
import static java.util.Collections.*;
import java.net.*;

import com.garretwilson.net.Authenticable;
import com.garretwilson.net.Host;

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

	/**The authenticator object used to retrieve client authentication.*/
	private Authenticable authenticator=null;

	/**Sets the authenticator object used to retrieve client authentication.
	@param authenticable The object to retrieve authentication information regarding a client.
	*/
	public void setAuthenticator(final Authenticable authenticable) {authenticator=authenticable;}
	
	/**@return The authenticator associated with this client or, if there is no authenticator defined,
	 	the authenticator associated with the default instance of the client.
	*/
	protected Authenticable getAuthenticator()
	{
		if(authenticator!=null)	//if we have an authenticator defined
			return authenticator;		//return the authenticator
		else if(getInstance()!=this)	//if the default instance is not this object
			return getInstance().getAuthenticator();	//ask the default instance for the authenticator
		else	//if this class is the defefault client
			return null;	//we've already determined we don't have an authenticator, so return null
	}

	/**The ID of the user to which this client is restricted, or <code>null</code> if this client is not restricted to a single user.*/
	private String username;

		/**@return The ID of the user to which this client is restricted, or <code>null</code> if this client is not restricted to a single user.*/
		public String getUsername() {return username;}

		/**Restricts this client to particular user.
		@param username The ID of the user to which this client is restricted,
			or <code>null</code> if this client should not restricted to a single user.
		*/
		public void setUsername(final String username) {this.username=username;}

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

	/**Creates a connection from a URI.
	@param uri The URI indicating the host to which to connect.
	@exception IllegalArgumentException if the given URI does not contain a valid host.
	*/
/*G***del if not needed
	public HTTPClientTCPConnection createConnection(final URI uri)
	{
		return new HTTPClientTCPConnection(this, uri);	//return a new connection to the given URI
	}
*/

	/**Creates a connection to a host.
	@param host The host to which to connect.
	*/
	public HTTPClientTCPConnection createConnection(final Host host)
	{
		return new HTTPClientTCPConnection(this, host);	//return a new connection to the given host
	}

	/**Determines password information in relation to a given URI and description.
	@param uri The URI for which authentication is requested.
	@param prompt A description of the authentication.
	@return The password authentication collected from the user, or <code>null</code> if none is provided.
	*/
	public PasswordAuthentication getPasswordAuthentication(final URI uri, final String prompt)
	{
		final Authenticable authenticator=getAuthenticator();	//see if an authenticator has been specified
		if(authenticator!=null)	//if we have an authenticator
		{
			return authenticator.getPasswordAuthentication(uri, prompt, username);	//ask the authenticator for the password, restricting authentication to a given user if appropriate 
		}
		else
		{
			/*G***fix
			final DigestAuthenticateChallenge digestChallenge=(DigestAuthenticateChallenge)challenge;	//get the challenge as a digest challenge
		final Host host=request.getHost();	//get the host of the request, as we may have been redirected
		final int port=host.getPort()>=0 ? host.getPort() : DEFAULT_PORT;	//TODO maybe force host to have a port
Debug.trace("getting password authentication");
final PasswordAuthentication passwordAuthentication=Authenticator.requestPasswordAuthentication(host.getName(), getInetAddress(), port,
response.getVersion().toString(), "You must enter a username and password to access this resource at \""+digestChallenge.getRealm()+"\".", digestChallenge.getScheme().toString());
Debug.trace("got password authentication", passwordAuthentication);
*/
		}
		return null;	//we couldn't retrieve a password
	}

}
