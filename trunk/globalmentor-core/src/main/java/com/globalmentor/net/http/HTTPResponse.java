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

import com.globalmentor.text.SyntaxException;

/**An HTTP response as defined by
<a href="http://www.ietf.org/rfc/rfc2616.txt">RFC 2616</a>,	"Hypertext Transfer Protocol -- HTTP/1.1".
@author Garret Wilson
*/
public interface HTTPResponse extends HTTPMessage
{

	/**@return The status code.*/
	public int getStatusCode();

	/**@return The provided textual representation of the status code.*/
	public String getReasonPhrase();

	/**@return The class of the response.
	@see #getResponseCode()
	*/
	public HTTPResponseClass getResponseClass();

	/**Checks the response code and throws an exception on an error condition.
	@throws HTTPException if the response code represents an error condition.
	@see #getStatusCode()
	@see #getReasonPhrase()
	*/
	public void checkStatus() throws HTTPException;

	/**Returns the authorization challenge.
	This method does not allow the wildcard '*' request-URI for the digest URI parameter.
	@return The credentials from the authorization header,
		or <code>null</code> if there is no such header.
	@throws SyntaxException if the given header was not syntactically correct.
	@throws IllegalArgumentException if the authorization information is not supported. 
	@see HTTP#WWW_AUTHENTICATE_HEADER
	*/
	public AuthenticateChallenge getWWWAuthenticate() throws SyntaxException, IllegalArgumentException;

	/**Sets the response header challenging the client to authenticate itself.
	@param response The HTTP response.
	@param challenge The authenticate challenge to issue to the client.
	@see HTTP#WWW_AUTHENTICATE_HEADER
	*/
	public void setWWWAuthenticate(final AuthenticateChallenge challenge);

}
