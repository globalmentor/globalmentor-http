package com.garretwilson.net.http;

import java.net.URI;

import com.garretwilson.net.Host;
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

	/**Gets the host information from the header.
	@return The host name and optional port of the requested resource, or <code>null</code> if there is no host header.
	@exception SyntaxException if the host header does not contain valid host information. 
	@see HTTPConstants#HOST_HEADER
	*/
	public Host getHost() throws SyntaxException;

	/**Sets the host header.
	@param host The host name and optional port of the requested resource.
	@see HTTPConstants#HOST_HEADER
	*/
	public void setHost(final Host host);

	/**Returns the authorization credentials.
	This method does not allow the wildcard '*' request-URI for the digest URI parameter.
	@return The credentials from the authorization header,
		or <code>null</code> if there is no such header.
	@exception SyntaxException if the given header was not syntactically correct.
	@exception IllegalArgumentException if the authorization information is not supported. 
	@see HTTPConstants#AUTHORIZATION_HEADER
	*/
	public AuthenticateCredentials getAuthorization() throws SyntaxException, IllegalArgumentException;

	/**Sets the response header containing authentication information.
	@param credentials The authentication credentials to present to the server.
	@see HTTPConstants#AUTHORIZATION_HEADER
	*/
	public void setAuthorization(final AuthenticateCredentials credentials);

}
