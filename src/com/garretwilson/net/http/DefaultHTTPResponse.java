package com.garretwilson.net.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import static com.garretwilson.net.http.HTTPConstants.*;
import static com.garretwilson.net.http.HTTPFormatter.*;
import static com.garretwilson.net.http.HTTPParser.*;
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

	/**@return The class of the response.
	@see #getResponseCode()
	*/
	public HTTPResponseClass getResponseClass()
	{
		return HTTPResponseClass.fromStatusCode(getStatusCode());	//get the response class from the status code
	}

	/**Checks the response code.
	<p>If the response code represents an error condition for which an HTTP exception
		is available, an HTTP exception is thrown.</p>
	<p>This method calls <code>checkStatus(int, String)</code>, and most subclasses
		should override that method instead of this one.</p>
	@exception HTTPException if the response code represents a known error condition
		for which an HTTP exception class is available.
	*/
	public void checkStatus() throws HTTPException
	{
//G***del if not needed		checkStatus(getStatusCode(), getReasonPhrase());	//check the status code and reason phrase
		try
		{
			switch(statusCode)	//see which response code this is
			{
				case SC_UNAUTHORIZED:	//401 Forbidden
					throw new HTTPUnauthorizedException(getWWWAuthenticate());
				case SC_FORBIDDEN:	//403 Forbidden
					throw new HTTPForbiddenException(reasonPhrase);
				case SC_NOT_FOUND:	//404 Not Found
					throw new HTTPNotFoundException(reasonPhrase);
				case SC_CONFLICT:	//409 Conflict
					throw new HTTPConflictException(reasonPhrase);
				case SC_GONE:	//410 Gone
					throw new HTTPGoneException(reasonPhrase);
	/*TODO
				   415 (Unsupported Media Type)- The server does not support the request
				   type of the body.
	
				   507 (Insufficient Storage) - The resource does not have sufficient
				   space to record the state of the resource after the execution of this
				   method.
	*/
	
			}
		}
		catch(final SyntaxException syntaxException)
		{
			//TODO decide what to do here
		}
	}

	/**Checks the given HTTP status code.
	If the status code represents an error condition for which an HTTP exception
	is available, an HTTP exception is thrown.
	@param statusCode The HTTP status code to check.
	@param reasonPhrase The response message received with the status code.
	@exception HTTPException if the response code represents a known error condition
		for which an HTTP exception class is available.
	*/
/*G***del if not needed
	public static void checkStatus(final int statusCode, final String reasonPhrase) throws HTTPException
	{
		switch(statusCode)	//see which response code this is
		{
			case SC_UNAUTHORIZED:	//401 Forbidden
				throw new HTTPUnauthorizedException(get);
			case SC_FORBIDDEN:	//403 Forbidden
				throw new HTTPForbiddenException(reasonPhrase);
			case SC_NOT_FOUND:	//404 Not Found
				throw new HTTPNotFoundException(reasonPhrase);
			case SC_CONFLICT:	//409 Conflict
				throw new HTTPConflictException(reasonPhrase);
			case SC_GONE:	//410 Gone
				throw new HTTPGoneException(reasonPhrase);
*/
/*TODO
			   415 (Unsupported Media Type)- The server does not support the request
			   type of the body.

			   507 (Insufficient Storage) - The resource does not have sufficient
			   space to record the state of the resource after the execution of this
			   method.
*/
/*G***del if not needed
		}
	}
*/

	/**Returns the authorization challenge.
	This method does not allow the wildcard '*' request-URI for the digest URI parameter.
	@return The credentials from the authorization header,
		or <code>null</code> if there is no such header.
	@exception SyntaxException if the given header was not syntactically correct.
	@exception IllegalArgumentException if the authorization information is not supported. 
	@see HTTPConstants#WWW_AUTHENTICATE_HEADER
	*/
	public AuthenticateChallenge getWWWAuthenticate() throws SyntaxException, IllegalArgumentException
	{
		final String authenticateHeader=getHeader(WWW_AUTHENTICATE_HEADER); //get the authenticate information
		return authenticateHeader!=null ? parseWWWAuthenticateHeader(authenticateHeader) : null;	//parse the authenticate header, if present
	}

	/**Sets the response header challenging the client to authenticate itself.
	@param challenge The authenticate challenge to issue to the client.
	@see HTTPConstants#WWW_AUTHENTICATE_HEADER
	*/
	public void setWWWAuthenticate(final AuthenticateChallenge challenge)
	{
		setHeader(WWW_AUTHENTICATE_HEADER, formatWWWAuthenticateHeader(new StringBuilder(), challenge).toString());	//set the WWW-Authenticate header
	}

}
