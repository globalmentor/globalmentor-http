package com.garretwilson.net.http;

import javax.servlet.ServletException;

/**A base class for HTTP-related errors.
@author Garret Wilson
*/
public class HTTPException extends ServletException
{
	/**The HTTP status code to return in the request.*/
	private final int statusCode;

		/**@return The HTTP status code to return in the request.*/
		public int getStatusCode() {return statusCode;}

	/**Constructs a new exception with the specified status code.
	@param statusCode The HTTP status code to return in the request.
	*/
	public HTTPException(final int statusCode)
	{
		this(statusCode, (String)null);	//construct the exception with the status code and no message
	}

	/**Constructs a new exception with the specified status code and detail message.
	@param statusCode The HTTP status code to return in the request.
	@param message The detail message.
	*/
	public HTTPException(final int statusCode, final String message)
	{
		this(statusCode, message, null);	//construct the class with no cause
	}

	/**Constructs a new exception with the specified status code, detail message, and cause.
	@param message The detail message.
	@param cause The cause, or <code>null</code> to indicate the cause is nonexistent or unknown.
	*/
	public HTTPException(final int statusCode, final String message, final Throwable cause)
	{
		super(message, cause);	//construct the parent class
		this.statusCode=statusCode;	//save the status code
	}

	/**Constructs a new exception with the specified status code and cause, along with a detail message derived from the cause.
	@param statusCode The HTTP status code to return in the request.
	@param cause The cause, or <code>null</code> to indicate the cause is nonexistent or unknown.
	*/
	public HTTPException(final int statusCode, final Throwable cause)
	{
		super(cause);	//construct the parent class, which will create a detail message
		this.statusCode=statusCode;	//save the status code
	}

}
