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

package com.globalmentor.net.http.webdav;

import static com.globalmentor.java.Objects.*;

import com.globalmentor.model.NameValuePair;

/**A WebDAV property, representing a WebDAV property name and a value (such as a literal string value or a document fragment value).
@author Garret Wilson
*/
public class WebDAVProperty extends NameValuePair<WebDAVPropertyName, WebDAVPropertyValue>
{

	/**Constructor specifying the name and value.
	@param name The WebDAV property's name.
	@param value The WebDAV property's value, which may be <code>null</code>.
	@exception NullPointerException if the given name is <code>null</code>.
	*/
	public WebDAVProperty(final WebDAVPropertyName name, final WebDAVPropertyValue value)
	{
		super(checkInstance(name, "WebDAV property name cannot be null."), value);	//construct the parent class with the name and value
	}

}
