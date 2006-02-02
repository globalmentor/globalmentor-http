package com.garretwilson.net.http;

import static com.garretwilson.net.http.HTTPConstants.*;

/**Indicates that a resource for which a condition <code>GET</code> has been requested using an <code>If-Modified-Since</code> date has not been modified after the indicated date. 
Corresponds to HTTP status code 304.
@author Garret Wilson
*/
public class HTTPNotModifiedException extends HTTPRedirectException
{

	/**Default constructor.*/
	public HTTPNotModifiedException()
	{
		super(SC_NOT_MODIFIED);	//construct parent class
	}

}
