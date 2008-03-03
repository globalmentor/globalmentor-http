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

import com.globalmentor.util.ObjectDecorator;

/**A literal WebDAV value of a resource property.
The literal value of the resource can be returned using {@link #toString()} or {@link #getText()}.
@author Garret Wilson
*/
public class WebDAVLiteralPropertyValue extends ObjectDecorator<String> implements WebDAVPropertyValue
{

	/**String literal constructor.
	@param literal The literal string this value represents.
	@exception NullPointerException if the given literal is <code>null</code>.
	*/
	public WebDAVLiteralPropertyValue(final String literal)
	{
		super(literal);	//construct the parent class
	}

	/**@return A non-<code>null</code> literal representing plain text contained in this WebDAV value, which may be the empty string.*/
	public String getText()
	{
		return getObject();	//return the string literal itself
	}
}
