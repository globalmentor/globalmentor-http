package com.garretwilson.net.http;

import java.net.URI;
import java.net.URISyntaxException;

import static com.garretwilson.net.http.HTTPConstants.*;
import static com.garretwilson.net.URIUtilities.*;
import com.garretwilson.util.SyntaxException;

/**The default implementation of an HTTP request as defined by
<a href="http://www.ietf.org/rfc/rfc2616.txt">RFC 2616</a>,	"Hypertext Transfer Protocol -- HTTP/1.1".
@author Garret Wilson
*/
public class DefaultHTTPRequest extends AbstractHTTPMessage implements HTTPRequest
{

	/**The HTTP method.*/
	private final String method;

		/**@return The HTTP method.*/
		public String getMethod() {return method;}

	/**The request URI.*/
	private final URI uri;

		/**@return The request URI.*/
		public URI getURI() {return uri;}
	
	/**Constructs a request with a method, address, and the default HTTP version, 1.1.
	@param method The HTTP method.
	@param uri The absolute URI or absolute path of the request.
	@see #DEFAULT_VERSION
	*/
	public DefaultHTTPRequest(final String method, final URI uri)
	{
		this(method, uri, DEFAULT_VERSION);	//construct the class with the default version
	}

	/**Constructs a request with a method, address, and version.
	@param method The HTTP method.
	@param uri The absolute URI or absolute path of the request.
	@param version The HTTP version being used.
	@exception IllegalArgumentException if the given URI does not represent
		an absolute URI or an absolute path.
	*/
	public DefaultHTTPRequest(final String method, final URI uri, final HTTPVersion version)
	{
		super(version);	//construct the parent class
		if(!uri.isAbsolute() && !isAbsolutePath(uri))	//if the URI is not absolute, and it doesn't represent an absolute path
		{
			throw new IllegalArgumentException("Request URI "+uri+" does represent neither an absolute URI nor an absolute path.");
		}
		this.method=method;
		this.uri=uri;
	}

	/**Sets the host header.
	@param hostURI The host name and optional port of the requested resource.
	@exception IllegalArgumentException if the given URI does not contain only a host and optional port. 
	*/
	public void setHost(final URI hostURI)
	{
		if(!isHost(hostURI))	//if this URI is not just a host and maybe a port
		{
			throw new IllegalArgumentException("URI "+hostURI+" must contain only a host and optional port.");
		}
		setHeader(HOST_HEADER, hostURI.toString());	//set the host
	}

	/**Gets the host information from the header.
	@return The host name and optional port of the requested resource, or <code>null</code> if there is no host header.
	@exception SyntaxException if the host header does not contain only a host and optional port. 
	*/
	public URI getHost() throws SyntaxException
	{
		final String host=getHeader(HOST_HEADER);	//get the host header, if there is one
		if(host!=null)	//if a host is present
		{
			try
			{
				final URI hostURI=new URI(host);	//create a URI from the host header		
				if(!isHost(hostURI))	//if this URI is not just a host and maybe a port
				{
					throw new SyntaxException(host, "URI must contain only a host and optional port.");
				}
				return hostURI;	//return the host URI
			}
			catch(final URISyntaxException uriSyntaxException)	//if the host is not syntactically correct
			{
				throw new SyntaxException(host, uriSyntaxException);
			}
		}
		else	//if no host was given
		{
			return null;	//show that no host is present
		}
	}
}
