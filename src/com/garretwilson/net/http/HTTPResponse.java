package com.garretwilson.net.http;

import com.garretwilson.util.SyntaxException;

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

	/**Checks the response code.
	<p>If the response code represents an error condition for which an HTTP exception
		is available, an HTTP exception is thrown.</p>
	<p>This method calls <code>checkStatus(int, String)</code>, and most subclasses
		should override that method instead of this one.</p>
	@exception HTTPException if the response code represents a known error condition
		for which an HTTP exception class is available.
	*/
	public void checkStatus() throws HTTPException;

	/**Returns the authorization challenge.
	This method does not allow the wildcard '*' request-URI for the digest URI parameter.
	@return The credentials from the authorization header,
		or <code>null</code> if there is no such header.
	@exception SyntaxException if the given header was not syntactically correct.
	@exception IllegalArgumentException if the authorization information is not supported. 
	@see HTTPConstants#WWW_AUTHENTICATE_HEADER
	*/
	public AuthenticateChallenge getWWWAuthenticate() throws SyntaxException, IllegalArgumentException;

	/**Sets the response header challenging the client to authenticate itself.
	@param response The HTTP response.
	@param challenge The authenticate challenge to issue to the client.
	@see HTTPConstants#WWW_AUTHENTICATE_HEADER
	*/
	public void setWWWAuthenticate(final AuthenticateChallenge challenge);

}
