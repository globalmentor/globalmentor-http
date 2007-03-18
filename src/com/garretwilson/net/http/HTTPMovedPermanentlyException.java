package com.garretwilson.net.http;

import java.net.URI;

import static com.garretwilson.net.http.HTTPConstants.*;

/**Indicates that a resource has moved permanently to a new location. 
Corresponds to HTTP status code 301.
@author Garret Wilson
*/
public class HTTPMovedPermanentlyException extends HTTPRedirectException
{

	/**The location to which a redirect should occur, or <code>null</code> if the redirect location is not given.*/
	private final URI location;

		/**@return The location to which a redirect should occur, or <code>null</code> if the redirect location is not given.*/
		public URI getLocation() {return location;}

	/**Constructs a new exception with the location.
	This constructor accepts a <code>null</code> location to indicate that no redirect location is known,
	because RFC 2616 only says that the location "should" be present, but such construction is discouraged.
	@param location The location to which a redirect should occur, or <code>null</code> if the redirect location is not known.
	*/
	public HTTPMovedPermanentlyException(final URI location)
	{
		super(SC_MOVED_PERMANENTLY);	//construct the parent class
		this.location=location;	//save the location
	}

}
