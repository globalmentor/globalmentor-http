package com.garretwilson.net.http;

import java.security.*;
import java.util.*;

import static com.garretwilson.lang.CharacterUtilities.*;
import static com.garretwilson.net.http.DigestAuthenticationConstants.*;
import static com.garretwilson.net.http.HTTPConstants.*;
import static com.garretwilson.security.SecurityConstants.*;
import static com.garretwilson.text.CharacterConstants.*;
import static com.garretwilson.text.FormatUtilities.*;
import static com.garretwilson.util.Base64.*;
import com.garretwilson.util.NameValuePair;

/**An encapsulation of a digest authenticate challenge of HTTP Digest Access Authentication,
<a href="http://www.ietf.org/rfc/rfc2617.txt">RFC 2617</a>,
	"HTTP Authentication: Basic and Digest Access Authentication", which obsoletes
<a href="http://www.ietf.org/rfc/rfc2069.txt">RFC 2069</a>,
	"An Extension to HTTP : Digest Access Authentication".
@author Garret Wilson
*/
public class DigestAuthenticateChallenge extends AuthenticateChallenge
{
	
	/**The object used to create message digests.*/
	private final MessageDigest messageDigest;

		/**@return The object used to create message digests.*/
		protected MessageDigest getMessageDigest() {return messageDigest;}

	/**The un-hashed server-specific data unique for each challenge.*/
	private final String nonce;

		/**@return The un-hashed server-specific data unique for each challenge.*/
		public String getNonce() {return nonce;}

	/**The opaque challenge data, or <code>null</code> for no opaque data.*/
	private final String opaque;

		/**@return The opaque challenge data, or <code>null</code> for no opaque data.*/
		public String getOpaque() {return opaque;}

	/**Whether the previous request from the client was rejected because the nonce value was stale.*/
	private boolean stale;

		/**@return Whether the previous request from the client was rejected because the nonce value was stale.*/
		public boolean isStale() {return stale;}
		
		/**Sets whether the previous request from the client was rejected because the nonce value was stale.
		@param stale <code>true</code> if the nonce value from a previous request was stale.
		*/
		public void setStale(final boolean stale) {this.stale=stale;}

	/**The array of quality of protection options, such as such as <code>AUTH_QOP</code>.*/
	private String[] qopOptions=new String[]{AUTH_QOP};	//default to authentication quality of protection

		/**@return The array of quality of protection options, such as such as <code>AUTH_QOP</code>.*/
		public String[] getQOPOptions() {return qopOptions;}
		
		/**Sets the quality of protection.
		@param qopOptions The quality of protection options.
		*/
		public void setQOPOptions(final String... qopOptions) {this.qopOptions=qopOptions;}

	/**Constructs a digest authentication challenge with the MD5 algorithm and no opaque data.
	Defaults to authentication quality of protection.
	@param realm The realm for which authentication is requested.
	@param nonce The un-hashed server-specific data unique for each challenge.
	@exception NoSuchAlgorithmException if the given algorithm is not recognized.
	@exception NullPointerException if the realm or the nonce is <code>null</code>.
	*/
	public DigestAuthenticateChallenge(final String realm, final String nonce) throws NoSuchAlgorithmException
	{
		this(realm, nonce, null);	//construct the challenge with no opaque data
	}

	/**Constructs a digest authentication challenge with the MD5 algorithm.
	Defaults to authentication quality of protection.
	@param realm The realm for which authentication is requested.
	@param nonce The un-hashed server-specific data unique for each challenge.
	@param opaque The opaque challenge data, or <code>null</code> for no opaque data.
	@exception NoSuchAlgorithmException if the given algorithm is not recognized.
	@exception NullPointerException if the realm or the nonce is <code>null</code>.
	*/
	public DigestAuthenticateChallenge(final String realm, final String nonce, final String opaque) throws NoSuchAlgorithmException
	{
		this(realm, nonce, opaque, MD5_ALGORITHM);	//construct the challenge with the MD5 algorithm
	}

	/**Constructs a digest authentication challenge.
	Defaults to authentication quality of protection.
	@param realm The realm for which authentication is requested.
	@param nonce The un-hashed server-specific data unique for each challenge.
	@param opaque The opaque challenge data, or <code>null</code> for no opaque data.
	@param algorithm The standard name of the digest algorithm. 
	@exception NoSuchAlgorithmException if the given algorithm is not recognized.
	@exception NullPointerException if the realm or the nonce is <code>null</code>.
	*/
	public DigestAuthenticateChallenge(final String realm, final String nonce, final String opaque, final String algorithm) throws NoSuchAlgorithmException
	{
		super(AuthenticationScheme.DIGEST, realm);	//construct the parent class
		if(nonce==null)	//if the nonce is null
		{
			throw new NullPointerException("Nonce must be provided.");
		}
		this.nonce=nonce;
		this.opaque=opaque;
		messageDigest=MessageDigest.getInstance(algorithm);	//construct the message digest
	}

	/**@return The authorization parameters for this challenge.*/
	public List<NameValuePair<String, String>> getParameters()
	{
		final List<NameValuePair<String, String>> parameterList=super.getParameters();	//get the default parameters
			//TODO implement domain
		final byte[] nonceBytes=toByteArray(getNonce().toCharArray());	//convert the nonce to bytes
		final byte[] nonceDigest=getMessageDigest().digest(nonceBytes);	//calculate the nonce digest
		final String nonceDigestBase64=encodeBytes(nonceDigest);	//base64-encode the nonce
		parameterList.add(new NameValuePair<String, String>(NONCE_PARAMETER, nonceDigestBase64));	//nonce
		parameterList.add(new NameValuePair<String, String>(OPAQUE_PARAMETER, getOpaque()));	//opaque
		final boolean stale=isStale();	//see if staleness should be indicated
		if(stale)	//if an earlier request had a stale nonce
		{
			parameterList.add(new NameValuePair<String, String>(STALE_PARAMETER, Boolean.toString(stale)));	//stale				
		}
		parameterList.add(new NameValuePair<String, String>(ALGORITHM_PARAMETER, getMessageDigest().getAlgorithm()));	//algorithm
		final String[] qop=getQOPOptions();	//get the quality of protection options
		if(qop!=null)	//if quality of protection was specified
		{
			parameterList.add(new NameValuePair<String, String>(QOP_PARAMETER, formatList(new StringBuilder(), COMMA_CHAR, qop).toString()));	//qop
		}
		return parameterList;	//return the list of parameters 
	}
}
