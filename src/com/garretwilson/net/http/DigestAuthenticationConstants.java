package com.garretwilson.net.http;

/**Constants for HTTP Digest Access Authentication,
<a href="http://www.ietf.org/rfc/rfc2617.txt">RFC 2617</a>,
	"HTTP Authentication: Basic and Digest Access Authentication", which obsoletes
<a href="http://www.ietf.org/rfc/rfc2069.txt">RFC 2069</a>,
	"An Extension to HTTP : Digest Access Authentication".
@author Garret Wilson
*/
public class DigestAuthenticationConstants
{
	/**The parameter for digest URI.*/
	public final static String DIGEST_URI_PARAMETER="uri";
	/**The parameter for the cnonce.*/
	public final static String CNONCE_PARAMETER="cnonce";
	/**The parameter for domain.*/
	public final static String DOMAIN_PARAMETER="domain";
	/**The parameter for the nonce.*/
	public final static String NONCE_PARAMETER="nonce";
	/**The parameter for the nonce count.*/
	public final static String NONCE_COUNT_PARAMETER="nc";
		/**The length of the nonce count hex string.*/
		public final static int NONCE_COUNT_LENGTH=8;
	/**The parameter for opaque.*/
	public final static String OPAQUE_PARAMETER="opaque";
	/**The parameter for stale.*/
	public final static String STALE_PARAMETER="stale";
	/**The parameter for algorithm.*/
	public final static String ALGORITHM_PARAMETER="algorithm";
	/**The parameter for quality of protection.*/
	public final static String QOP_PARAMETER="qop";
		/**Authentication quality of protection.*/	
		public final static String AUTH_QOP="auth";
		/**Authentication with integerity protection quality of protection.*/	
		public final static String AUTH_INT_QOP="auth-int";
	/**The parameter for response.*/
	public final static String RESPONSE_PARAMETER="response";
	/**The parameter for username.*/
	public final static String USERNAME_PARAMETER="username";


	/**The delimiter used when concatenating multiple strings before hashing.*/
	public final static char DIGEST_DELIMITER=':';
	/**The characters of the delimiter used when concatenating multiple strings before hashing.*/
	public final static char[] DIGEST_DELIMITER_CHARS=new char[]{DIGEST_DELIMITER};

}
