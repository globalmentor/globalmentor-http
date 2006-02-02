package com.garretwilson.net.http;

import java.net.URI;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.net.http.HTTPConstants.*;

/**Indicates that a resource has moved temporarily to a new location. 
Corresponds to HTTP status code 302.
@author Garret Wilson
*/
public class HTTPMovedTemporarilyException extends HTTPRedirectException
{

	/**The location to which a redirect should occur.*/
	private final URI location;

		/**@return The location to which a redirect should occur.*/
		public URI getLocation() {return location;}

	/**Constructs a new exception with the location.
	@param location The location to which a redirect should occur.
	@exception NullPointerException if the location is <code>null</code>.
	*/
	public HTTPMovedTemporarilyException(final URI location)
	{
		super(SC_MOVED_TEMPORARILY);	//construct the parent class
		this.location=checkNull(location, "Location must be provided.");	//save the location
	}

}
