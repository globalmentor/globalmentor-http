package com.garretwilson.net.http.webdav;

import static com.garretwilson.net.URIUtilities.getRawPathQueryFragment;
import static com.garretwilson.net.URIUtilities.isAbsolutePath;
import static com.garretwilson.net.http.HTTPConstants.*;
import static com.garretwilson.net.http.HTTPFormatter.formatAuthorizationHeader;
import static com.garretwilson.net.http.HTTPParser.parseAuthorizationHeader;
import static com.garretwilson.net.http.webdav.WebDAVConstants.*;

import java.net.URI;

import com.garretwilson.net.Host;
import com.garretwilson.net.http.AbstractHTTPMessage;
import com.garretwilson.net.http.AuthenticateCredentials;
import com.garretwilson.net.http.DefaultHTTPRequest;
import com.garretwilson.net.http.HTTPConstants;
import com.garretwilson.net.http.HTTPRequest;
import com.garretwilson.net.http.HTTPVersion;
import com.garretwilson.util.SyntaxException;

/**The default implementation of a WebDAV request as defined by
<a href="http://www.ietf.org/rfc/rfc2518.txt">RFC 2518</a>,	"HTTP Extensions for Distributed Authoring -- WEBDAV".
@author Garret Wilson
*/
public class DefaultWebDAVRequest extends DefaultHTTPRequest implements WebDAVRequest
{

	/**Constructs a request with a method, address, and the default HTTP version, 1.1.
	@param method The HTTP method.
	@param uri The absolute URI of the request.
	@exception IllegalArgumentException if the given URI does not represent an absolute URI with an absolute path.
	@see #DEFAULT_VERSION
	*/
	public DefaultWebDAVRequest(final String method, final URI uri)
	{
		this(method, uri, DEFAULT_VERSION);	//construct the class with the default version
	}

	/**Constructs a request with a method, address, and version.
	@param method The HTTP method.
	@param uri The absolute URI of the request.
	@param version The HTTP version being used.
	@exception IllegalArgumentException if the given URI does not represent an absolute URI with an absolute path.
	*/
	public DefaultWebDAVRequest(final String method, final URI uri, final HTTPVersion version)
	{
		super(method, uri, version);	//construct the parent class
	}

	/**Gets the requested depth from the header.
  @return The depth or <code>Depth.INFINITY</code> if an infinite, undefined, or or unrecognized depth is indicated.
	@see WebDAVConstants#DEPTH_HEADER
	*/
	public Depth getDepth()
	{
		final String depthString=getHeader(DEPTH_HEADER);	//get the depth header
		if(depthString==null)	//no depth specified
		{
			return Depth.INFINITY;	//default to infinity
		}
		else if(DEPTH_0.equals(depthString))	//0
		{
			return Depth.ZERO;
		}
		else if(DEPTH_1.equals(depthString))	//1
		{
			return Depth.ONE;
		}
		else if(DEPTH_INFINITY.equals(depthString))	//infinity
		{
			return Depth.INFINITY;
		}
		else	//unrecognized depth (technically an error)
		{
			return Depth.INFINITY;	//default to infinity
		}
	}

	/**Sets the depth.
	@param depth The requested depth.
	@see WebDAVConstants#DEPTH_HEADER
	*/
	public void setDepth(final Depth depth)
	{
			//get the string version of the depth ordinal, or "infinity" for the infinite depth
		final String depthString=depth==Depth.INFINITY ? DEPTH_INFINITY : Integer.toString(depth.ordinal());
		setHeader(DEPTH_HEADER, depthString);	//set the depth
	}

}
