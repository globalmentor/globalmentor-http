package com.garretwilson.net.http.webdav;

import com.garretwilson.net.http.HTTPRequest;

/**A WebDAV request as defined by
<a href="http://www.ietf.org/rfc/rfc2518.txt">RFC 2518</a>,	"HTTP Extensions for Distributed Authoring -- WEBDAV".
@author Garret Wilson
*/
public interface WebDAVRequest extends HTTPRequest
{

	/**Gets the requested depth from the header.
  @return The depth or <code>Depth.INFINITY</code> if an infinite, undefined, or or unrecognized depth is indicated.
	@see WebDAVConstants#DEPTH_HEADER
	*/
	public Depth getDepth();

	/**Sets the depth.
	@param depth The requested depth.
	@see WebDAVConstants#DEPTH_HEADER
	*/
	public void setDepth(final Depth depth);

}
