package com.globalmentor.net.http;

import static com.globalmentor.net.http.HTTPConstants.*;

/**Indicates that the server has not found anything matching the Request-URI. 
Corresponds to HTTP status code 404.
@author Garret Wilson
*/
public class HTTPNotFoundException extends HTTPClientErrorException
{

	/**Constructs a new exception.*/
	public HTTPNotFoundException()
	{
		this((String)null);	//construct the exception with no message
	}

	/**Constructs a new exception with the specified detail message.
	@param message The detail message.
	*/
	public HTTPNotFoundException(final String message)
	{
		this(message, null);	//construct the class with no cause
	}

	/**Constructs a new exception with the specified cause, along with a detail message derived from the cause.
	@param cause The cause, or <code>null</code> to indicate the cause is nonexistent or unknown.
	*/
	public HTTPNotFoundException(final Throwable cause)
	{
		this(cause!=null ? cause.toString() : null, cause);	//create an exception with a generated detail message
	}

	/**Constructs a new exception with the specified detail message and cause.
	@param message The detail message.
	@param cause The cause, or <code>null</code> to indicate the cause is nonexistent or unknown.
	*/
	public HTTPNotFoundException(final String message, final Throwable cause)
	{
		super(SC_NOT_FOUND, message, cause);	//construct the parent class
	}
}
