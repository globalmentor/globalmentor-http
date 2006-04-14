package com.garretwilson.net.http;

/**Constants for HTTP Digest Access Authentication,
<a href="http://www.ietf.org/rfc/rfc2617.txt">RFC 2617</a>,
	"HTTP Authentication: Basic and Digest Access Authentication", which obsoletes
<a href="http://www.ietf.org/rfc/rfc2069.txt">RFC 2069</a>,
	"An Extension to HTTP : Digest Access Authentication".
@author Garret Wilson
*/
public class BasicAuthenticationConstants
{
	/**The delimiter used when concatenating multiple strings before Base64-encoding.*/
	public final static char BASIC_DELIMITER=':';
}
