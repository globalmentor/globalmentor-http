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

import static com.globalmentor.net.http.HTTP.*;

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
