package com.globalmentor.net.http;

import static com.globalmentor.net.http.HTTPConstants.*;

/**Indicates that precondition given in one or more of the request-header fields evaluated to false when it was tested on the server. 
Corresponds to HTTP status code 412.
@author Garret Wilson
*/
public class HTTPPreconditionFailedException extends HTTPClientErrorException
{

	/**Constructs a new exception.*/
	public HTTPPreconditionFailedException()
	{
		this((String)null);	//construct the exception with no message
	}

	/**Constructs a new exception with the specified detail message.
	@param message The detail message.
	*/
	public HTTPPreconditionFailedException(final String message)
	{
		this(message, null);	//construct the class with no cause
	}

	/**Constructs a new exception with the specified cause, along with a detail message derived from the cause.
	@param cause The cause, or <code>null</code> to indicate the cause is nonexistent or unknown.
	*/
	public HTTPPreconditionFailedException(final Throwable cause)
	{
		this(cause!=null ? cause.toString() : null, cause);	//create an exception with a generated detail message
	}

	/**Constructs a new exception with the specified detail message and cause.
	@param message The detail message.
	@param cause The cause, or <code>null</code> to indicate the cause is nonexistent or unknown.
	*/
	public HTTPPreconditionFailedException(final String message, final Throwable cause)
	{
		super(SC_PRECONDITION_FAILED, message, cause);	//construct the parent class
	}
}
