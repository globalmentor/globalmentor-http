package com.garretwilson.net.http;

import java.net.URI;
import static com.garretwilson.net.http.HTTPConstants.*;

/**Indicates that a resource has moved permanently to a new location. 
Corresponds to HTTP status code 301.
@author Garret Wilson
*/
public class HTTPMovedPermanentlyException extends HTTPRedirectException
{

	/**Constructs a new exception with the location.
	@param location The location to which a redirect should occur.
	@exception NullPointerException if the location is <code>null</code>.
	*/
	public HTTPMovedPermanentlyException(final URI location)
	{
		super(SC_MOVED_PERMANENTLY, location);	//construct parent class
	}

}
