package com.garretwilson.net.http;

import java.net.URI;
import java.net.URISyntaxException;

import static com.garretwilson.net.http.HTTPConstants.*;
import static com.garretwilson.net.URIUtilities.*;
import com.garretwilson.util.SyntaxException;

/**The default implementation of an HTTP response as defined by
<a href="http://www.ietf.org/rfc/rfc2616.txt">RFC 2616</a>,	"Hypertext Transfer Protocol -- HTTP/1.1".
@author Garret Wilson
*/
public class DefaultHTTPResponse extends AbstractHTTPMessage implements HTTPResponse
{

	/**The status code.*/
	private final int statusCode;

		/**@return The status code.*/
		public int getStatusCode() {return statusCode;}

	/**The provided textual representation of the status code.*/
	private final String reasonPhrase;

		/**@return The provided textual representation of the status code.*/
		public String getReasonPhrase() {return reasonPhrase;}

	/**Constructs a request with a status code, reason phrase, and the default HTTP version, 1.1.
	@param statusCode The status code.
	@param reasonPhrase The provided textual representation of the status code.
	@see #DEFAULT_VERSION
	*/
	public DefaultHTTPResponse(final int statusCode, final String reasonPhrase)
	{
		this(DEFAULT_VERSION, statusCode, reasonPhrase);	//construct the class with the default version
	}

	/**Constructs a response with a version, status code, and reason phrase.
	@param version The HTTP version being used.
	@param statusCode The status code.
	@param reasonPhrase The provided textual representation of the status code.
	*/
	public DefaultHTTPResponse(final HTTPVersion version, final int statusCode, final String reasonPhrase)
	{
		super(version);	//construct the parent class
		this.statusCode=statusCode;
		this.reasonPhrase=reasonPhrase;
	}

}
