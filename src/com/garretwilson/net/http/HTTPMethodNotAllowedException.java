package com.garretwilson.net.http;

import java.util.*;
import static com.garretwilson.net.http.HTTPConstants.*;
import static com.garretwilson.util.CollectionUtilities.*;

/**Indicates that method specified in the HTTP request line is not allowed for the
   resource identified by the request URI. 
Corresponds to HTTP status code 405.
@author Garret Wilson
*/
public class HTTPMethodNotAllowedException extends HTTPClientErrorException
{

	/**The set of allowed methods, either strings or objects the strings of which represent HTTP methods.*/
	private final Set<String> allowedMethods;

		/**@return The set of allowed methods, either strings or objects the strings of which represent HTTP methods.*/
		public Set<String> getAllowedMethods() {return allowedMethods;}

	/**Constructs a new exception with the allowed methods.
	@param allowed A set of allowed HTTP methods represented by enum values.
	*/
	public HTTPMethodNotAllowedException(final EnumSet<? extends Enum<?>> allowed)
	{
		super(SC_METHOD_NOT_ALLOWED);	//construct the parent class
		allowedMethods=new HashSet<String>(allowed.size());	//create a string set
		addAll(allowedMethods, allowed);	//add string versions of the enums to our allowed method set
	}

	/**Constructs a new exception with the allowed methods.
	@param allowed A set of allowed HTTP methods.
	*/
	public HTTPMethodNotAllowedException(final Set<String> allowed)
	{
		super(SC_METHOD_NOT_ALLOWED);	//construct the parent class
		this.allowedMethods=allowed;
	}
}
