package com.garretwilson.net.http;

import static com.garretwilson.net.http.HTTPConstants.*;

/**Indicates that the server encountered an unexpected condition which prevented it
 	from fulfilling the request. 
Corresponds to HTTP status code 500.
@author Garret Wilson
*/
public class HTTPInternalServerErrorException extends HTTPServerErrorException
{

	/**Constructs a new exception with the specified status code.
	@param statusCode The HTTP status code to return in the request.
	*/
	public HTTPInternalServerErrorException()
	{
		this((String)null);	//construct the exception with the status code and no message
	}

	/**Constructs a new exception with the specified status code and detail message.
	@param statusCode The HTTP status code to return in the request.
	@param message The detail message.
	*/
	public HTTPInternalServerErrorException(final String message)
	{
		this(message, null);	//construct the class with no cause
	}

	/**Constructs a new exception with the specified status code and cause, along with a detail message derived from the cause.
	@param statusCode The HTTP status code to return in the request.
	@param cause The cause, or <code>null</code> to indicate the cause is nonexistent or unknown.
	*/
	public HTTPInternalServerErrorException(final Throwable cause)
	{
		this(cause!=null ? cause.toString() : null, cause);	//create an exception with a generated detail message
	}

	/**Constructs a new exception with the specified status code, detail message, and cause.
	@param message The detail message.
	@param cause The cause, or <code>null</code> to indicate the cause is nonexistent or unknown.
	*/
	public HTTPInternalServerErrorException(final String message, final Throwable cause)
	{
		super(SC_INTERNAL_SERVER_ERROR, message, cause);	//construct the parent class
	}
}
