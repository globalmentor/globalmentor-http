package com.garretwilson.net.http.webdav;

import java.net.URI;

import static com.garretwilson.text.xml.QualifiedName.*;

/**Constant values for WebDAV as defined by
<a href="http://www.ietf.org/rfc/rfc2518.txt">RFC 2518</a>,	"HTTP Extensions for Distributed Authoring -- WEBDAV".
<p>Status code declarations and comments used from Tomcat org.apache.catalina.servlets.WebdavServlet by Remy Maucherat Revision: 1.19 $ $Date: 2004/09/19 01:20:10.</p>  
@author Garret Wilson
*/
public class WebDAVConstants
{

	/**The WebDAV COPY method.*/
	public final static String COPY_METHOD="COPY";
	/**The WebDAV LOCk method.*/
	public final static String LOC_METHOD="LOCK";
	/**The WebDAV MKCOL method.*/
	public final static String MKCOL_METHOD="MKCOL";
	/**The WebDAV MOVE method.*/
	public final static String MOVE_METHOD="MOVE";
	/**The WebDAV PROPFIND method.*/
	public final static String PROPFIND_METHOD="PROPFIND";
	/**The WebDAV PROPPATCH method.*/
	public final static String PROPPATCH_METHOD="PROPPATCH";
	/**The WebDAV UNLOCK method.*/
	public final static String UNLOCK_METHOD="UNLOCK";

	/**The recommended prefix to the WebDAV namespace.*/
	public final static String WEBDAV_NAMESPACE_PREFIX="D";
	/**The WebDAV namespace identifier, which is not a true URI.*/
	public final static String WEBDAV_NAMESPACE="DAV:";

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
	/**The header indicating the destination of COPY or MOVE.*/
	public final static String DESTINATION_HEADER="Destination";
	/**The header indicating preferred Microsoft authoring.*/
	public final static String MS_AUTHOR_VIA_HEADER="MS-Author-Via";
		/**The header indicating Microsoft authoring via DAV.*/
		public final static String MS_AUTHOR_VIA_DAV="DAV";
	/**The header indicating whether an existing resource should be overwritten.*/
	public final static String OVERWRITE_HEADER="Overwrite";
		/**The overwrite header value indicating false.*/
		public final static String OVERWRITE_FALSE="F";
		/**The overwrite header value indicating true.*/
		public final static String OVERWRITE_TRUE="T";

		//property names
	public final static String CREATION_DATE_PROPERTY_NAME="creationdate";
	public final static String DISPLAY_NAME_PROPERTY_NAME="displayname";
	public final static String GET_CONTENT_LANGUAGE_PROPERTY_NAME="getcontentlanguage";
	public final static String GET_CONTENT_LENGTH_PROPERTY_NAME="getcontentlength";
	public final static String GET_CONTENT_TYPE_PROPERTY_NAME="getcontenttype";
	public final static String GET_ETAG_PROPERTY_NAME="getetag";
	public final static String GET_LAST_MODIFIED_PROPERTY_NAME="getlastmodified";
	public final static String LOCK_DISCOVERY_PROPERTY_NAME="lockdiscovery";
	public final static String RESOURCE_TYPE_PROPERTY_NAME="resourcetype";

		//properties
	public final static URI CREATION_DATE_PROPERTY=createReferenceURI(WEBDAV_NAMESPACE, CREATION_DATE_PROPERTY_NAME);
	public final static URI DISPLAY_NAME_PROPERTY=createReferenceURI(WEBDAV_NAMESPACE, DISPLAY_NAME_PROPERTY_NAME);
	public final static URI GET_CONTENT_LANGUAGE_PROPERTY=createReferenceURI(WEBDAV_NAMESPACE, GET_CONTENT_LANGUAGE_PROPERTY_NAME);
	public final static URI GET_CONTENT_LENGTH_PROPERTY=createReferenceURI(WEBDAV_NAMESPACE, GET_CONTENT_LENGTH_PROPERTY_NAME);
	public final static URI GET_CONTENT_TYPE_PROPERTY=createReferenceURI(WEBDAV_NAMESPACE, GET_CONTENT_TYPE_PROPERTY_NAME);
	public final static URI GET_ETAG_PROPERTY=createReferenceURI(WEBDAV_NAMESPACE, GET_ETAG_PROPERTY_NAME);
	public final static URI GET_LAST_MODIFIED_PROPERTY=createReferenceURI(WEBDAV_NAMESPACE, GET_LAST_MODIFIED_PROPERTY_NAME);
	public final static URI LOCK_DISCOVERY_PROPERTY=createReferenceURI(WEBDAV_NAMESPACE, LOCK_DISCOVERY_PROPERTY_NAME);
	public final static URI RESOURCE_TYPE_PROPERTY=createReferenceURI(WEBDAV_NAMESPACE, RESOURCE_TYPE_PROPERTY_NAME);

		//resource type names
	public final static String COLLECTION_TYPE_NAME="collection";

		//resource types
	public final static URI COLLECTION_TYPE=createReferenceURI(WEBDAV_NAMESPACE, COLLECTION_TYPE_NAME);

		//XML names
	/**The all properties element name.*/
	public final static String ELEMENT_ALLPROP="allprop";
	/**The href element name.*/
	public final static String ELEMENT_HREF="href";
	/**The multiple status container element name.*/
	public final static String ELEMENT_MULTISTATUS="multistatus";
	/**The property element name.*/
	public final static String ELEMENT_PROP="prop";
	/**The property name element name.*/
	public final static String ELEMENT_PROPNAME="propname";
	/**The propfind element name.*/
	public final static String ELEMENT_PROPFIND="propfind";
	/**The propstat element name.*/
	public final static String ELEMENT_PROPSTAT="propstat";
	/**The response element name.*/
	public final static String ELEMENT_RESPONSE="response";
	/**The status element name.*/
	public final static String ELEMENT_STATUS="status";

  /**
   * Status code (207) indicating that the response requires
   * providing status for multiple independent operations.
   */
  public static final int SC_MULTI_STATUS = 207;
  // This one colides with HTTP 1.1
  // "207 Parital Update OK"


  /**
   * Status code (418) indicating the entity body submitted with
   * the PATCH method was not understood by the resource.
   */
  public static final int SC_UNPROCESSABLE_ENTITY = 418;
  // This one colides with HTTP 1.1
  // "418 Reauthentication Required"


  /**
   * Status code (419) indicating that the resource does not have
   * sufficient space to record the state of the resource after the
   * execution of this method.
   */
  public static final int SC_INSUFFICIENT_SPACE_ON_RESOURCE = 419;
  // This one colides with HTTP 1.1
  // "419 Proxy Reauthentication Required"


  /**
   * Status code (420) indicating the method was not executed on
   * a particular resource within its scope because some part of
   * the method's execution failed causing the entire method to be
   * aborted.
   */
  public static final int SC_METHOD_FAILURE = 420;


  /**
   * Status code (423) indicating the destination resource of a
   * method is locked, and either the request did not contain a
   * valid Lock-Info header, or the Lock-Info header identifies
   * a lock held by another principal.
   */
  public static final int SC_LOCKED = 423;

}
