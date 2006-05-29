package com.garretwilson.net.http.webdav;

import static com.garretwilson.net.http.webdav.WebDAVConstants.*;

import java.net.URI;

import com.garretwilson.net.http.DefaultHTTPRequest;
import com.garretwilson.net.http.HTTPVersion;

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

	/**Retrieves the destination URI.
	@return The URI indicating the destination of a COPY or MOVE,	or <code>null</code> if the {@value WebDAVConstants#DESTINATION_HEADER} header is not present.
	@exception IllegalArgumentException if the destination header value does not represent a valid URI or the represented URI is not absolute.
	@see WebDAVConstants#DESTINATION_HEADER
	*/
	public URI getDestination()
	{
		final String destinationHeader=getHeader(DESTINATION_HEADER); //get the destination header value
		if(destinationHeader!=null)	//if there is a destination header value
		{
			final URI destinationURI=URI.create(destinationHeader);	//create a URI from the given value
			if(!destinationURI.isAbsolute())	//if the URI is not absolute
			{
				throw new IllegalArgumentException(DESTINATION_HEADER+" header value "+destinationHeader+" is not absolute.");
			}
			return destinationURI;	//return the URI we created from the value
		}
		else	//if there is no destination header
		{
			return null;	//there is no destination URI
		}
	}	
	
	/**Sets the destination URI.
	The destination header value is ignored if it does not represennt a valid URI or the represented URI is not absolute.
	@param destinationURI The absolute URI indicating the destination of a COPY or MOVE.
	@exception IllegalArgumentException if the given destination URI is not absolute.
	@see WebDAVConstants#DESTINATION_HEADER
	*/
	public void setDestination(final URI destinationURI)
	{
		if(destinationURI.isAbsolute())	//if the URI is absolute
		{
			setHeader(DESTINATION_HEADER, destinationURI.toString());	//set the destination URI
		}
		else	//if the destination URI is not absolute
		{
			throw new IllegalArgumentException("Destination URI "+destinationURI+" is not absolute.");
		}
	}	

	/**Returns the overwrite status.
	@return <code>true</code> if the WebDAV {@value WebDAVConstants#OVERWRITE_HEADER} header is missing or {@value WebDAVConstants#OVERWRITE_TRUE}, or <code>false</code> if the value is {@value WebDAVConstants#OVERWRITE_FALSE}.
	@exception IllegalArgumentException if the overwrite header is present and is not {@value WebDAVConstants#OVERWRITE_TRUE} or {@value WebDAVConstants#OVERWRITE_FALSE}.
	@see WebDAVConstants#OVERWRITE_HEADER
	@see WebDAVConstants#OVERWRITE_FALSE
	@see WebDAVConstants#OVERWRITE_TRUE
	*/
	public boolean isOverwrite() throws IllegalArgumentException
	{
		final String overwriteHeader=getHeader(OVERWRITE_HEADER); //get the overwrite header value
		if(overwriteHeader!=null)	//if the overwrite header was given
		{
			if(overwriteHeader.equals(OVERWRITE_FALSE))	//if the value is "F"
			{
				return false;	//return false
			}
			else if(!overwriteHeader.equals(OVERWRITE_TRUE))	//if the value is present, but not "T" or "F"
			{
				throw new IllegalArgumentException("Illegal header "+OVERWRITE_HEADER+" value "+overwriteHeader+".");
			}
		}
		return true;	//default to allowing overwrite
	}

	/**Sets the overwrite status.
	@param overwrite <code>true</code> if the the WebDAV {@value WebDAVConstants#OVERWRITE_HEADER} should be set to {@value WebDAVConstants#OVERWRITE_TRUE}, else <code>false</code> if it should be set to {@value WebDAVConstants#OVERWRITE_FALSE}.
	@see WebDAVConstants#OVERWRITE_HEADER
	@see WebDAVConstants#OVERWRITE_FALSE
	@see WebDAVConstants#OVERWRITE_TRUE
	*/
	public void setOverwrite(final boolean overwrite)
	{
		setHeader(OVERWRITE_HEADER, overwrite ? OVERWRITE_TRUE : OVERWRITE_FALSE);	//set the overwrite header with the correct value
	}

}
