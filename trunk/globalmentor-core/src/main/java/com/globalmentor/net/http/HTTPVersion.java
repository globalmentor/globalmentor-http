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

/**An encapsulation of the HTTP versioning scheme.
@author Garret Wilson
*/
public class HTTPVersion
{

	/**The major version number.*/
	private final int major;

		/**@return The major version number.*/
		public int getMajor() {return major;}

	/**The minor version number.*/
	private final int minor;

		/**@return The minor version number.*/
		public int getMinor() {return minor;}

	/**Constructor for a major.minor version.
	@param newMajor The major version.
	@param newMinor The minor version.
	*/
	public HTTPVersion(final int newMajor, final int newMinor)
	{
		major=newMajor;
		minor=newMinor;
	}

	/**@return The version as a string in a form indicating the protocol and the version, such as <code><var>major</var>.<var>minor</var></code>.*/
	public String toString()
	{
		return new StringBuilder().append(VERSION_IDENTIFIER).append(VERSION_SEPARATOR).append(getMajor()).append(VERSION_DELIMITER).append(getMinor()).toString();	//HTTP/major.minor
	}

}