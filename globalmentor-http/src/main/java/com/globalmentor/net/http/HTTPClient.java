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

import java.net.PasswordAuthentication;

import com.globalmentor.net.*;
import com.globalmentor.util.*;

/**
 * Represents the identify of a group of related HTTP connections. Keeps a cache of authentication information for visited domains and realms.
 * @author Garret Wilson
 */
public class HTTPClient extends AbstractClient {

	/**
	 * The map of connections we have available for certain hosts. A connection will be released if memory requires and it is not being used.
	 */
	//TODO create a daemon thread that will release old, unused connections
	//TODO del if not needed	private final ReadWriteLockMap<Host, HTTPClientTCPConnection> hostConnectionMap=new DecoratorReadWriteLockMap<Host, HTTPClientTCPConnection>(new PurgeOnWriteSoftValueHashMap<Host, HTTPClientTCPConnection>());

	/** The default instance of the HTTP client. */
	private static HTTPClient instance = null;

	/** @return The default instance of this class, or <code>null</code> if there is no default instance. */
	protected AbstractClient getDefaultInstance() {
		return instance;
	}

	/** @return The default instance of the HTTP client. */
	public static HTTPClient getInstance() { //TODO fix race condition
		if(instance == null) { //if we have not yet created an instance
			instance = new HTTPClient(); //create a new client			
		}
		return instance; //return the shared instance of the client
	}

	/** Default constructor with no authenticator. */
	public HTTPClient() {
		this(null); //construct the class with no authenticator
	}

	/**
	 * Authenticator constructor.
	 * @param authenticator The authenticator to use for this client, or <code>null</code> if the default authenticator should be used if available.
	 */
	public HTTPClient(final Authenticable authenticator) {
		super(authenticator); //construct the parent class
	}

	/**
	 * Creates an unsecure connection to a host. Ultimately delegates to {@link #createConnection(Host, PasswordAuthentication, boolean)}.
	 * @param host The host to which to connect.
	 */
	public final HTTPClientTCPConnection createConnection(final Host host) {
		return createConnection(host, false); //create and return a connection that is not secure
	}

	/**
	 * Creates an unsecure connection to a host, using connection-specific password authentication. Ultimately delegates to
	 * {@link #createConnection(Host, PasswordAuthentication, boolean)}.
	 * @param host The host to which to connect.
	 * @param passwordAuthentication The connection-specific password authentication, or <code>null</code> if there should be no connection-specific password
	 *          authentication.
	 */
	public final HTTPClientTCPConnection createConnection(final Host host, final PasswordAuthentication passwordAuthentication) {
		return createConnection(host, passwordAuthentication, false); //create and return a connection that is not secure
	}

	/**
	 * Creates a connection to a host that is optionally secure. Ultimately delegates to {@link #createConnection(Host, PasswordAuthentication, boolean)}.
	 * @param host The host to which to connect.
	 * @param secure Whether the connection should be secure.
	 */
	public final HTTPClientTCPConnection createConnection(final Host host, final boolean secure) {
		return createConnection(host, null, secure); //create and return an optionally secure connection with no connection-specific authentication
	}

	/**
	 * Creates a connection to a host that is optionally secure, using connection-specific password authentication.
	 * @param host The host to which to connect.
	 * @param passwordAuthentication The connection-specific password authentication, or <code>null</code> if there should be no connection-specific password
	 *          authentication.
	 * @param secure Whether the connection should be secure.
	 */
	public HTTPClientTCPConnection createConnection(final Host host, final PasswordAuthentication passwordAuthentication, final boolean secure) {
		return new HTTPClientTCPConnection(this, host, passwordAuthentication, secure); //return a new connection to the given host
	}

}
