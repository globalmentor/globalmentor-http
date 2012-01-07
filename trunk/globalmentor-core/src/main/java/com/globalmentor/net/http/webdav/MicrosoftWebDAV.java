/*
 * Copyright © 2008-2012 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

/**
 * WebDAV constants used by Microsoft to mount drives on WebDAV in Windows Vista.
 * @author Garret Wilson
 * @see <a href="http://msdn.microsoft.com/en-us/library/cc250176.aspx">WebDAV Microsoft Extensions Server Details: Server Properties</a>
 */
public class MicrosoftWebDAV
{

	/** The URI to the Microsoft property namespace. */
	public final static URI MICROSOFT_WEBDAV_PROPERTY_NAMESPACE_URI = URI.create("urn:schemas-microsoft-com:");

	/** The creation timestamp in the format specified by RFC 1123. */
	public final static WebDAVPropertyName WIN32_CREATION_TIME = new WebDAVPropertyName(MICROSOFT_WEBDAV_PROPERTY_NAMESPACE_URI, "Win32CreationTime");
	/** The last accessed timestamp in the format specified by RFC 1123. */
	public final static WebDAVPropertyName WIN32_LAST_ACCESS_TIME = new WebDAVPropertyName(MICROSOFT_WEBDAV_PROPERTY_NAMESPACE_URI, "Win32LastAccessTime");
	/** The last modified timestamp in the format specified by RFC 1123. */
	public final static WebDAVPropertyName WIN32_LAST_MODIFIED_TIME = new WebDAVPropertyName(MICROSOFT_WEBDAV_PROPERTY_NAMESPACE_URI, "Win32LastModifiedTime");
	public final static WebDAVPropertyName WIN32_FILE_ATTRIBUTES = new WebDAVPropertyName(MICROSOFT_WEBDAV_PROPERTY_NAMESPACE_URI, "Win32FileAttributes");

}
