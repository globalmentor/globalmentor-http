package com.garretwilson.net.http;

import java.net.URI;

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
}
