package com.garretwilson.net.http.webdav;

import java.net.URI;

/**Constants used by the Apache mod_dav implementation.
@author Garret Wilson
@see <a href="http://www.webdav.org/mod_dav/">mod_dav: a DAV module for Apache</a>
*/
public class ApacheWebDAV
{

	/**The recommended prefix to the Apache WebDAV property namespace.*/
	public final static String APACHE_WEBDAV_PROPERTY_NAMESPACE_PREFIX="apache";

	/**The URI to the Apache WebDAV property namespace.*/
	public final static URI APACHE_WEBDAV_PROPERTY_NAMESPACE_URI=URI.create("http://apache.org/dav/props/");

		//mod_dav property names
	/**Describes the executable status of the resource. The local name of <code>apache:executable</code>.
	This property is defined by mod_dav's default repository, the "filesystem" repository. It corresponds to the "executable" permission flag in most filesystems.
	This property is not defined on collections. Value values are "T" and "F".
	*/
	public final static String EXECUTABLE_PROPERTY_NAME="executable";

}
