/*
 * Copyright © 1996-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.globalmentor.net.http.webdav;

import static com.globalmentor.net.http.webdav.WebDAV.*;

import java.net.URI;

/**
 * WebDAV constants used by South River Technologies, makers of WebDrive. Some information gathered from communication with WebDrive tech support
 * representative, e.g. 2007-03-01.
 * @author Garret Wilson
 * @see <a href="https://webdrive.com/">WebDrive</a>
 */
public class SRTWebDAV {

	/** The URI to the South River Technologies WebDAV property namespace. */
	public static final URI SRT_WEBDAV_PROPERTY_NAMESPACE_URI = URI.create("http://www.southrivertech.com/");

	//property names deprecated because they were being used in the WebDAV namespace
	/**
	 * The creation time, using the RFC 3339 Internet timestamp ISO 8601 profile, as set by WebDrive; deprecated by South River Technologies because it uses the
	 * standard WebDAV namespace.
	 */
	public static final WebDAVPropertyName DEPRECATED_CREATION_TIME_PROPERTY_NAME = new WebDAVPropertyName(WEBDAV_NAMESPACE, "srt_creationtime");

	/**
	 * The last modified time, using the RFC 3339 Internet timestamp ISO 8601 profile, as set by WebDrive; deprecated by South River Technologies because it uses
	 * the standard WebDAV namespace.
	 */
	public static final WebDAVPropertyName DEPRECATED_MODIFIED_TIME_PROPERTY_NAME = new WebDAVPropertyName(WEBDAV_NAMESPACE, "srt_modifiedtime");

	/**
	 * The server's last modified time, using the RFC 3339 Internet timestamp ISO 8601 profile, last known to WebDrive as reported by the
	 * {@link WebDAV#GET_LAST_MODIFIED_PROPERTY_NAME} property; deprecated by South River Technologies because it uses the standard WebDAV namespace. WebDrive
	 * stores this as a duplicate time value so that it will know if any other software modified the file without WebDrive's knowledge. If the value of this
	 * property matches the {@link WebDAV#GET_LAST_MODIFIED_PROPERTY_NAME} property, WebDrive will assume that {@link #DEPRECATED_MODIFIED_TIME_PROPERTY_NAME}
	 * stores the correct last modified time set by WebDrive.
	 */
	public static final WebDAVPropertyName DEPRECATED_TIMESTAMP_PROPERTY_NAME = new WebDAVPropertyName(WEBDAV_NAMESPACE, "srt_proptimestamp");

}
