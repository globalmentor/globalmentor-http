package com.garretwilson.net.http;

import com.garretwilson.net.AbstractClient;
import com.garretwilson.net.Authenticable;
import com.garretwilson.net.Host;

/**Represents the identify of a group of related HTTP connections.
Keeps a cache of authentication information for visited domains and realms.
@author Garret Wilson
*/
public class HTTPClient extends AbstractClient
{

	/**The default instance of the HTTP client.*/
	private static HTTPClient instance=null;

	/**@return The default instance of this class, or <code>null</code> if there is no default instance.*/
	protected AbstractClient getDefaultInstance() {return instance;}

	/**@return The default instance of the HTTP client.*/
	public static HTTPClient getInstance()
	{
		if(instance==null)	//if we have not yet created an instance
		{
			instance=new HTTPClient();	//create a new client			
		}
		return instance;	//return the shared instance of the client
	}

	/**Default constructor with no authenticator.*/
	public HTTPClient()
	{
		this(null);	//construct the class with no authenticator
	}

	/**Authenticator constructor.
	@param authenticator The authenticator to use for this client, or <code>null</code> if the default authenticator should be used if available. 
	*/
	public HTTPClient(final Authenticable authenticator)
	{
		super(authenticator);	//construct the parent class
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

	/**Creates an unsecure connection to a host.
	@param host The host to which to connect.
	*/
	public HTTPClientTCPConnection createConnection(final Host host)
	{
		return createConnection(host, false);	//create and return a connection that is not secure
	}

	/**Creates a connection to a host that is optionally secure.
	@param host The host to which to connect.
	@param secure Whether the connection should be secure.
	*/
	public HTTPClientTCPConnection createConnection(final Host host, final boolean secure)
	{
		return new HTTPClientTCPConnection(this, host, secure);	//return a new connection to the given host
	}

}
