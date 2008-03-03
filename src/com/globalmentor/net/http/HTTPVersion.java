package com.globalmentor.net.http;

import static com.globalmentor.net.http.HTTPConstants.*;

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