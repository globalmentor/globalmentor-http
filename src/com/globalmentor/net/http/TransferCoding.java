package com.globalmentor.net.http;

/**Transfer coding values for HyperText Transfer Protocol (HTTP) as defined by
<a href="http://www.ietf.org/rfc/rfc2616.txt">RFC 2616</a>,	"Hypertext Transfer Protocol -- HTTP/1.1", 3.6.
@see <a href="http://www.ietf.org/rfc/rfc2616.txt">RFC 2616</a>  
@author garret Garret Wilson
*/
public enum TransferCoding
{
	/**The HTTP chunked transfer coding.*/
	chunked,
	/**The HTTP COMPRESS transfer coding.*/
	compress,
	/**The HTTP deflate transfer coding.*/
	deflate,
	/**The HTTP gzip transfer coding.*/
	gzip,
	/**The HTTP identity transfer coding.*/
	identity;
}
