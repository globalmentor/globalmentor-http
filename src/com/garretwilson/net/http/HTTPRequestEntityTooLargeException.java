package com.garretwilson.net.http;

import static com.garretwilson.net.http.HTTPConstants.*;

/**Indicates that the server is refusing to process a request because the request
 	entity is larger than the server is willing or able to process. 
Corresponds to HTTP status code 413.
@author Garret Wilson
*/
public class HTTPRequestEntityTooLargeException extends HTTPClientErrorException
{

	/**Constructs a new exception.*/
	public HTTPRequestEntityTooLargeException()
	{
		this((String)null);	//construct the exception with no message
	}

	/**Constructs a new exception with the specified detail message.
	@param message The detail message.
	*/
	public HTTPRequestEntityTooLargeException(final String message)
	{
		this(message, null);	//construct the class with no cause
	}

	/**Constructs a new exception with the specified cause, along with a detail message derived from the cause.
	@param cause The cause, or <code>null</code> to indicate the cause is nonexistent or unknown.
	*/
	public HTTPRequestEntityTooLargeException(final Throwable cause)
	{
		this(cause!=null ? cause.toString() : null, cause);	//create an exception with a generated detail message
	}

	/**Constructs a new exception with the specified detail message and cause.
	@param message The detail message.
	@param cause The cause, or <code>null</code> to indicate the cause is nonexistent or unknown.
	*/
	public HTTPRequestEntityTooLargeException(final String message, final Throwable cause)
	{
		super(SC_REQUEST_ENTITY_TOO_LARGE, message, cause);	//construct the parent class
	}
}
