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

import com.globalmentor.net.http.HTTPRequest;
import com.globalmentor.net.http.webdav.WebDAV;

/**
 * A WebDAV request as defined by <a href="http://www.ietf.org/rfc/rfc2518.txt">RFC 2518</a>, "HTTP Extensions for Distributed Authoring -- WEBDAV".
 * @author Garret Wilson
 */
public interface WebDAVRequest extends HTTPRequest {

	/**
	 * Gets the requested depth from the header.
	 * @return The depth or <code>Depth.INFINITY</code> if an infinite, undefined, or or unrecognized depth is indicated.
	 * @see WebDAV#DEPTH_HEADER
	 */
	public Depth getDepth();

	/**
	 * Sets the depth.
	 * @param depth The requested depth.
	 * @see WebDAV#DEPTH_HEADER
	 */
	public void setDepth(final Depth depth);

	/**
	 * Retrieves the destination URI.
	 * @return The URI indicating the destination of a COPY or MOVE, or <code>null</code> if the {@value WebDAV#DESTINATION_HEADER} header is not present.
	 * @throws IllegalArgumentException if the destination header value does not represent a valid URI or the represented URI is not absolute.
	 * @see WebDAV#DESTINATION_HEADER
	 */
	public URI getDestination();

	/**
	 * Sets the destination URI. The destination header value is ignored if it does not represennt a valid URI or the represented URI is not absolute.
	 * @param destinationURI The absolute URI indicating the destination of a COPY or MOVE.
	 * @throws IllegalArgumentException if the given destination URI is not absolute.
	 * @see WebDAV#DESTINATION_HEADER
	 */
	public void setDestination(final URI destinationURI);

	/**
	 * Returns the overwrite status.
	 * @return <code>true</code> if the WebDAV {@value WebDAV#OVERWRITE_HEADER} header is missing or {@value WebDAV#OVERWRITE_TRUE}, or <code>false</code> if the
	 *         value is {@value WebDAV#OVERWRITE_FALSE}.
	 * @throws IllegalArgumentException if the overwrite header is present and is not {@value WebDAV#OVERWRITE_TRUE} or {@value WebDAV#OVERWRITE_FALSE}.
	 * @see WebDAV#OVERWRITE_HEADER
	 * @see WebDAV#OVERWRITE_FALSE
	 * @see WebDAV#OVERWRITE_TRUE
	 */
	public boolean isOverwrite() throws IllegalArgumentException;

	/**
	 * Sets the overwrite status.
	 * @param overwrite <code>true</code> if the the WebDAV {@value WebDAV#OVERWRITE_HEADER} should be set to {@value WebDAV#OVERWRITE_TRUE}, else
	 *          <code>false</code> if it should be set to {@value WebDAV#OVERWRITE_FALSE}.
	 * @see WebDAV#OVERWRITE_HEADER
	 * @see WebDAV#OVERWRITE_FALSE
	 * @see WebDAV#OVERWRITE_TRUE
	 */
	public void setOverwrite(final boolean overwrite);

}
