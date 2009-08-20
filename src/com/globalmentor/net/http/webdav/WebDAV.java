/*
 * Copyright © 1996-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.globalmentor.net.http.webdav;

import java.net.URI;
import java.util.*;

import static com.globalmentor.net.http.webdav.WebDAVPropertyName.*;
import static com.globalmentor.text.xml.XML.createQualifiedName;

import com.globalmentor.collections.DecoratorIDedMappedList;

import org.w3c.dom.NodeList;

/**Constant values and utilities for WebDAV as defined by
<a href="http://www.ietf.org/rfc/rfc2518.txt">RFC 2518</a>,	"HTTP Extensions for Distributed Authoring -- WEBDAV".
<p>Status code declarations and comments used from Tomcat org.apache.catalina.servlets.WebdavServlet by Remy Maucherat Revision: 1.19 $ $Date: 2004/09/19 01:20:10.</p>  
@author Garret Wilson
*/
public class WebDAV
{

	/**The WebDAV COPY method.*/
	public final static String COPY_METHOD="COPY";
	/**The WebDAV LOCk method.*/
	public final static String LOCK_METHOD="LOCK";
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
	public final static WebDAVPropertyName CREATION_DATE_PROPERTY_NAME=new WebDAVPropertyName(WEBDAV_NAMESPACE, "creationdate");
	public final static WebDAVPropertyName DISPLAY_NAME_PROPERTY_NAME=new WebDAVPropertyName(WEBDAV_NAMESPACE, "displayname");
	public final static WebDAVPropertyName GET_CONTENT_LANGUAGE_PROPERTY_NAME=new WebDAVPropertyName(WEBDAV_NAMESPACE, "getcontentlanguage");
	public final static WebDAVPropertyName GET_CONTENT_LENGTH_PROPERTY_NAME=new WebDAVPropertyName(WEBDAV_NAMESPACE, "getcontentlength");
	public final static WebDAVPropertyName GET_CONTENT_TYPE_PROPERTY_NAME=new WebDAVPropertyName(WEBDAV_NAMESPACE, "getcontenttype");
	public final static WebDAVPropertyName GET_ETAG_PROPERTY_NAME=new WebDAVPropertyName(WEBDAV_NAMESPACE, "getetag");
	public final static WebDAVPropertyName GET_LAST_MODIFIED_PROPERTY_NAME=new WebDAVPropertyName(WEBDAV_NAMESPACE, "getlastmodified");
	public final static WebDAVPropertyName LOCK_DISCOVERY_PROPERTY_NAME=new WebDAVPropertyName(WEBDAV_NAMESPACE, "lockdiscovery");
	public final static WebDAVPropertyName RESOURCE_TYPE_PROPERTY_NAME=new WebDAVPropertyName(WEBDAV_NAMESPACE, "resourcetype");

	/**The constant property list indicating all properties.*/
	public final static DecoratorIDedMappedList<URI, WebDAVPropertyName> ALL_PROPERTIES=new DecoratorIDedMappedList<URI, WebDAVPropertyName>(new HashMap<URI, WebDAVPropertyName>(), new ArrayList<WebDAVPropertyName>());

	/**The constant property list indicating all property names.*/
	public final static DecoratorIDedMappedList<URI, WebDAVPropertyName> PROPERTY_NAMES=new DecoratorIDedMappedList<URI, WebDAVPropertyName>(new HashMap<URI, WebDAVPropertyName>(), new ArrayList<WebDAVPropertyName>());

		//resource type names
	public final static String COLLECTION_TYPE_NAME="collection";

		//resource types
	public final static URI COLLECTION_TYPE=createPropertyURI(WEBDAV_NAMESPACE, COLLECTION_TYPE_NAME);

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
	/**The property find element name.*/
	public final static String ELEMENT_PROPFIND="propfind";
	/**The property status stat element name.*/
	public final static String ELEMENT_PROPSTAT="propstat";
	/**The property update element name.*/
	public final static String ELEMENT_PROPERTYUPDATE="propertyupdate";
	/**The property remove element name.*/
	public final static String ELEMENT_REMOVE="remove";
	/**The response element name.*/
	public final static String ELEMENT_RESPONSE="response";
	/**The property set element name.*/
	public final static String ELEMENT_SET="set";
	/**The status element name.*/
	public final static String ELEMENT_STATUS="status";

  /**
   * Status code (207) indicating that the response requires
   * providing status for multiple independent operations.
   */
  public static final int SC_MULTI_STATUS = 207;
  // This one collides with HTTP 1.1
  // "207 Parital Update OK"


  /**
   * Status code (418) indicating the entity body submitted with
   * the PATCH method was not understood by the resource.
   */
  public static final int SC_UNPROCESSABLE_ENTITY = 418;
  // This one collides with HTTP 1.1
  // "418 Reauthentication Required"


  /**
   * Status code (419) indicating that the resource does not have
   * sufficient space to record the state of the resource after the
   * execution of this method.
   */
  public static final int SC_INSUFFICIENT_SPACE_ON_RESOURCE = 419;
  // This one collides with HTTP 1.1
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


	/**Determines if a resource is a collection based upon given properties
	@param webdavProperties The properties to examine.
	@return <code>true</code> if the given properties indicates that the resource is a collection, else <code>false</code>.
	*/
	public static boolean isCollection(final Map<WebDAVPropertyName, WebDAVProperty> webdavProperties)
	{
		final WebDAVProperty resourceTypeProperty=webdavProperties.get(RESOURCE_TYPE_PROPERTY_NAME);	//try to get the value of D:resourcetype
		if(resourceTypeProperty!=null)	//if there is a resource type indicated
		{
			final WebDAVPropertyValue resourceTypePropertyValue=resourceTypeProperty.getValue();	//get the value of the resource type property
			if(resourceTypePropertyValue instanceof WebDAVDocumentFragmentPropertyValue)	//if the property value represents a document fragment
			{
				final NodeList valueNodes=((WebDAVDocumentFragmentPropertyValue)resourceTypePropertyValue).getDocumentFragment().getChildNodes();	//get the children of the document fragment
				if(valueNodes.getLength()==1 && COLLECTION_TYPE.equals(createQualifiedName(valueNodes.item(0)).getURI()))	//if there is one child with a reference URI of D:collection
				{
					return true;	//indicate that the resource is a collection
				}
			}
		}
		return false;	//if there is no D:resourcetype property or the D:resourcetype property value was not D:collection, the resource is not a collection
	}

}
