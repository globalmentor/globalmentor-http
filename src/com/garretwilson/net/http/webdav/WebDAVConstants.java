package com.garretwilson.net.http.webdav;

/**Constant values for WebDAV as defined by
<a href="http://www.ietf.org/rfc/rfc2518.txt">RFC 2518</a>,	"HTTP Extensions for Distributed Authoring -- WEBDAV". 
@author Garret Wilson
*/
public class WebDAVConstants
{

	public enum WebDAVMethod {COPY, DELETE, GET, HEAD, LOCK, MKCOL, MOVE, OPTIONS, PUT, POST, PROPFIND, PROPPATCH, TRACE, UNLOCK};
	
	/**The header indicating the allowed methods.*/
	public final static String ALLOW_HEADER="allow";	//TODO is this an HTTP method or a WebDAV method
	/**The header indicating the WevDAV versions supported.*/
	public final static String DAV_HEADER="DAV";
	/**The header indicating preferred Microsoft authoring.*/
	public final static String MS_AUTHOR_VIA_HEADER="MS-Author-Via";
		/**The header indicating Microsoft authoring via DAV.*/
		public final static String MS_AUTHOR_VIA_DAV="DAV";

}
