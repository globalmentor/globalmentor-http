package com.globalmentor.net.http.webdav;

/**The depth of a WebDAV request.
The ordinal of each depth except for <code>INFINITY</code> will reflect the value of the depth.
@author Garret Wilson
*/
public enum Depth
{
	/**Zero depth.*/
	ZERO,
	/**Single depth.*/
	ONE,
	/**Infinite depth.*/
	INFINITY;
}
