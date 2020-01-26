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

import java.security.*;

import static com.globalmentor.security.MessageDigests.*;
import static com.globalmentor.text.TextFormatter.*;

import static java.util.Objects.*;

/**
 * An encapsulation of a digest authenticate challenge of HTTP Digest Access Authentication, <a href="http://www.ietf.org/rfc/rfc2617.txt">RFC 2617</a>,
 * "HTTP Authentication: Basic and Digest Access Authentication", which obsoletes <a href="http://www.ietf.org/rfc/rfc2069.txt">RFC 2069</a>,
 * "An Extension to HTTP : Digest Access Authentication".
 * @author Garret Wilson
 */
public class DigestAuthenticateChallenge extends AbstractAuthenticateChallenge {

	/** The object used to create message digests. */
	private final MessageDigest messageDigest;

	/** @return The object used to create message digests. */
	protected MessageDigest getMessageDigest() {
		return messageDigest;
	}

	/** The un-hashed server-specific data unique for each challenge. */
	private final String nonce;

	/** @return The un-hashed server-specific data unique for each challenge. */
	public String getNonce() {
		return nonce;
	}

	/** @return The hashed nonce value. */
	public String getNonceDigest() {
		return formatHex(digest(getMessageDigest(), getNonce())); //calculate the nonce digest
	}

	/** The opaque challenge data, or <code>null</code> for no opaque data. */
	private final String opaque;

	/** @return The opaque challenge data, or <code>null</code> for no opaque data. */
	public String getOpaque() {
		return opaque;
	}

	/** Whether the previous request from the client was rejected because the nonce value was stale. */
	private boolean stale;

	/** @return Whether the previous request from the client was rejected because the nonce value was stale. */
	public boolean isStale() {
		return stale;
	}

	/**
	 * Sets whether the previous request from the client was rejected because the nonce value was stale.
	 * @param stale <code>true</code> if the nonce value from a previous request was stale.
	 */
	public void setStale(final boolean stale) {
		this.stale = stale;
	}

	/** The array of quality of protection options. */
	private QOP[] qopOptions = new QOP[] { QOP.AUTH }; //default to authentication quality of protection

	/** @return The array of quality of protection options. */
	public QOP[] getQOPOptions() {
		return qopOptions;
	}

	/**
	 * Sets the quality of protection.
	 * @param qopOptions The quality of protection options.
	 */
	public void setQOPOptions(final QOP... qopOptions) {
		this.qopOptions = qopOptions;
	}

	/**
	 * Constructs a digest authentication challenge with the MD5 algorithm and no opaque data. Defaults to authentication quality of protection.
	 * @param realm The realm for which authentication is requested.
	 * @param nonce The un-hashed server-specific data unique for each challenge.
	 * @throws NoSuchAlgorithmException if the given algorithm is not recognized.
	 * @throws NullPointerException if the realm or the nonce is <code>null</code>.
	 */
	public DigestAuthenticateChallenge(final String realm, final String nonce) throws NoSuchAlgorithmException {
		this(realm, nonce, null); //construct the challenge with no opaque data
	}

	/**
	 * Constructs a digest authentication challenge with the MD5 algorithm and no opaque data. Defaults to authentication quality of protection.
	 * @param realm The realm for which authentication is requested.
	 * @param nonce The un-hashed server-specific data unique for each challenge.
	 * @param stale Whether the previous request from the client was rejected because the nonce value was stale.
	 * @throws NoSuchAlgorithmException if the given algorithm is not recognized.
	 * @throws NullPointerException if the realm or the nonce is <code>null</code>.
	 */
	public DigestAuthenticateChallenge(final String realm, final String nonce, final boolean stale) throws NoSuchAlgorithmException {
		this(realm, nonce, null, stale); //construct the challenge with no opaque data
	}

	/**
	 * Constructs a digest authentication challenge with the MD5 algorithm. Defaults to authentication quality of protection.
	 * @param realm The realm for which authentication is requested.
	 * @param nonce The un-hashed server-specific data unique for each challenge.
	 * @param opaque The opaque challenge data, or <code>null</code> for no opaque data.
	 * @throws NoSuchAlgorithmException if the given algorithm is not recognized.
	 * @throws NullPointerException if the realm or the nonce is <code>null</code>.
	 */
	public DigestAuthenticateChallenge(final String realm, final String nonce, final String opaque) throws NoSuchAlgorithmException {
		this(realm, nonce, opaque, MD5.getName()); //construct the challenge with the MD5 algorithm
	}

	/**
	 * Constructs a digest authentication challenge with the MD5 algorithm. Defaults to authentication quality of protection.
	 * @param realm The realm for which authentication is requested.
	 * @param nonce The un-hashed server-specific data unique for each challenge.
	 * @param opaque The opaque challenge data, or <code>null</code> for no opaque data.
	 * @param stale Whether the previous request from the client was rejected because the nonce value was stale.
	 * @throws NoSuchAlgorithmException if the given algorithm is not recognized.
	 * @throws NullPointerException if the realm or the nonce is <code>null</code>.
	 */
	public DigestAuthenticateChallenge(final String realm, final String nonce, final String opaque, final boolean stale) throws NoSuchAlgorithmException {
		this(realm, nonce, opaque, stale, MD5.getName()); //construct the challenge with the MD5 algorithm
	}

	/**
	 * Constructs a digest authentication challenge. Defaults to authentication quality of protection.
	 * @param realm The realm for which authentication is requested.
	 * @param nonce The un-hashed server-specific data unique for each challenge.
	 * @param opaque The opaque challenge data, or <code>null</code> for no opaque data.
	 * @param algorithm The standard name of the digest algorithm.
	 * @throws NoSuchAlgorithmException if the given algorithm is not recognized.
	 * @throws NullPointerException if the realm or the nonce is <code>null</code>.
	 */
	public DigestAuthenticateChallenge(final String realm, final String nonce, final String opaque, final String algorithm) throws NoSuchAlgorithmException {
		this(realm, nonce, opaque, false, algorithm); //construct a non-stale challenge
	}

	/**
	 * Constructs a digest authentication challenge. Defaults to authentication quality of protection.
	 * @param realm The realm for which authentication is requested.
	 * @param nonce The un-hashed server-specific data unique for each challenge.
	 * @param opaque The opaque challenge data, or <code>null</code> for no opaque data.
	 * @param stale Whether the previous request from the client was rejected because the nonce value was stale.
	 * @param algorithm The standard name of the digest algorithm.
	 * @throws NoSuchAlgorithmException if the given algorithm is not recognized.
	 * @throws NullPointerException if the realm or the nonce is <code>null</code>.
	 */
	public DigestAuthenticateChallenge(final String realm, final String nonce, final String opaque, final boolean stale, final String algorithm)
			throws NoSuchAlgorithmException {
		super(AuthenticationScheme.DIGEST, realm); //construct the parent class
		this.nonce = requireNonNull(nonce, "Nonce must be provided");
		this.opaque = opaque;
		this.stale = stale;
		messageDigest = MessageDigest.getInstance(algorithm); //construct the message digest
	}

}
