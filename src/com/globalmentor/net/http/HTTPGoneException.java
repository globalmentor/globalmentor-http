package com.globalmentor.net.http;

import static com.globalmentor.net.http.HTTPConstants.*;

/**Indicates that the requested resource is no longer available at the server and no
 	forwarding address is known. 
Corresponds to HTTP status code 410.
@author Garret Wilson
*/
public class HTTPGoneException extends HTTPClientErrorException
{

	/**Constructs a new exception.*/
	public HTTPGoneException()
	{
		this((String)null);	//construct the exception with no message
	}

	/**Constructs a new exception with the specified detail message.
	@param message The detail message.
	*/
	public HTTPGoneException(final String message)
	{
		this(message, null);	//construct the class with no cause
	}

	/**Constructs a new exception with the specified cause, along with a detail message derived from the cause.
	@param cause The cause, or <code>null</code> to indicate the cause is nonexistent or unknown.
	*/
	public HTTPGoneException(final Throwable cause)
	{
		this(cause!=null ? cause.toString() : null, cause);	//create an exception with a generated detail message
	}

	/**Constructs a new exception with the specified detail message, and cause.
	@param message The detail message.
	@param cause The cause, or <code>null</code> to indicate the cause is nonexistent or unknown.
	*/
	public HTTPGoneException(final String message, final Throwable cause)
	{
		super(SC_GONE, message, cause);	//construct the parent class
	}
}
