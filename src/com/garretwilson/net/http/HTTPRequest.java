package com.garretwilson.net.http;

import java.net.URI;

import com.garretwilson.util.SyntaxException;

/**An HTTP request as defined by
<a href="http://www.ietf.org/rfc/rfc2616.txt">RFC 2616</a>,	"Hypertext Transfer Protocol -- HTTP/1.1".
@author Garret Wilson
*/
public interface HTTPRequest extends HTTPMessage
{

	/**@return The HTTP method.*/
	public String getMethod();

	/**@return The request URI.*/
	public URI getURI();
	
	/**Sets the host header.
	@param hostURI The host name and optional port of the requested resource.
	@exception IllegalArgumentException if the given URI does not contain only a host and optional port. 
	*/
	public void setHost(final URI hostURI);

	/**Gets the host information from the header.
	@return The host name and optional port of the requested resource, or <code>null</code> if there is no host header.
	@exception SyntaxException if the host header does not contain only a host and optional port. 
	*/
	public URI getHost() throws SyntaxException;

}
