package com.globalmentor.net.http.webdav;

import java.net.URI;

/**Constants used by the Subversion WebDAV
@author Garret Wilson
*/
public class SubversionWebDAV
{

	/**The URI to the Subversion DAV namespace.*/
	public final static URI SUBVERSION_DAV_NAMESPACE_URI=URI.create("http://subversion.tigris.org/xmlns/dav/");
	/**The URI to the Subversion custom property namespace.*/
	public final static URI SUBVERSION_CUSTOM_NAMESPACE_URI=URI.create("http://subversion.tigris.org/xmlns/custom/");
	
}
