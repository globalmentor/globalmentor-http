package com.garretwilson.net.http;

/**The class of the response according to a response code as defined by
<a href="http://www.ietf.org/rfc/rfc2616.txt">RFC 2616</a>,	"Hypertext Transfer Protocol -- HTTP/1.1".
@author Garret Wilson
*/
public enum HTTPResponseClass
{

	/**1xx: Informational - Request received, continuing process.*/
	INFORMATIONAL,
	
	/**2xx: Success - The action was successfully received, understood, and accepted.*/
	SUCCESS,
	
	/**3xx: Redirection - Further action must be taken in order to complete the request.*/
	REDIRECTION,

	/**4xx: Client Error - The request contains bad syntax or cannot be fulfilled.*/
	CLIENT_ERROR,

	/**5xx: Server Error - The server failed to fulfill an apparently valid request.*/
	SERVER_ERROR;

	/**@return <code>true</code> if the response class is a client or server error.
	@see #CLIENT_ERROR
	@see #SERVER_ERROR
	*/
	public boolean isError()
	{
		return this==CLIENT_ERROR || this==SERVER_ERROR;	//see if this is one of the two error classes
	}
	
	/**Returns the response class of a given status code.
	@param statusCode The status code of a response.
	@return The class of a response with the given status code.
	@exception IllegalArgumentException if the given status code does not fall
		within one of the response classes defined by RFC 2616.
	*/
	public static HTTPResponseClass fromStatusCode(final int statusCode)
	{
		final int ordinal=(statusCode/100)-1;	//get the first digit of the status code, which represents the ordinal plus one
		final HTTPResponseClass[] values=HTTPResponseClass.values();	//get the available values
		if(ordinal>=0 && ordinal<values.length)	//if our ordinal is in the range of response classes
		{
			return values[ordinal];	//return this response class			
		}
		else	//if there is no corresponding response class
		{
			throw new IllegalArgumentException("Unrecognized status code "+statusCode+".");
		}
	}
}
