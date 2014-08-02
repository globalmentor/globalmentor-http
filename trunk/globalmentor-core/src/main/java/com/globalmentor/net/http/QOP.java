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

import static com.globalmentor.net.http.DigestAuthentication.*;

/**Represents quality of protection.
@author Garret Wilson
*/
public enum QOP
{

	/**Authentication quality of protection.*/	
	AUTH(AUTH_QOP),

	/**Authentication with integerity protection quality of protection.*/	
	AUTH_INT(AUTH_INT_QOP);

	/**The string value of the enum.*/
	private final String string;
	
	/**String constructor.
	@param string The string value of the enum.
	*/
	private QOP(final String string) {this.string=string;}
	
	/**@return The string value of the enum.*/
	public String toString() {return string;}

	/**Determines the enumeration value matching the given string.
	@param string The string value.
	@return The enumeration value corresponding to the string.
	@throws IllegalArgumentException if the enum has no constant with the specified name.
	@throws NullPointerException if the string is <code>null</code>.
	@see #toString()
	*/
	public static QOP valueOfString(final String string)	//TODO improve by storing values in a map
	{
		for(final QOP value:QOP.values())	//look at each value
		{
			if(string.equals(value.toString()))	//if this value has the right string
			{
				return value;	//return this value
			}
		}
		throw new IllegalArgumentException(string);	//show that we didn't recognize the string
	}
}
