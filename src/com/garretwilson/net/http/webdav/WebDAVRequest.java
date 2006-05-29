package com.garretwilson.net.http.webdav;

import static com.garretwilson.net.http.webdav.WebDAVConstants.DESTINATION_HEADER;
import static com.garretwilson.net.http.webdav.WebDAVConstants.OVERWRITE_FALSE;
import static com.garretwilson.net.http.webdav.WebDAVConstants.OVERWRITE_HEADER;
import static com.garretwilson.net.http.webdav.WebDAVConstants.OVERWRITE_TRUE;

import java.net.URI;

import com.garretwilson.net.http.HTTPRequest;
import com.garretwilson.net.http.webdav.WebDAVConstants;

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

	/**Retrieves the destination URI.
	@return The URI indicating the destination of a COPY or MOVE,	or <code>null</code> if the {@value WebDAVConstants#DESTINATION_HEADER} header is not present.
	@exception IllegalArgumentException if the destination header value does not represent a valid URI or the represented URI is not absolute.
	@see WebDAVConstants#DESTINATION_HEADER
	*/
	public URI getDestination();
	
	/**Sets the destination URI.
	The destination header value is ignored if it does not represennt a valid URI or the represented URI is not absolute.
	@param destinationURI The absolute URI indicating the destination of a COPY or MOVE.
	@exception IllegalArgumentException if the given destination URI is not absolute.
	@see WebDAVConstants#DESTINATION_HEADER
	*/
	public void setDestination(final URI destinationURI);

	/**Returns the overwrite status.
	@return <code>true</code> if the WebDAV {@value WebDAVConstants#OVERWRITE_HEADER} header is missing or {@value WebDAVConstants#OVERWRITE_TRUE}, or <code>false</code> if the value is {@value WebDAVConstants#OVERWRITE_FALSE}.
	@exception IllegalArgumentException if the overwrite header is present and is not {@value WebDAVConstants#OVERWRITE_TRUE} or {@value WebDAVConstants#OVERWRITE_FALSE}.
	@see WebDAVConstants#OVERWRITE_HEADER
	@see WebDAVConstants#OVERWRITE_FALSE
	@see WebDAVConstants#OVERWRITE_TRUE
	*/
	public boolean isOverwrite() throws IllegalArgumentException;

	/**Sets the overwrite status.
	@param overwrite <code>true</code> if the the WebDAV {@value WebDAVConstants#OVERWRITE_HEADER} should be set to {@value WebDAVConstants#OVERWRITE_TRUE}, else <code>false</code> if it should be set to {@value WebDAVConstants#OVERWRITE_FALSE}.
	@see WebDAVConstants#OVERWRITE_HEADER
	@see WebDAVConstants#OVERWRITE_FALSE
	@see WebDAVConstants#OVERWRITE_TRUE
	*/
	public void setOverwrite(final boolean overwrite);

}
