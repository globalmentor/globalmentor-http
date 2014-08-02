/*
 * Copyright © 1996-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.globalmentor.net.http;

import java.net.URI;

import com.globalmentor.net.Host;
import static com.globalmentor.net.URIs.*;
import static com.globalmentor.net.http.HTTP.*;
import static com.globalmentor.net.http.HTTPFormatter.*;
import static com.globalmentor.net.http.HTTPParser.*;
import com.globalmentor.text.SyntaxException;

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

	/**The actual string used to make the request; either an absolute URI or an absolute path, depending on the circumstances.*/
	private String requestURI;

		/**@return The actual string used to make the request; either an absolute URI or an absolute path, depending on the circumstances.*/
		public String getRequestURI() {return requestURI;}

		/**Sets the actual string used to make the request.
		@param requestURI Either an absolute URI or an absolute path, depending on the circumstances.
		@throws IllegalArgumentException if the given URI does not represent
			an absolute URI or an absolute path.
		*/
		public void setRequestURI(final String requestURI)
		{
			final URI uri=URI.create(requestURI);	//create a URI from the request URI TODO check for "*"
			if(!uri.isAbsolute() && !isAbsolutePath(uri))	//if the URI is not absolute, and it doesn't represent an absolute path
			{
				throw new IllegalArgumentException("Request URI "+uri+" does represent neither an absolute URI nor an absolute path.");
			}
			this.requestURI=requestURI;	//save the request URI
		}

	/**Constructs a request with a method, address, and the default HTTP version, 1.1.
	@param method The HTTP method.
	@param uri The absolute URI of the request.
	@throws IllegalArgumentException if the given URI does not represent
		an absolute URI with an absolute path.
	@see #DEFAULT_VERSION
	*/
	public DefaultHTTPRequest(final String method, final URI uri)
	{
		this(method, uri, DEFAULT_VERSION);	//construct the class with the default version
	}

	/**Constructs a request with a method, address, and version.
	@param method The HTTP method.
	@param uri The absolute URI of the request.
	@param version The HTTP version being used.
	@throws IllegalArgumentException if the given URI does not represent
		an absolute URI with an absolute path.
	*/
	public DefaultHTTPRequest(final String method, final URI uri, final HTTPVersion version)
	{
		super(version);	//construct the parent class
		if(!uri.isAbsolute())	//if the URI is not absolute
		{
			throw new IllegalArgumentException("Request URI "+uri+" does not represent an absolute URI.");
		}
		this.method=method;
		this.uri=uri;
		setRequestURI(getRawPathQueryFragment(uri));	//default to the unencoded path?query#fragment as the HTTP request URI
	}

	/**Gets the host information from the header.
	@return The host name and optional port of the requested resource, or <code>null</code> if there is no host header.
	@throws SyntaxException if the host header does not contain valid host information. 
	@see HTTP#HOST_HEADER
	*/
	public Host getHost() throws SyntaxException
	{
		final String host=getHeader(HOST_HEADER);	//get the host header, if there is one
		if(host!=null)	//if a host is present
		{
			try
			{
				return new Host(host);	//construct and return a new host
			}
			catch(final IllegalArgumentException illegalArgumentException)	//if the host is not syntactically correct
			{
				throw new SyntaxException(illegalArgumentException, host);
			}
		}
		else	//if no host was given
		{
			return null;	//show that no host is present
		}
	}

	/**Sets the host header.
	@param host The host name and optional port of the requested resource.
	@see HTTP#HOST_HEADER
	*/
	public void setHost(final Host host)
	{
		setHeader(HOST_HEADER, host.toString());	//set the host
	}

	/**Returns the authorization credentials.
	This method does not allow the wildcard '*' request-URI for the digest URI parameter.
	@return The credentials from the authorization header,
		or <code>null</code> if there is no such header.
	@throws SyntaxException if the given header was not syntactically correct.
	@throws IllegalArgumentException if the authorization information is not supported. 
	@see HTTP#AUTHORIZATION_HEADER
	*/
	public AuthenticateCredentials getAuthorization() throws SyntaxException, IllegalArgumentException
	{
		final String authorizationHeader=getHeader(AUTHORIZATION_HEADER); //get the authorization information
		return authorizationHeader!=null ? parseAuthorizationHeader(authorizationHeader) : null;	//parse the authorization header, if present
	}

	/**Sets the response header containing authentication information.
	@param credentials The authentication credentials to present to the server.
	@see HTTP#AUTHORIZATION_HEADER
	*/
	public void setAuthorization(final AuthenticateCredentials credentials)
	{
		setHeader(AUTHORIZATION_HEADER, formatAuthorizationHeader(new StringBuilder(), credentials).toString());	//set the Authenticate header
	}

}
