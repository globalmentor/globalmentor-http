/*
 * Copyright © 1996-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
