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
		super(statusCode);	//construct parent class
		if(statusCode<400 || statusCode>=500)	//if this is not a client error status code
		{
			throw new IllegalArgumentException("Invalid client error status code "+statusCode);
		}
	}

}
