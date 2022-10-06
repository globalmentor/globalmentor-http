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

/**
 * WebDAV HTTP methods as defined by <a href="http://www.ietf.org/rfc/rfc2518.txt">RFC 2518</a>, "HTTP Extensions for Distributed Authoring -- WEBDAV".
 * @author Garret Wilson
 */
public enum WebDAVMethod {

	/** <code>COPY</code> */
	COPY,
	/*TODO decide if this is properly a WebDAV method CONNECT,*/
	/** <code>DELETE</code> */
	DELETE,
	/** <code>GET</code> */
	GET,
	/** <code>HEAD</code> */
	HEAD,
	/** <code>LOCK</code> */
	LOCK,
	/** <code>MKCOL</code> */
	MKCOL,
	/** <code>MOVE</code> */
	MOVE,
	/** <code>OPTIONS</code> */
	OPTIONS,
	/** <code>PUT</code> */
	PUT,
	/** <code>POST</code> */
	POST,
	/** <code>PROPFIND</code> */
	PROPFIND,
	/** <code>PROPPATCH</code> */
	PROPPATCH,
	/** <code>TRACE</code> */
	TRACE,
	/** <code>UNLOCK</code> */
	UNLOCK

}
