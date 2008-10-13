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
	public final static WebDAVPropertyName EXECUTABLE_PROPERTY_NAME=new WebDAVPropertyName(APACHE_WEBDAV_PROPERTY_NAMESPACE_URI, "executable");

}
