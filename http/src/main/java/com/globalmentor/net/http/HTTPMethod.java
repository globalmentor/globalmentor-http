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

package com.globalmentor.net.http;

/**
 * HTTP methods as defined by <a href="https://www.ietf.org/rfc/rfc2616.txt">RFC 2616</a>, "Hypertext Transfer Protocol -- HTTP/1.1".
 * @author Garret Wilson
 */
public enum HTTPMethod {

	/** <code>CONNECT</code> */
	CONNECT,
	/** <code>DELETE</code> */
	DELETE,
	/** <code>GET</code> */
	GET,
	/** <code>HEAD</code> */
	HEAD,
	/** <code>POST</code> */
	POST,
	/** <code>PUT</code> */
	PUT,
	/** <code>OPTIONS</code> */
	OPTIONS,
	/** <code>TRACE</code> */
	TRACE

}
