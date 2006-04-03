package com.garretwilson.net.http;

import java.net.URI;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.net.http.HTTPConstants.*;

/**Indicates that a resource has moved permanently to a new location. 
Corresponds to HTTP status code 301.
@author Garret Wilson
*/
public class HTTPMovedPermanentlyException extends HTTPRedirectException
{

	/**The location to which a redirect should occur.*/
	private final URI location;

		/**@return The location to which a redirect should occur.*/
		public URI getLocation() {return location;}

	/**Constructs a new exception with the location.
	@param location The location to which a redirect should occur.
	@exception NullPointerException if the location is <code>null</code>.
	*/
	public HTTPMovedPermanentlyException(final URI location)
	{
		super(SC_MOVED_PERMANENTLY);	//construct the parent class
		this.location=checkInstance(location, "Location must be provided.");	//save the location
	}

}
