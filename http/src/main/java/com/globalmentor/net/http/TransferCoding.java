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
 * Transfer coding values for HyperText Transfer Protocol (HTTP) as defined by <a href="https://www.ietf.org/rfc/rfc2616.txt">RFC 2616</a>,
 * "Hypertext Transfer Protocol -- HTTP/1.1", 3.6.
 * @see <a href="https://www.ietf.org/rfc/rfc2616.txt">RFC 2616</a>
 * @author garret Garret Wilson
 */
public enum TransferCoding {
	/** The HTTP chunked transfer coding. */
	chunked,
	/** The HTTP COMPRESS transfer coding. */
	compress,
	/** The HTTP deflate transfer coding. */
	deflate,
	/** The HTTP gzip transfer coding. */
	gzip,
	/** The HTTP identity transfer coding. */
	identity;
}
