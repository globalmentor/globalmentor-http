package com.globalmentor.net.http;

import java.io.IOException;

import com.globalmentor.net.*;

/**A base class for HTTP-related errors.
@author Garret Wilson
*/
public class HTTPException extends IOException
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

	/**Constructs a new exception with the specified status code and cause, along with a detail message derived from the cause.
	@param statusCode The HTTP status code to return in the request.
	@param cause The cause, or <code>null</code> to indicate the cause is nonexistent or unknown.
	*/
	public HTTPException(final int statusCode, final Throwable cause)
	{
		this(statusCode, cause!=null ? cause.toString() : null, cause);	//create an exception with a generated detail message
	}

	/**Constructs a new exception with the specified status code, detail message, and cause.
	@param message The detail message.
	@param cause The cause, or <code>null</code> to indicate the cause is nonexistent or unknown.
	*/
	public HTTPException(final int statusCode, final String message, final Throwable cause)
	{
		super(message);	//construct the parent class
		initCause(cause);	//indicate the source of this exception
		this.statusCode=statusCode;	//save the status code
	}

	/**Translates the given resource I/O exception to an HTTP-specific exception.
	The following special translations are performed:
	<dl>
		<dt>{@link ResourceForbiddenException}</dt> <dd>{@link HTTPForbiddenException}</dd>
		<dt>{@link ResourceNotFoundException}</dt> <dd>{@link HTTPNotFoundException}</dd>
		<dt>{@link ResourceMovedTemporarilyException}</dt> <dd>{@link HTTPMovedTemporarilyException}</dd>
		<dt>{@link ResourceMovedTemporarilyException}</dt> <dd>{@link HTTPMovedTemporarilyException}</dd>
		<dt>{@link ResourceStateException}</dt> <dd>{@link HTTPPreconditionFailedException}</dd>
	</dl>
	All other exceptions are sent back as {@link HTTPInternalServerErrorException}s.
	@param resourceIOException The I/O exception related to a particular resource.
	@return An HTTP exception analagous to the given resource I/O exception.
	*/
	public static HTTPException createHTTPException(final ResourceIOException resourceIOException)
	{
		if(resourceIOException instanceof ResourceForbiddenException)
		{
			return new HTTPForbiddenException(resourceIOException);
		}
		else if(resourceIOException instanceof ResourceNotFoundException)
		{
			return new HTTPNotFoundException(resourceIOException);
		}
		else if(resourceIOException instanceof ResourceMovedTemporarilyException)
		{
			return new HTTPMovedTemporarilyException(((ResourceMovedTemporarilyException)resourceIOException).getNewResourceURI());
		}
		else if(resourceIOException instanceof ResourceMovedPermanentlyException)
		{
			return new HTTPMovedPermanentlyException(((ResourceMovedPermanentlyException)resourceIOException).getNewResourceURI());
		}
		else if(resourceIOException instanceof ResourceStateException)
		{
			return new HTTPPreconditionFailedException(resourceIOException);
		}
		else	//if this is not one of our specially-handled exceptions
		{
			return new HTTPInternalServerErrorException(resourceIOException);	//return an internal server error
		}
	}

}
