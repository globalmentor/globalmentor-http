package com.garretwilson.net.http;

import java.net.URI;

import static com.garretwilson.net.http.HTTPConstants.*;
import com.garretwilson.util.NameValuePair;

/**Class that knows how to format HTTP information.
@author Garret Wilson
*/
public class HTTPFormatter
{

	/**Formats the request line of an HTTP request.
	@param method The HTTP method.
	@param requestURI The absolute URI or absolute path of the request, or '*'.
	@param httpVersion The HTTP version.
	@return The string builder containing the formatted information.
	*/
	public static StringBuilder formatRequestLine(final StringBuilder stringBuilder, final String method, final String requestURI, final HTTPVersion httpVersion)
	{
		stringBuilder.append(method).append(SP).append(requestURI.toString()).append(SP);	//Method SP Request-URI SP
		formatVersion(stringBuilder, httpVersion);	//HTTP-Version
		return stringBuilder.append(CRLF);	//CRLF
	}

	/**Formats an HTTP version in the form <code>HTTP/<var>major</var>.<var>minor</var></code>.
	@param version The HTTP version.
	@return The string builder containing the formatted information.
	*/
	public static StringBuilder formatVersion(final StringBuilder stringBuilder, final HTTPVersion version)
	{
		return stringBuilder.append(VERSION_IDENTIFIER).append(VERSION_SEPARATOR).append(version.getMajor()).append(VERSION_DELIMITER).append(version.getMinor());	//HTTP/major.minor
	}

	/**Formats an HTTP header line in the form <code><var>name</var>: <var>value</var>CRLF</code>.
	@param header The HTTP header.
	@return The string builder containing the formatted information.
	*/
	public static StringBuilder formatHeaderLine(final StringBuilder stringBuilder, final NameValuePair<String, String> header)
	{
		return stringBuilder.append(header.getName()).append(HEADER_SEPARATOR).append(SP).append(header.getValue()).append(CRLF);	//name: SP value
	}
}
