package com.garretwilson.net.http;

import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static com.garretwilson.lang.LongUtilities.*;
import static com.garretwilson.net.http.DigestAuthenticationConstants.*;
import static com.garretwilson.net.http.HTTPConstants.*;
import static com.garretwilson.security.MessageDigestUtilities.*;
import static com.garretwilson.security.SecurityConstants.*;
import static com.garretwilson.text.FormatUtilities.*;
import static com.garretwilson.util.Base64.*;
import com.garretwilson.util.Debug;
import com.garretwilson.util.NameValuePair;


/**An encapsulation of digest authenticate credentials of HTTP Digest Access Authentication,
<a href="http://www.ietf.org/rfc/rfc2617.txt">RFC 2617</a>,
	"HTTP Authentication: Basic and Digest Access Authentication", which obsoletes
<a href="http://www.ietf.org/rfc/rfc2069.txt">RFC 2069</a>,
	"An Extension to HTTP : Digest Access Authentication".
@author Garret Wilson
*/
public class DigestAuthenticateCredentials extends AbstractAuthentication implements AuthenticateCredentials
{

	/**The object used to create message digests.*/
	private final MessageDigest messageDigest;

		/**@return The object used to create message digests.*/
		protected MessageDigest getMessageDigest() {return messageDigest;}

	/**The username.*/
	private final String username;

		/**@return The username.*/
		public String getUsername() {return username;}

	/**The un-hashed server-specific data unique for each challenge.*/
	private final String nonce;

		/**@return The un-hashed server-specific data unique for each challenge.*/
		public String getNonce() {return nonce;}

	/**The digest URI.*/
	private final URI uri;

		/**@return The digest URI.*/
		public URI getURI() {return uri;}

	/**The response appropriate for these credentials.*/
	private final String response;

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

	/**Constructs digest authentication credentials.
	This implementation only supports the MD5 algorithm.
	@param username The username of the principal submitting the credentials
	@param realm The realm for which authentication is requested.
	@param nonce The un-hashed server-specific data unique for each challenge.
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
	public DigestAuthenticateCredentials(final String username, final String realm, final String nonce, final URI digestURI, final String response,
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
		if(response==null)	//if no response was given
		{
			throw new NullPointerException("Response must be provided.");
		}
		if(!MD5_ALGORITHM.equals(algorithm))	//currently, we only support MD5
		{
			throw new NoSuchAlgorithmException("Algorithm "+algorithm+" not supported.");
		}
		this.username=username;
		this.nonce=nonce;
		this.uri=digestURI;
		this.response=response;
		this.cnonce=cnonce;
		this.opaque=opaque;
		this.qop=qop;
		this.nonceCount=nonceCount;
		messageDigest=MessageDigest.getInstance(algorithm);	//construct the message digest
	}

	/**Determines if these credentials are valid for the given request method and user password.
	A request digest is generated from the provided information and compared with the response of the credentials.
	@param method The case-sensitive HTTP method used for the corresponding request.
	@param password The user password.
	@return <code>true</code> if these credentials are valid for the given method and password.
	@see #getResponse()
	@see #getRequestDigest(String, String)
	*/
	public boolean isValid(final String method, final String password)
	{
Debug.trace("checking validity with method", method, "password", password);
Debug.trace("our response", getResponse());
Debug.trace("expected response", getRequestDigest(method, password));
		return getRequestDigest(method, password).equals(getResponse());	//see if what we calculate matches what we already have
	}
		
	/**Calculates the request digest for the given credentials using the provided request method and user password.
	@param method The case-sensitive HTTP method used for the corresponding request.
	@param password The user password.
	@return The request digest calculated from the values of the credentials and the given method and password.
	*/
	protected String getRequestDigest(final String method, final String password)
	{
		assert MD5_ALGORITHM.equals(getMessageDigest().getAlgorithm()) : "Only the MD5 algorithm is supported.";	//TODO complete for MD5-sess
		final String a1=getUsername()+DIGEST_DELIMITER+getRealm()+DIGEST_DELIMITER+password;	//construct the A1 value to be hashed
		final QOP qop=getQOP();	//get the quality of protection
		assert qop!=QOP.AUTH_INT : "Quality of Protection "+getQOP()+" not supported.";	//TODO complete for auth-int
		final String a2=method+DIGEST_DELIMITER+getURI();	//constuct the A2 value to be hashed
		final String token;	//determine the token to hash
		if(qop==QOP.AUTH || qop==QOP.AUTH_INT)	//if the quality of protection is given
		{
			token=encodeBytes(digest(getMessageDigest(), getNonce()))	//H(A1)
					+DIGEST_DELIMITER+getNonce()	//nonce-value
					+DIGEST_DELIMITER+toHexString(getNonceCount(), NONCE_COUNT_LENGTH)	//nc-value: nonce-count, eight characters
					+DIGEST_DELIMITER+getCNonce()	//cnonce-value
					+DIGEST_DELIMITER+getQOP()	//qop-value
					+DIGEST_DELIMITER+encodeBytes(digest(getMessageDigest(), a2));	//H(A2)
		}
		else if(qop==null)	//if the quality of protection is not present
		{
			token=encodeBytes(digest(getMessageDigest(), getNonce()))	//H(A1)
					+DIGEST_DELIMITER+getNonce()	//nonce-value
					+DIGEST_DELIMITER+encodeBytes(digest(getMessageDigest(), a2));	//H(A2)
		}
		else	//if an unrecognized value is present (this is not possible unless the QOP enum is enlarged)
		{
			throw new AssertionError("Unknown QOP.");
		}
		return encodeBytes(digest(getMessageDigest(), token));	//hash the token to get the request digest
	}
		
	/**@return The authorization parameters for this challenge.
	This version only returns the realm parameter.
	Child classes may override this method and append other parameters to the list before returning it.
	*/
	public List<NameValuePair<String, String>> getParameters()
	{
		final List<NameValuePair<String, String>> parameterList=new ArrayList<NameValuePair<String, String>>();	//create the list of parameters
		parameterList.add(new NameValuePair<String, String>(REALM_PARAMETER, getRealm()));	//add the realm parameter
		return parameterList;	//return the list of parameters 
	}

	/**@return A string representation of the challenge.*/
	public final String toString()
	{
		final StringBuilder stringBuilder=new StringBuilder();
		stringBuilder.append(getScheme()).append(SP_CHAR);	//authentication scheme
		final List<NameValuePair<String, String>> parameters=getParameters();	//get the challenge parameters
		formatAttributes(stringBuilder, parameters.toArray(new NameValuePair[parameters.size()]));	//parameters
		return stringBuilder.toString();
	}

}
