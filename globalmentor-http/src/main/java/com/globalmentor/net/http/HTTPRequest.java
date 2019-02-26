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
import com.globalmentor.text.SyntaxException;

/**
 * An HTTP request as defined by <a href="http://www.ietf.org/rfc/rfc2616.txt">RFC 2616</a>, "Hypertext Transfer Protocol -- HTTP/1.1".
 * @author Garret Wilson
 */
public interface HTTPRequest extends HTTPMessage {

	/** @return The HTTP method. */
	public String getMethod();

	/** @return The request URI. */
	public URI getURI();

	/** @return The actual string used to make the request; either an absolute URI or an absolute path, depending on the circumstances. */
	public String getRequestURI();

	/**
	 * Sets the actual string used to make the request.
	 * @param requestURI Either an absolute URI or an absolute path, depending on the circumstances.
	 * @throws IllegalArgumentException if the given URI does not represent an absolute URI or an absolute path.
	 */
	public void setRequestURI(final String requestURI);

	/**
	 * Gets the host information from the header.
	 * @return The host name and optional port of the requested resource, or <code>null</code> if there is no host header.
	 * @throws SyntaxException if the host header does not contain valid host information.
	 * @see HTTP#HOST_HEADER
	 */
	public Host getHost() throws SyntaxException;

	/**
	 * Sets the host header.
	 * @param host The host name and optional port of the requested resource.
	 * @see HTTP#HOST_HEADER
	 */
	public void setHost(final Host host);

	/**
	 * Returns the authorization credentials. This method does not allow the wildcard '*' request-URI for the digest URI parameter.
	 * @return The credentials from the authorization header, or <code>null</code> if there is no such header.
	 * @throws SyntaxException if the given header was not syntactically correct.
	 * @throws IllegalArgumentException if the authorization information is not supported.
	 * @see HTTP#AUTHORIZATION_HEADER
	 */
	public AuthenticateCredentials getAuthorization() throws SyntaxException, IllegalArgumentException;

	/**
	 * Sets the response header containing authentication information.
	 * @param credentials The authentication credentials to present to the server.
	 * @see HTTP#AUTHORIZATION_HEADER
	 */
	public void setAuthorization(final AuthenticateCredentials credentials);

}
