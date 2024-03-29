/*
 * Copyright © 1996-2012 GlobalMentor, Inc. <https://www.globalmentor.com/>
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

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.*;

import com.globalmentor.collections.*;
import com.globalmentor.io.*;
import com.globalmentor.java.Bytes;
import com.globalmentor.model.AbstractHashObject;
import com.globalmentor.net.*;

import static com.globalmentor.net.URIs.*;
import static com.globalmentor.net.HTTP.*;

import static java.util.Objects.*;

/**
 * A client's view of an HTTP resource on the server. For many error conditions, a subclass of {@link HTTPException} will be thrown. This class is not thread
 * safe.
 * @author Garret Wilson
 * @see HTTPException
 */
public class HTTPResource extends DefaultResource //TODO improve by having a persistence timeout, by checking for a server's close request, and by detecting closed connections and preventing socket exceptions
{

	/** The client used to create a connection to this resource. */
	private final HTTPClient client;

	/** @return The client used to create a connection to this resource. */
	protected HTTPClient getClient() {
		return client;
	}

	/** The preset password authentication, or <code>null</code> if this resource specifies no preset password authentication. */
	private final PasswordAuthentication passwordAuthentication;

	/** @return The preset password authentication, or <code>null</code> if this connection specifies no preset password authentication. */
	protected PasswordAuthentication getPasswordAuthentication() {
		return passwordAuthentication;
	}

	/** Whether cached properties are to be returned; the default is <code>true</code>. */
	private boolean cached = true;

	/** @return Whether cached properties are to be returned; the default is <code>true</code>. */
	public boolean isCached() {
		return cached;
	}

	/**
	 * Sets whether cached properties should be used. If caching is turned off, values are still cached in case other resources using this client want to use the
	 * cached values.
	 * @param cached Whether cached properties are to be returned.
	 */
	public void setCached(final boolean cached) {
		if(this.cached != cached) { //if the caching state is changing
			this.cached = cached; //update the cache state
		}
	}

	/** The lock controlling access to the caches. */
	protected static final ReadWriteLock cacheLock = new ReentrantReadWriteLock();

	/** The soft value map containing cached exists information. This map is made thread-safe through the use of {@link #cacheLock}. */
	protected static final Map<CacheKey, CachedExists> cachedExistsMap = new DecoratorReadWriteLockMap<CacheKey, CachedExists>(
			new PurgeOnWriteSoftValueHashMap<CacheKey, CachedExists>(), cacheLock);

	/** Clears all information from the caches. This version clears all cached exists information. */
	protected void clearCache() {
		cachedExistsMap.clear();
	}

	/**
	 * Caches the given exists status for this resource.
	 * @param exists The existence status.
	 */
	protected void cacheExists(final boolean exists) {
		cachedExistsMap.put(new CacheKey(getClient(), getURI()), new CachedExists(exists)); //cache the information
	}

	/**
	 * Removes all cached information for a given resource. This version calls uncaches exists information.
	 * @param resourceURI The URI of the resource for which cached information should be removed.
	 */
	protected void uncacheInfo(final URI resourceURI) {
		cachedExistsMap.remove(new CacheKey(getClient(), resourceURI)); //uncache the exists status for the given resource
	}

	/**
	 * Removes all cached information for this resource. This is a convenience method that delegates to {@link #uncacheInfo(URI)}.
	 */
	protected void uncacheInfo() {
		uncacheInfo(getURI()); //uncache the information for this resource
	}

	/**
	 * Constructs an HTTP resource at a particular URI using the default client.
	 * @param referenceURI The URI of the HTTP resource this object represents.
	 * @throws IllegalArgumentException if the given reference URI is not absolute, the reference URI has no host, or the scheme is not an HTTP scheme.
	 * @throws NullPointerException if the given reference URI is <code>null</code>.
	 */
	public HTTPResource(final URI referenceURI) throws IllegalArgumentException, NullPointerException {
		this(referenceURI, (PasswordAuthentication)null); //construct the resource with no preset password authentication
	}

	/**
	 * Constructs an HTTP resource at a particular URI using specified password authentication.
	 * @param referenceURI The URI of the HTTP resource this object represents.
	 * @param passwordAuthentication The password authentication to use in connecting to this resource, or <code>null</code> if there should be no preset password
	 *          authentication.
	 * @throws IllegalArgumentException if the given reference URI is not absolute, the reference URI has no host, or the scheme is not an HTTP scheme.
	 * @throws NullPointerException if the given reference URI is <code>null</code>.
	 */
	public HTTPResource(final URI referenceURI, final PasswordAuthentication passwordAuthentication) throws IllegalArgumentException, NullPointerException {
		this(referenceURI, HTTPClient.getInstance(), passwordAuthentication); //construct the resource with the default client		
	}

	/**
	 * Constructs an HTTP resource at a particular URI using a particular client.
	 * @param referenceURI The URI of the HTTP resource this object represents.
	 * @param client The client used to create a connection to this resource.
	 * @throws IllegalArgumentException if the given reference URI is not absolute, the reference URI has no host, or the scheme is not an HTTP scheme.
	 * @throws NullPointerException if the given reference URI and/or client is <code>null</code>.
	 */
	public HTTPResource(final URI referenceURI, final HTTPClient client) throws IllegalArgumentException, NullPointerException {
		this(referenceURI, client, null); //construct the class with no preset password authentication
	}

	/**
	 * Constructs an HTTP resource at a particular URI using a particular client and specified password authentication.
	 * @param referenceURI The URI of the HTTP resource this object represents.
	 * @param client The client used to create a connection to this resource.
	 * @param passwordAuthentication The password authentication to use in connecting to this resource, or <code>null</code> if there should be no preset password
	 *          authentication.
	 * @throws IllegalArgumentException if the given reference URI is not absolute, the reference URI has no host, or the scheme is not an HTTP scheme.
	 * @throws NullPointerException if the given reference URI and/or client is <code>null</code>.
	 */
	public HTTPResource(final URI referenceURI, final HTTPClient client, final PasswordAuthentication passwordAuthentication) throws IllegalArgumentException,
			NullPointerException {
		super(referenceURI); //construct the parent class
		if(!referenceURI.isAbsolute()) { //if the URI is not absolute
			throw new IllegalArgumentException("URI " + referenceURI + " is not absolute.");
		}
		if(referenceURI.getHost() == null) { //if the URI has no host
			throw new IllegalArgumentException("URI " + referenceURI + " has no host specified.");
		}
		if(!isHTTPURI(referenceURI)) { //if this isn't a HTTP or HTTPS resource
			throw new IllegalArgumentException("Invalid HTTP scheme " + referenceURI.getScheme());
		}
		this.client = requireNonNull(client, "Client cannot be null."); //save the client
		this.passwordAuthentication = passwordAuthentication; //save the password authentication
	}

	/**
	 * Deletes the resource using the {@value HTTP#DELETE_METHOD} method.
	 * @throws IOException if there was an error invoking the method.
	 */
	public void delete() throws IOException {
		final HTTPRequest request = new DefaultHTTPRequest(DELETE_METHOD, getURI()); //create a DELETE request
		final HTTPClientTCPConnection connection = getConnection(); //get a connection to the server
		final HTTPResponse response = connection.sendRequest(request, Bytes.NO_BYTES); //send the request and get the response
		connection.readResponseBody(request, response); //ignore the response body
		response.checkStatus(); //check the status of the response, throwing an exception if this is an error
		if(isCached()) { //if we're caching this resource
			if(isCollectionURI(getURI())) { //if this is a collection, we may have information cached for child resources
				clearCache(); //dump all our cache; this is a drastic measure, but we can't have cached information for children that no longer exist TODO improve to be more selective
			} else { //for non-collection resources
				cacheLock.writeLock().lock(); //lock the cache for writing
				try {
					uncacheInfo(); //uncache our info for this resource
					cacheExists(false); //indicate that the resource no longer exists
				} finally {
					cacheLock.writeLock().unlock(); //always release the write lock
				}
			}
		}
	}

	/**
	 * Determines if a resource exists. This implementation checks for existence by invoking the {@value HTTP#HEAD_METHOD} method if values are not cached.
	 * @return <code>true</code> if the resource is present on the server.
	 * @throws IOException if there was an error invoking a method.
	 */
	public boolean exists() throws IOException {
		if(isCached()) { //if we're caching values
			final CacheKey cacheKey = new CacheKey(getClient(), getURI()); //create a new cache key
			CachedExists cachedExists = cachedExistsMap.get(cacheKey); //get cached exists state from the map
			if(cachedExists != null && !cachedExists.isStale()) { //there is cached exists information that isn't stale
				return cachedExists.exists(); //return the new exists information
			}
		}
		final boolean exists = getExists(); //determine if the resource exists
		if(isCached()) { //if we are caching information
			cachedExistsMap.put(new CacheKey(getClient(), getURI()), new CachedExists(exists)); //cache the exists status
		}
		return exists; //return the exists status
	}

	/**
	 * Determines the exists state for this resource The value is not retrieved from the cache. This version invokes the {@value HTTP#HEAD_METHOD} method to
	 * determine existence.
	 * @return The latest determined existence status.
	 * @throws IOException if there was an error invoking a method.
	 */
	protected boolean getExists() throws IOException {
		final HTTPRequest request = new DefaultHTTPRequest(HEAD_METHOD, getURI()); //create a HEAD request
		final HTTPClientTCPConnection connection = getConnection(); //get a connection to the server
		final HTTPResponse response = connection.sendRequest(request, Bytes.NO_BYTES); //get the response
		connection.readResponseBody(request, response); //ignore the response body
		if(response.getStatusCode() == SC_NOT_FOUND //404 Not Found
				|| response.getStatusCode() == SC_GONE) { //410 Gone
			return false; //show that the resource is not there
		}
		response.checkStatus(); //check the status of the response, throwing an exception if this is an error
		return true; //if no exceptions were thrown, assume the resource exists
	}

	/**
	 * Retrieves the contents of a resource using the {@value HTTP#GET_METHOD} method.
	 * @return An input stream to the server.
	 * @throws IOException if there was an error invoking the method.
	 */
	public InputStream getInputStream() throws IOException {
		Boolean exists = null; //we'll see if we can determine existence
		final HTTPRequest request = new DefaultHTTPRequest(GET_METHOD, getURI()); //create a GET request
		final HTTPClientTCPConnection connection = getConnection(); //get a connection to the server
		try {
			final HTTPResponse response = connection.sendRequest(request, Bytes.NO_BYTES); //get the response
			try {
				response.checkStatus(); //check the status of the response, throwing an exception if this is an error
				exists = Boolean.TRUE; //if GET succeeds, the resource exists
			} catch(final HTTPNotFoundException notFoundException) { //404 Not Found
				exists = Boolean.FALSE; //show that the resource is not there
				connection.readResponseBody(request, response); //skip the response body
				throw notFoundException; //rethrow the exception
			} catch(final HTTPGoneException goneException) { //410 Gone
				exists = Boolean.FALSE; //show that the resource is permanently not there
				connection.readResponseBody(request, response); //skip the response body
				throw goneException; //rethrow the exception
			}
			return connection.getResponseBodyInputStream(request, response); //get an input stream to the response body
		} finally {
			if(isCached() && exists != null) { //if information is being cached and we know the latest existence state
				cacheExists(exists.booleanValue()); //update the exists status
			}
		}
	}

	/**
	 * Retrieves the contents of a resource using the {@value HTTP#GET_METHOD} method. The cached existence property is updated if information is being cached.
	 * @return The content received from the server.
	 * @throws IOException if there was an error invoking the method.
	 * @see #cacheExists(boolean)
	 */
	public byte[] get() throws IOException {
		Boolean exists = null; //we'll see if we can determine existence
		final HTTPRequest request = new DefaultHTTPRequest(GET_METHOD, getURI()); //create a GET request
		final HTTPClientTCPConnection connection = getConnection(); //get a connection to the server
		try {
			final HTTPResponse response = connection.sendRequest(request, Bytes.NO_BYTES); //get the response
			if(response.getStatusCode() == SC_NOT_FOUND //404 Not Found
					|| response.getStatusCode() == SC_GONE) { //410 Gone
				exists = Boolean.FALSE; //show that the resource is not there
			}
			response.checkStatus(); //check the status of the response, throwing an exception if this is an error
			exists = Boolean.TRUE; //if GET succeeds, the resource exists
			return connection.readResponseBody(request, response); //read and return the response body
		} finally {
			if(isCached() && exists != null) { //if information is being cached and we know the latest existence state
				cacheExists(exists.booleanValue()); //update the exists status
			}
		}
	}

	/**
	 * Accesses a resource using the {@value HTTP#HEAD_METHOD} method. The cached existence property is updated.
	 * @throws IOException if there was an error invoking the method.
	 * @see #cacheExists(boolean)
	 */
	public void head() throws IOException {
		Boolean exists = null; //we'll see if we can determine existence
		final HTTPRequest request = new DefaultHTTPRequest(HEAD_METHOD, getURI()); //create a HEAD request
		final HTTPClientTCPConnection connection = getConnection(); //get a connection to the server
		try {
			final HTTPResponse response = connection.sendRequest(request, Bytes.NO_BYTES); //get the response
			connection.readResponseBody(request, response); //ignore the response body
			if(response.getStatusCode() == SC_NOT_FOUND //404 Not Found
					|| response.getStatusCode() == SC_GONE) { //410 Gone
				exists = Boolean.FALSE; //show that the resource is not there
			}
			response.checkStatus(); //check the status of the response, throwing an exception if this is an error
			exists = Boolean.TRUE; //if no exceptions were thrown, assume the resource exists
		} finally {
			if(isCached() && exists != null) { //if information is being cached and we know the latest existence state
				cacheExists(exists.booleanValue()); //update the exists status
			}
		}
	}

	/**
	 * Stores the contents of a resource using the {@value HTTP#PUT_METHOD} method.
	 * @param content The bytes to store at the resource location.
	 * @throws IOException if there was an error invoking the method.
	 */
	public void put(final byte[] content) throws IOException {
		final HTTPRequest request = new DefaultHTTPRequest(PUT_METHOD, getURI()); //create a PUT request
		final HTTPClientTCPConnection connection = getConnection(); //get a connection to the server
		final HTTPResponse response = connection.sendRequest(request, content); //get the response
		connection.readResponseBody(request, response); //ignore the response body
		response.checkStatus(); //check the status of the response, throwing an exception if this is an error
		if(isCached()) { //if we're caching this resource
			cacheLock.writeLock().lock(); //lock the cache for writing
			try {
				uncacheInfo(); //uncache our info for this resource; the new content could change properties such as content-length
				cacheExists(true); //we just put content with no errors, so it should now exist
			} finally {
				cacheLock.writeLock().unlock(); //always release the write lock
			}
		}
	}

	/**
	 * Retrieves an output stream to a resource using the {@value HTTP#PUT_METHOD} method.
	 * @return An output stream to the resource.
	 * @throws IOException if there was an error invoking the method.
	 */
	public OutputStream getOutputStream() throws IOException {
		final HTTPRequest request = new DefaultHTTPRequest(PUT_METHOD, getURI()); //create a PUT request
		final HTTPClientTCPConnection connection = getConnection(); //get a connection to the server
		final OutputStream outputStream = connection.writeRequest(request); //write the request to the server
		if(isCached()) { //if we're caching this resource
			cacheLock.writeLock().lock(); //lock the cache for writing
			try {
				uncacheInfo(); //uncache our info for this resource; the new content could change properties such as content-length
				cacheExists(true); //just writing the PUT request will probably create something, so we assume the resource exists, even if we later don't succeed in writing all the bytes
			} finally {
				cacheLock.writeLock().unlock(); //always release the write lock
			}
		}
		return new ReadResponseOutputStreamDecorator(connection, request, outputStream); //return a version of the output stream that will read the response when finished
	}

	/**
	 * Reads an object from the resource using HTTP GET with the given I/O support.
	 * @param <T> The type of the I/O support.
	 * @param io The I/O support for reading the object.
	 * @return The object read from the resource.
	 * @throws IOException if there is an error reading the data.
	 */
	public <T> T get(final IO<T> io) throws IOException {
		try (final InputStream inputStream = getInputStream()) { //get an input stream to the resource
			return io.read(inputStream, getURI()); //read the object, using the resource reference URI as the base URI
		}
	}

	/**
	 * Writes an object to the resource using HTTP PUT with the given I/O support.
	 * @param <T> The type of the I/O support.
	 * @param object The object to write to the resource.
	 * @param io The I/O support for writing the object.
	 * @throws IOException if there is an error writing the data.
	 */
	public <T> void put(final T object, final IO<T> io) throws IOException {
		try (final OutputStream outputStream = getOutputStream()) {//get an output stream to the resource
			io.write(outputStream, getURI(), object); //write the object, using the resource reference URI as the base URI
		}
	}

	/** The lazily-created connection. */
	private HTTPClientTCPConnection connection = null;

	/**
	 * Gets a connection to the server.
	 * @return A connection to the server.
	 */
	protected HTTPClientTCPConnection getConnection() {
		if(connection == null) { //if no connection has been created
			final URI referenceURI = getURI(); //get the reference URI
			final boolean secure = HTTP.HTTPS_URI_SCHEME.equals(referenceURI.getScheme()); //see if this connection should be secure
			connection = getClient().createConnection(getHost(getURI()), getPasswordAuthentication(), secure); //get a connection to the URI
		}
		return connection;
	}

	/**
	 * Sends a request to the server.
	 * @param request The request to send to the server.
	 * @return The response from the server.
	 * @throws IOException if there was an error sending the request or receiving the response.
	 */
	/*TODO del if not needed
		protected HTTPResponse sendRequest(final HTTPRequest request) throws IOException {	//TODO add connection persistence
			final URI referenceURI=getURI();	//get the reference URI
			final boolean secure=HTTP.HTTPS_SCHEME.equals(referenceURI.getScheme());	//see if this connection should be secure
			final HTTPClientTCPConnection connection=getClient().createConnection(getHost(getURI()), getPasswordAuthentication(), secure);	//get a connection to the URI
			try
			{
				return connection.sendRequest(request);	//send the request and return the response
			}
			finally
			{
				connection.disconnect();	//always close the connection
			}
		}
	*/

	/**
	 * Sends a request to the server using a custom authenticator.
	 * @param request The request to send to the server.
	 * @return The response from the server.
	 * @throws IOException if there was an error sending the request or receiving the response.
	 */
	/*TODO bring back if needed
		protected HTTPResponse sendRequest(final HTTPRequest request, final Authenticable authenticator) throws IOException {	//TODO add connection peristence
			final URI referenceURI=getURI();	//get the reference URI
			final boolean secure=HTTP.HTTPS_SCHEME.equals(referenceURI.getScheme());	//see if this connection should be secure
			final HTTPClientTCPConnection connection=getClient().createConnection(getHost(getURI()), getPasswordAuthentication(), secure);	//get a connection to the URI
			try
			{
				return connection.sendRequest(request);	//send the request and return the response
			}
			finally
			{
				connection.disconnect();	//always close the connection
			}
		}
	*/

	/**
	 * Creates an output stream that, after being closed, reads the HTTP response and throws an error if appropriate.
	 * @author Garret Wilson
	 */
	protected class ReadResponseOutputStreamDecorator extends OutputStreamDecorator<OutputStream> {

		private final HTTPClientTCPConnection connection;
		private final HTTPRequest request;

		/**
		 * Decorates the given output stream.
		 * @param connection The connection to the HTTP server.
		 * @param request The request that resulted in the creation of the output stream.
		 * @param outputStream The output stream to decorate.
		 * @throws NullPointerException if the given connection, request, and/or stream is <code>null</code>.
		 */
		public ReadResponseOutputStreamDecorator(final HTTPClientTCPConnection connection, final HTTPRequest request, final OutputStream outputStream) {
			super(outputStream); //construct the parent class
			this.connection = requireNonNull(connection, "Connection cannot be null.");
			this.request = requireNonNull(request, "Request cannot be null.");
		}

		/**
		 * Called after the stream is successfully closed. This version reads the HTTP response and throws an error if the response is an error condition.
		 * @throws IOException if an I/O error occurs.
		 */
		protected void afterClose() throws IOException {
			super.afterClose();
			final HTTPResponse response = connection.readResponse(request); //read the response
			connection.readResponseBody(request, response); //ignore the response body
			response.checkStatus(); //check the status of the response, throwing an exception if this is an error
		}

	}

	/**
	 * A key for cached resource information.
	 * @author Garret Wilson
	 */
	protected static class CacheKey extends AbstractHashObject {

		/**
		 * HTTP client and resource URI constructor.
		 * @param httpClient The HTTP client.
		 * @param resourceURI The resource URI.
		 * @throws NullPointerException if the given HTTP client and/or resource URI is <code>null</code>.
		 */
		public CacheKey(final HTTPClient httpClient, final URI resourceURI) {
			super(requireNonNull(httpClient, "HTTP client cannot be null."), requireNonNull(resourceURI, "Resource URI cannot be null."));
		}
	}

	/**
	 * Abstract class for information stored in an HTTP resource cache.
	 * @author Garret Wilson
	 */
	protected static class AbstractCachedInfo {

		/** The length of time, in milliseconds, to keep cached information. */
		private static final long CACHE_EXPIRATION_MILLISECONDS = 10000;

		/** The time the cached information was created. */
		private final long createdTime;

		/** @return The time the cached information was created. */
		public long getCreatedTime() {
			return createdTime;
		}

		/** @return <code>true</code> if the cached information has expired. */
		public boolean isStale() {
			return System.currentTimeMillis() - getCreatedTime() > CACHE_EXPIRATION_MILLISECONDS;
		}

		/** Default constructor. */
		public AbstractCachedInfo() {
			createdTime = System.currentTimeMillis(); //record the time this information was created
		}
	}

	/**
	 * Existence information stored in an HTTP resource cache.
	 * @author Garret Wilson
	 */
	protected static class CachedExists extends AbstractCachedInfo {

		/** The cached record of whether the resource exists. */
		private final boolean exists;

		/** @return The cached record of whether the resource exists. */
		public boolean exists() {
			return exists;
		}

		/**
		 * Constructor.
		 * @param exists Whether the resource is currently known to exist.
		 */
		public CachedExists(final boolean exists) {
			this.exists = exists; //save the cached existence state
		}
	}

}
