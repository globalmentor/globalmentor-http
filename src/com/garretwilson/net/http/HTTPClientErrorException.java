package com.garretwilson.net.http;

/**Indicates that the client has erred. 
Corresponds to HTTP status codes 4xx.
@author Garret Wilson
*/
public abstract class HTTPClientErrorException extends HTTPException
{

	/**Constructs a new exception with the specified status code.
	@param statusCode The HTTP status code to return in the request.
	@exception IllegalArgumentException if the status code is not a 4xx status code.
	*/
	public HTTPClientErrorException(final int statusCode)
	{
		this(statusCode, (String)null);	//construct the exception with the status code and no message
	}

	/**Constructs a new exception with the specified status code and detail message.
	@param statusCode The HTTP status code to return in the request.
	@param message The detail message.
	@exception IllegalArgumentException if the status code is not a 4xx status code.
	*/
	public HTTPClientErrorException(final int statusCode, final String message)
	{
		this(statusCode, message, null);	//construct the class with no cause
	}

	/**Constructs a new exception with the specified status code and cause, along with a detail message derived from the cause.
	@param statusCode The HTTP status code to return in the request.
	@param cause The cause, or <code>null</code> to indicate the cause is nonexistent or unknown.
	@exception IllegalArgumentException if the status code is not a 4xx status code.
	*/
	public HTTPClientErrorException(final int statusCode, final Throwable cause)
	{
		this(statusCode, cause!=null ? cause.toString() : null, cause);	//create an exception with a generated detail message
	}

	/**Constructs a new exception with the specified status code, detail message, and cause.
	@param message The detail message.
	@param cause The cause, or <code>null</code> to indicate the cause is nonexistent or unknown.
	@exception IllegalArgumentException if the status code is not a 4xx status code.
	*/
	public HTTPClientErrorException(final int statusCode, final String message, final Throwable cause)
	{
		super(statusCode, message, cause);	//construct the parent class
		if(statusCode<400 || statusCode>=500)	//if this is not a client error status code
		{
			throw new IllegalArgumentException("Invalid client error status code "+statusCode);
		}
	}

}
