package com.garretwilson.net.http;

import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static com.garretwilson.lang.ByteUtilities.*;
import static com.garretwilson.lang.LongUtilities.*;
import static com.garretwilson.net.http.DigestAuthenticationConstants.*;
import static com.garretwilson.net.http.HTTPConstants.*;
import com.garretwilson.security.MessageDigestUtilities;
import static com.garretwilson.security.MessageDigestUtilities.*;
import static com.garretwilson.security.SecurityConstants.*;
import static com.garretwilson.text.FormatUtilities.*;
import com.garretwilson.util.Debug;
import com.garretwilson.util.NameValuePair;

/**An encapsulation of digest authenticate credentials of HTTP Digest Access Authentication,
<a href="http://www.ietf.org/rfc/rfc2617.txt">RFC 2617</a>,
	"HTTP Authentication: Basic and Digest Access Authentication", which obsoletes
<a href="http://www.ietf.org/rfc/rfc2069.txt">RFC 2069</a>,
	"An Extension to HTTP : Digest Access Authentication".
@author Garret Wilson
*/
public class DigestAuthenticateCredentials extends AbstractHTTPAuthentication implements AuthenticateCredentials
{

	/**The object used to create message digests.*/
	private final MessageDigest messageDigest;

		/**@return The object used to create message digests.*/
		protected MessageDigest getMessageDigest() {return messageDigest;}

	/**The username.*/
	private final String username;

		/**@return The username.*/
		public String getUsername() {return username;}

	/**The hashed server-specific data unique for each challenge.*/
	private final String nonce;

		/**@return The hashed server-specific data unique for each challenge.*/
		public String getNonce() {return nonce;}

	/**The digest URI.*/
	private final String uri;

		/**@return The digest URI.*/
		public String getURI() {return uri;}

	/**The response appropriate for these credentials.*/
	private String response;

		/**@return The response appropriate for these credentials.*/
		public String getResponse() {return response;}

	/**The cnonce value, or <code>null</code> for no value.*/
	private final String cnonce;

		/**@return The cnonce value, or <code>null</code> for no value.*/
		public String getCNonce() {return cnonce;}

	/**The opaque challenge data, or <code>null</code> for no opaque data.*/
	private final String opaque;

		/**@return The opaque challenge data, or <code>null</code> for no opaque data.*/
		public String getOpaque() {return opaque;}

	/**The quality of protection, or <code>null</code> if this data is not available or not applicable.*/
	private final QOP qop;

		/**@return The quality of protection, or <code>null</code> if this data is not available or not applicable.*/
		public QOP getQOP() {return qop;}

	/**The count of the number of requests (including the current request)
	 	that the client has sent with the nonce value in this request, or -1 if not provided.
  */
	private final long nonceCount;	//this must be a long, because an int cannot hold the full range of eight hexadecimal characters

		/**@return The count of the number of requests (including the current request)
		 	that the client has sent with the nonce value in this request, or -1 if not provided.
		*/
		public long getNonceCount() {return nonceCount;}

		/**@return The nonce count in the form <code><var>XXXXXXXX</var></code>, or <code>null</code> if there is no nonce count.
		@see #getNonceCount()
		*/
		public String getNonceCountString()
		{
			final long nonceCount=getNonceCount();	//get the nonce count
			return nonceCount>=0 ? getNonceCountString(nonceCount) : null;	//return a string representation of the nonce count, if there is one			
		}

		/**Constructs a string representation of a nonce count suitable for sending via HTTP.
		@param nonceCount The nonce count to convert to a string.
		@return The nonce count string in the form <code><var>XXXXXXXX</var></code>.
		*/
		protected static String getNonceCountString(final long nonceCount)
		{
			return toHexString(nonceCount, NONCE_COUNT_LENGTH);	//return a string representation of the nonce count			
		}

	/**@return The ID of the principal for which the credentials purport to provide authentication.*/
	public String getPrincipalID()
	{
		return getUsername();	//return the username
	}

	/**Constructs digest authentication credentials from request information.
	This implementation only supports the MD5 algorithm.
	@param username The username of the principal submitting the credentials
	@param realm The realm for which authentication is requested.
	@param nonce The hashed server-specific data unique for each challenge.
	@param digestURI The digest URI.
	@param response The response appropriate for these credentials.
	@param cnonce The cnonce value, or <code>null</code> for no value.
	@param opaque The opaque challenge data, or <code>null</code> for no opaque data.
	@param qop The quality of protection, or <code>null</code> if this data is not available or not applicable.
	@param nonceCount The count of the number of requests (including the current request)
		that the client has sent with the nonce value in this request, or -1 if not provided.
	@param algorithm The standard name of the digest algorithm. 
	@exception NoSuchAlgorithmException if the given algorithm is not recognized.
	@exception IllegalArgumentException if a quality of protection is provided and either cnonce or nonce-count
		is not provided.
	@exception NullPointerException if the realm, nonoce, username, digest URI, or response is <code>null</code>.
	*/
	public DigestAuthenticateCredentials(final String username, final String realm, final String nonce, final String digestURI, final String response,
			final String cnonce, final String opaque, final QOP qop, final long nonceCount, final String algorithm) throws NoSuchAlgorithmException
	{
		this(null, username, realm, null, nonce, digestURI, response, cnonce, opaque, qop, nonceCount, algorithm);	//construct the class with the pre-calculated response
	}

	/**Constructs digest authentication credentials from user information.
	The correct response will be calculated.
	This implementation only supports the MD5 algorithm.
	@param method The case-sensitive HTTP method used for the corresponding request.
	@param username The username of the principal submitting the credentials
	@param realm The realm for which authentication is requested.
	@param password The user password.
	@param nonce The hashed server-specific data unique for each challenge.
	@param digestURI The digest URI.
	@param cnonce The cnonce value, or <code>null</code> for no value.
	@param opaque The opaque challenge data, or <code>null</code> for no opaque data.
	@param qop The quality of protection, or <code>null</code> if this data is not available or not applicable.
	@param nonceCount The count of the number of requests (including the current request)
		that the client has sent with the nonce value in this request, or -1 if not provided.
	@param algorithm The standard name of the digest algorithm. 
	@exception NoSuchAlgorithmException if the given algorithm is not recognized.
	@exception IllegalArgumentException if a quality of protection is provided and either cnonce or nonce-count
		is not provided.
	@exception NullPointerException if the method, realm, nonoce, username, or digest URI is <code>null</code>.
	*/
	public DigestAuthenticateCredentials(final String method, final String username, final String realm, final char[] password, final String nonce, final String digestURI,
			final String cnonce, final String opaque, final QOP qop, final long nonceCount, final String algorithm) throws NoSuchAlgorithmException
	{
		this(method, username, realm, password, nonce, digestURI, null, cnonce, opaque, qop, nonceCount, algorithm);	//construct the class, allowing the response to be calculated
	}

	/**Full credential constructor.
	This implementation only supports the MD5 algorithm.
	@param method The case-sensitive HTTP method used for the corresponding request, or <code>null</code> if a response is provided.
	@param username The username of the principal submitting the credentials
	@param realm The realm for which authentication is requested.
	@param password The user password, or <code>null</code> if a response is provided.
	@param nonce The hashed server-specific data unique for each challenge.
	@param digestURI The digest URI.
	@param response The response appropriate for these credentials, or <code>null</code> if the response should be calculated from the method and password.
	@param cnonce The cnonce value, or <code>null</code> for no value.
	@param opaque The opaque challenge data, or <code>null</code> for no opaque data.
	@param qop The quality of protection, or <code>null</code> if this data is not available or not applicable.
	@param nonceCount The count of the number of requests (including the current request)
		that the client has sent with the nonce value in this request, or -1 if not provided.
	@param algorithm The standard name of the digest algorithm. 
	@exception NoSuchAlgorithmException if the given algorithm is not recognized.
	@exception IllegalArgumentException if a quality of protection is provided and either cnonce or nonce-count
		is not provided.
	@exception NullPointerException if the realm, nonoce, username, digest URI, or response is <code>null</code>, and cannot be calculated by the given information.
	*/
	protected DigestAuthenticateCredentials(final String method, final String username, final String realm, final char[] password, final String nonce, final String digestURI, final String response,
			final String cnonce, final String opaque, final QOP qop, final long nonceCount, final String algorithm) throws NoSuchAlgorithmException
	{
		super(AuthenticationScheme.DIGEST, realm);	//construct the parent class
		if(nonce==null)	//if the nonce is null
		{
			throw new NullPointerException("Nonce must be provided.");
		}
		if(qop!=null && (cnonce==null || nonceCount<0))	//if we were given a quality of protection, the cnonce and nonce-count must be given, too
		{
			throw new IllegalArgumentException("A quality of protection must be paired with both a cnonce and a nonce-count.");
		}
		if(username==null)	//if no username was given
		{
			throw new NullPointerException("Username must be provided.");
		}
		if(digestURI==null)	//if no digest URI was given
		{
			throw new NullPointerException("Digest URI must be provided.");
		}
		if(!MD5_ALGORITHM.equals(algorithm))	//currently, we only support MD5
		{
			throw new NoSuchAlgorithmException("Algorithm "+algorithm+" not supported.");
		}
		this.username=username;
		this.nonce=nonce;
		this.uri=digestURI;
		this.cnonce=cnonce;
		this.opaque=opaque;
		this.qop=qop;
		this.nonceCount=nonceCount;
		messageDigest=MessageDigest.getInstance(algorithm);	//construct the message digest
		if(response!=null)	//if a response was given
		{
			this.response=response;	//use the given response
		}
		else if(method!=null && password!=null)	//if we have a method and password
		{
			this.response=getRequestDigest(method, password);	//calculate the response request digest
		}
		else	//if we have neither a response nor a method and password
		{
			throw new NullPointerException("Response must be provided.");			
		}
	}

	/**Determines if these credentials are valid for the given request method and user password.
	A request digest is generated from the provided information and compared with the response of the credentials.
	@param method The case-sensitive HTTP method used for the corresponding request.
	@param password The user password.
	@return <code>true</code> if these credentials are valid for the given method and password.
	@see #getResponse()
	@see #getRequestDigest(String, String)
	*/
	public boolean isValid(final String method, final char[] password)
	{
//G***del Debug.trace("comparing digest", getRequestDigest(method, password), "with received response", getResponse());
		return getRequestDigest(method, password).equals(getResponse());	//see if what we calculate matches what we already have
	}
		
	/**Calculates the request digest for the given credentials using the provided request method and user password.
	@param method The case-sensitive HTTP method used for the corresponding request.
	@param password The user password.
	@return The request digest calculated from the values of the credentials and the given method and password.
	*/
	protected String getRequestDigest(final String method, final char[] password)
	{
		final MessageDigest messageDigest=getMessageDigest();	//get the message digest
		try
		{
			assert MD5_ALGORITHM.equals(getMessageDigest().getAlgorithm()) : "Only the MD5 algorithm is supported.";	//TODO complete for MD5-sess
				//H(A1)
			messageDigest.reset();	//reset the message digest for calculating H(A1)
			update(messageDigest, getUsername());	//username
			update(messageDigest, DIGEST_DELIMITER_CHARS);	//:
			update(messageDigest, getRealm());	//realm
			update(messageDigest, DIGEST_DELIMITER_CHARS);	//:
			update(messageDigest, password);	//password
			final String hashA1=toHexString(messageDigest.digest());	//calculate H(A1)
			final QOP qop=getQOP();	//get the quality of protection
			assert qop!=QOP.AUTH_INT : "Quality of Protection "+getQOP()+" not supported.";	//TODO complete for auth-int
				//H(A2)
			messageDigest.reset();	//reset the message digest for calculating H(A2)
			update(messageDigest, method);	//method
			update(messageDigest, DIGEST_DELIMITER_CHARS);	//:
			update(messageDigest, getURI());	//digest-uri
			final String hashA2=toHexString(messageDigest.digest());	//calculate H(A2)
				//request-digest
			messageDigest.reset();	//reset the message digest for calculating request-digest
			update(messageDigest, hashA1);	//H(A1)
			update(messageDigest, DIGEST_DELIMITER_CHARS);	//:
			update(messageDigest, getNonce());	//nonce
			if(qop==QOP.AUTH || qop==QOP.AUTH_INT)	//if we get a quality of protection we recognize
			{
				update(messageDigest, DIGEST_DELIMITER_CHARS);	//:
				update(messageDigest, getNonceCountString());	//nc-value
				update(messageDigest, DIGEST_DELIMITER_CHARS);	//:
				update(messageDigest, getCNonce());	//cnonce-value
				update(messageDigest, DIGEST_DELIMITER_CHARS);	//:
				update(messageDigest, qop.toString());	//qop-value
			}
			else if(qop!=null)	//if an unrecognized quality of protection is present (this is not possible unless the QOP enum is enlarged)
			{
				throw new AssertionError("Unknown QOP.");
			}
			update(messageDigest, DIGEST_DELIMITER_CHARS);	//:
			update(messageDigest, hashA2);	//H(A2)
			return toHexString(messageDigest.digest());	//calculate request-digest			
		}
		finally
		{
			messageDigest.reset();	//always reset the message digest
		}
	}

	/**Calculates the hash of the username, realm, and password, indicated by H(A1) in RFC 2617.
	@param username The username.
	@param realm The realm for which authentication is requested.
	@param password The user password.
	@return The hash of the username, realm, and password, with correct delimiters, as specified by RFC 2617.
	*/
/*G***fix
	public static String getUsernameRealmPasswordHash(final String username, final String realm, final char[] password)
	{
		final MessageDigest messageDigest=getMessageDigest();	//get the message digest
		try
		{
			assert MD5_ALGORITHM.equals(getMessageDigest().getAlgorithm()) : "Only the MD5 algorithm is supported.";	//TODO complete for MD5-sess
				//H(A1)
			messageDigest.reset();	//reset the message digest for calculating H(A1)
			update(messageDigest, getUsername());	//username
			update(messageDigest, DIGEST_DELIMITER_CHARS);	//:
			update(messageDigest, getRealm());	//realm
			update(messageDigest, DIGEST_DELIMITER_CHARS);	//:
			update(messageDigest, password);	//password
			final String hashA1=toHexString(messageDigest.digest());	//calculate H(A1)
			final QOP qop=getQOP();	//get the quality of protection
			assert qop!=QOP.AUTH_INT : "Quality of Protection "+getQOP()+" not supported.";	//TODO complete for auth-int
				//H(A2)
			messageDigest.reset();	//reset the message digest for calculating H(A2)
			update(messageDigest, method);	//method
			update(messageDigest, DIGEST_DELIMITER_CHARS);	//:
			update(messageDigest, getURI());	//digest-uri
			final String hashA2=toHexString(messageDigest.digest());	//calculate H(A2)
				//request-digest
			messageDigest.reset();	//reset the message digest for calculating request-digest
			update(messageDigest, hashA1);	//H(A1)
			update(messageDigest, DIGEST_DELIMITER_CHARS);	//:
			update(messageDigest, getNonce());	//nonce
			if(qop==QOP.AUTH || qop==QOP.AUTH_INT)	//if we get a quality of protection we recognize
			{
				update(messageDigest, DIGEST_DELIMITER_CHARS);	//:
				update(messageDigest, getNonceCountString());	//nc-value
				update(messageDigest, DIGEST_DELIMITER_CHARS);	//:
				update(messageDigest, getCNonce());	//cnonce-value
				update(messageDigest, DIGEST_DELIMITER_CHARS);	//:
				update(messageDigest, qop.toString());	//qop-value
			}
			else if(qop!=null)	//if an unrecognized quality of protection is present (this is not possible unless the QOP enum is enlarged)
			{
				throw new AssertionError("Unknown QOP.");
			}
			update(messageDigest, DIGEST_DELIMITER_CHARS);	//:
			update(messageDigest, hashA2);	//H(A2)
			return toHexString(messageDigest.digest());	//calculate request-digest			
		}
		finally
		{
			messageDigest.reset();	//always reset the message digest
		}
	}
*/

	/**@return The authorization parameters for this challenge.
	This version only returns the realm parameter.
	Child classes may override this method and append other parameters to the list before returning it.
	*/
/*G***del
	public List<NameValuePair<String, String>> getParameters()
	{
		final List<NameValuePair<String, String>> parameterList=new ArrayList<NameValuePair<String, String>>();	//create the list of parameters
		parameterList.add(new NameValuePair<String, String>(REALM_PARAMETER, getRealm()));	//add the realm parameter
		return parameterList;	//return the list of parameters 
	}
*/

	/**@return A string representation of the challenge.*/
/*G***del
	public final String toString()
	{
		final StringBuilder stringBuilder=new StringBuilder();
		stringBuilder.append(getScheme()).append(SP);	//authentication scheme
		final List<NameValuePair<String, String>> parameters=getParameters();	//get the challenge parameters
		formatAttributes(stringBuilder, parameters.toArray(new NameValuePair[parameters.size()]));	//parameters
		return stringBuilder.toString();
	}
*/

}
