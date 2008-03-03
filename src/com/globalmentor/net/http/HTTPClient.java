package com.globalmentor.net.http;

import java.net.PasswordAuthentication;

import com.globalmentor.net.AbstractClient;
import com.globalmentor.net.Authenticable;
import com.globalmentor.net.Host;

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

	/**Creates an unsecure connection to a host.
	@param host The host to which to connect.
	*/
	public HTTPClientTCPConnection createConnection(final Host host)
	{
		return createConnection(host, false);	//create and return a connection that is not secure
	}

	/**Creates an unsecure connection to a host, using connection-specific password authentication.
	@param host The host to which to connect.
	@param passwordAuthentication The connection-specific password authentication, or <code>null</code> if there should be no connection-specific password authentication.
	*/
	public HTTPClientTCPConnection createConnection(final Host host, final PasswordAuthentication passwordAuthentication)
	{
		return createConnection(host, passwordAuthentication, false);	//create and return a connection that is not secure
	}

	/**Creates a connection to a host that is optionally secure.
	@param host The host to which to connect.
	@param secure Whether the connection should be secure.
	*/
	public HTTPClientTCPConnection createConnection(final Host host, final boolean secure)
	{
		return createConnection(host, null, secure);	//create and return an optionally secure connection with no connection-specific authentication
	}

	/**Creates a connection to a host that is optionally secure, using connection-specific password authentication.
	@param host The host to which to connect.
	@param passwordAuthentication The connection-specific password authentication, or <code>null</code> if there should be no connection-specific password authentication.
	@param secure Whether the connection should be secure.
	*/
	public HTTPClientTCPConnection createConnection(final Host host, final PasswordAuthentication passwordAuthentication, final boolean secure)
	{
		return new HTTPClientTCPConnection(this, host, passwordAuthentication, secure);	//return a new connection to the given host
	}

}
