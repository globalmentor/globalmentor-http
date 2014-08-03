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

package com.globalmentor.net.http;

/**
 * Constants for HTTP Digest Access Authentication, <a href="http://www.ietf.org/rfc/rfc2617.txt">RFC 2617</a>,
 * "HTTP Authentication: Basic and Digest Access Authentication", which obsoletes <a href="http://www.ietf.org/rfc/rfc2069.txt">RFC 2069</a>,
 * "An Extension to HTTP : Digest Access Authentication".
 * @author Garret Wilson
 */
public class DigestAuthentication {

	/** The parameter for digest URI. */
	public final static String DIGEST_URI_PARAMETER = "uri";
	/** The parameter for the cnonce. */
	public final static String CNONCE_PARAMETER = "cnonce";
	/** The parameter for domain. */
	public final static String DOMAIN_PARAMETER = "domain";
	/** The parameter for the nonce. */
	public final static String NONCE_PARAMETER = "nonce";
	/** The parameter for the nonce count. */
	public final static String NONCE_COUNT_PARAMETER = "nc";
	/** The length of the nonce count hex string. */
	public final static int NONCE_COUNT_LENGTH = 8;
	/** The parameter for opaque. */
	public final static String OPAQUE_PARAMETER = "opaque";
	/** The parameter for stale. */
	public final static String STALE_PARAMETER = "stale";
	/** The parameter for algorithm. */
	public final static String ALGORITHM_PARAMETER = "algorithm";
	/** The parameter for quality of protection. */
	public final static String QOP_PARAMETER = "qop";
	/** Authentication quality of protection. */
	public final static String AUTH_QOP = "auth";
	/** Authentication with integerity protection quality of protection. */
	public final static String AUTH_INT_QOP = "auth-int";
	/** The parameter for response. */
	public final static String RESPONSE_PARAMETER = "response";
	/** The parameter for username. */
	public final static String USERNAME_PARAMETER = "username";

	/** The delimiter used when concatenating multiple strings before hashing. */
	public final static char DIGEST_DELIMITER = ':';
	/** The characters of the delimiter used when concatenating multiple strings before hashing. */
	public final static char[] DIGEST_DELIMITER_CHARS = new char[] { DIGEST_DELIMITER };

}
