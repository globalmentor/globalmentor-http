package com.globalmentor.net.http;

/**Content coding values for HyperText Transfer Protocol (HTTP) as defined by
<a href="http://www.ietf.org/rfc/rfc2616.txt">RFC 2616</a>,	"Hypertext Transfer Protocol -- HTTP/1.1", 3.5.
@see <a href="http://www.ietf.org/rfc/rfc2616.txt">RFC 2616</a>  
@author garret Garret Wilson
*/
public enum ContentCoding
{
	/**The HTTP COMPRESS transfer coding.*/
	compress,
	/**The HTTP deflate transfer coding.*/
	deflate,
	/**The HTTP gzip transfer coding.*/
	gzip,
	/**The HTTP identity transfer coding.*/
	identity;
}
