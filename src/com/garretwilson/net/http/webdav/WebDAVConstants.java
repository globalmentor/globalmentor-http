package com.garretwilson.net.http.webdav;

import java.net.URI;

/**Constant values for WebDAV as defined by
<a href="http://www.ietf.org/rfc/rfc2518.txt">RFC 2518</a>,	"HTTP Extensions for Distributed Authoring -- WEBDAV". 
@author Garret Wilson
*/
public class WebDAVConstants
{

	public enum WebDAVMethod {COPY, DELETE, GET, HEAD, LOCK, MKCOL, MOVE, OPTIONS, PUT, POST, PROPFIND, PROPPATCH, TRACE, UNLOCK};

	
	/**The recommended prefix to the WebDAV namespace.*/
	public final static String WEBDAV_NAMESPACE_PREFIX="D";
	/**The URI to the WebDAV namespace.*/
	public final static URI WEBDAV_NAMESPACE_URI=URI.create("DAV:");

	/**The header indicating the allowed methods.*/
	public final static String ALLOW_HEADER="allow";	//TODO is this an HTTP method or a WebDAV method
	/**The header indicating the WevDAV versions supported.*/
	public final static String DAV_HEADER="DAV";
	/**The header indicating the depth of property discovery.*/
	public final static String DEPTH_HEADER="Depth";
		/**The depth header value indicating zero depth.*/
		public final static String DEPTH_0="0";
		/**The depth header value indicating single depth.*/
		public final static String DEPTH_1="1";
		/**The depth header value indicating an infinite depth.*/
		public final static String DEPTH_INFINITY="infinity";
	/**The header indicating preferred Microsoft authoring.*/
	public final static String MS_AUTHOR_VIA_HEADER="MS-Author-Via";
		/**The header indicating Microsoft authoring via DAV.*/
		public final static String MS_AUTHOR_VIA_DAV="DAV";

		//XML names
	/**The all properties element name.*/
	public final static String ELEMENT_ALLPROP="allprop";
	/**The property element name.*/
	public final static String ELEMENT_PROP="prop";
	/**The property name element name.*/
	public final static String ELEMENT_PROPNAME="propname";
	/**The propfind element name.*/
	public final static String ELEMENT_PROPFIND="propfind";
}
