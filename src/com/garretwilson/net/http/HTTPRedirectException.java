package com.garretwilson.net.http;

import java.net.URI;

/**Indicates that further action must be taken in order to complete the request. 
Corresponds to HTTP status codes 3xx.
@author Garret Wilson
*/
public abstract class HTTPRedirectException extends HTTPException
{

	/**The location to which a redirect should occur.*/
	private final URI location;

		/**@return The location to which a redirect should occur.*/
		public URI getLocation() {return location;}

	/**Constructs a new exception with the specified status code and location.
	@param statusCode The HTTP status code to return in the request.
	@param location The location to which a redirect should occur.
	@exception IllegalArgumentException if the status code is not a 3xx status code.
	@exception NullPointerException if the location is <code>null</code>.
	*/
	public HTTPRedirectException(final int statusCode, final URI location)
	{
		super(statusCode);	//construct parent class
		if(statusCode<300 || statusCode>=400)	//if this is not a redirect status code
		{
			throw new IllegalArgumentException("Invalid redirect status code "+statusCode);
		}
		if(location==null)	//if the location is null
		{
			throw new NullPointerException("Location must be provided.");
		}
		this.location=location;	//save the location
	}

}
