package com.garretwilson.net.http;

import java.security.*;

import static com.globalmentor.java.Objects.*;
import static com.globalmentor.security.MessageDigests.*;
import static com.globalmentor.text.TextFormatter.*;

/**An encapsulation of a digest authenticate challenge of HTTP Digest Access Authentication,
<a href="http://www.ietf.org/rfc/rfc2617.txt">RFC 2617</a>,
	"HTTP Authentication: Basic and Digest Access Authentication", which obsoletes
<a href="http://www.ietf.org/rfc/rfc2069.txt">RFC 2069</a>,
	"An Extension to HTTP : Digest Access Authentication".
@author Garret Wilson
*/
public class DigestAuthenticateChallenge extends AbstractAuthenticateChallenge
{
	
	/**The object used to create message digests.*/
	private final MessageDigest messageDigest;

		/**@return The object used to create message digests.*/
		protected MessageDigest getMessageDigest() {return messageDigest;}

	/**The un-hashed server-specific data unique for each challenge.*/
	private final String nonce;

		/**@return The un-hashed server-specific data unique for each challenge.*/
		public String getNonce() {return nonce;}

		/**@return The hashed nonce value.*/
		public String getNonceDigest()
		{
			return formatHex(new StringBuilder(), digest(getMessageDigest(), getNonce())).toString();	//calculate the nonce digest
		}

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

	/**The array of quality of protection options.*/
	private QOP[] qopOptions=new QOP[]{QOP.AUTH};	//default to authentication quality of protection

		/**@return The array of quality of protection options.*/
		public QOP[] getQOPOptions() {return qopOptions;}
		
		/**Sets the quality of protection.
		@param qopOptions The quality of protection options.
		*/
		public void setQOPOptions(final QOP... qopOptions) {this.qopOptions=qopOptions;}

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

	/**Constructs a digest authentication challenge with the MD5 algorithm and no opaque data.
	Defaults to authentication quality of protection.
	@param realm The realm for which authentication is requested.
	@param nonce The un-hashed server-specific data unique for each challenge.
	@param stale Whether the previous request from the client was rejected because the nonce value was stale.
	@exception NoSuchAlgorithmException if the given algorithm is not recognized.
	@exception NullPointerException if the realm or the nonce is <code>null</code>.
	*/
	public DigestAuthenticateChallenge(final String realm, final String nonce, final boolean stale) throws NoSuchAlgorithmException
	{
		this(realm, nonce, null, stale);	//construct the challenge with no opaque data
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

	/**Constructs a digest authentication challenge with the MD5 algorithm.
	Defaults to authentication quality of protection.
	@param realm The realm for which authentication is requested.
	@param nonce The un-hashed server-specific data unique for each challenge.
	@param opaque The opaque challenge data, or <code>null</code> for no opaque data.
	@param stale Whether the previous request from the client was rejected because the nonce value was stale.
	@exception NoSuchAlgorithmException if the given algorithm is not recognized.
	@exception NullPointerException if the realm or the nonce is <code>null</code>.
	*/
	public DigestAuthenticateChallenge(final String realm, final String nonce, final String opaque, final boolean stale) throws NoSuchAlgorithmException
	{
		this(realm, nonce, opaque, stale, MD5_ALGORITHM);	//construct the challenge with the MD5 algorithm
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
		this(realm, nonce, opaque, false, algorithm);	 //construct a non-stale challenge
	}

	/**Constructs a digest authentication challenge.
	Defaults to authentication quality of protection.
	@param realm The realm for which authentication is requested.
	@param nonce The un-hashed server-specific data unique for each challenge.
	@param opaque The opaque challenge data, or <code>null</code> for no opaque data.
	@param stale Whether the previous request from the client was rejected because the nonce value was stale.
	@param algorithm The standard name of the digest algorithm.
	@exception NoSuchAlgorithmException if the given algorithm is not recognized.
	@exception NullPointerException if the realm or the nonce is <code>null</code>.
	*/
	public DigestAuthenticateChallenge(final String realm, final String nonce, final String opaque, final boolean stale, final String algorithm) throws NoSuchAlgorithmException
	{
		super(AuthenticationScheme.DIGEST, realm);	//construct the parent class
		this.nonce=checkInstance(nonce, "Nonce must be provided");
		this.opaque=opaque;
		this.stale=stale;
		messageDigest=MessageDigest.getInstance(algorithm);	//construct the message digest
	}

}
