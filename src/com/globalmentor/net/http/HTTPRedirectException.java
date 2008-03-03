package com.globalmentor.net.http;

/**Indicates that further action must be taken in order to complete the request. 
Corresponds to HTTP status codes 3xx.
@author Garret Wilson
*/
public abstract class HTTPRedirectException extends HTTPException
{

	/**Constructs a new exception with the specified status code and location.
	@param statusCode The HTTP status code to return in the request.
	@exception IllegalArgumentException if the status code is not a 3xx status code.
	*/
	public HTTPRedirectException(final int statusCode)
	{
		super(statusCode);	//construct parent class
		if(statusCode<300 || statusCode>=400)	//if this is not a redirect status code
		{
			throw new IllegalArgumentException("Invalid redirect status code "+statusCode);
		}
	}

}
