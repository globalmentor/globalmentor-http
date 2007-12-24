package com.garretwilson.net.http;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.*;

import com.garretwilson.io.*;
import static com.garretwilson.lang.ObjectUtilities.*;
import com.garretwilson.net.*;
import static com.garretwilson.net.URIConstants.*;
import static com.garretwilson.net.URIs.*;
import com.garretwilson.util.*;

import static com.garretwilson.net.http.HTTPConstants.*;


/**A client's view of an HTTP resource on the server.
For many error conditions, a subclass of {@link HTTPException} will be thrown.
This class is not thread safe.
@author Garret Wilson
@see HTTPException
*/
public class HTTPResource extends DefaultResource
{

	/**The client used to create a connection to this resource.*/
	private final HTTPClient client;

		/**@return The client used to create a connection to this resource.*/
		protected HTTPClient getClient() {return client;}

	/**The preset password authentication, or <code>null</code> if this resource specifies no preset password authentication.*/
	private final PasswordAuthentication passwordAuthentication;
	
		/**@return The preset password authentication, or <code>null</code> if this connection specifies no preset password authentication.*/
		protected PasswordAuthentication getPasswordAuthentication() {return passwordAuthentication;}

	/**Whether cached properties are to be returned; the default is <code>true</code>.*/
	private boolean cached=true;

		/**@return Whether cached properties are to be returned; the default is <code>true</code>.*/
		public boolean isCached() {return cached;}

		/**Sets whether cached properties should be used.
		If caching is turned off, values are still cached in case other resources using this client want to use the cached values.
		@param cached Whether cached properties are to be returned.
		*/
		public void setCached(final boolean cached)
		{
			if(this.cached!=cached)	//if the caching state is changing
			{
				this.cached=cached;	//update the cache state
			}
		}

	/**The lock controlling access to the caches.*/
	protected final static ReadWriteLock cacheLock=new ReentrantReadWriteLock();

	/**The soft value map containing cached exists information. This map is made thread-safe through the use of {@link #cacheLock}.*/
	protected final static Map<CacheKey, CachedExists> cachedExistsMap=new DecoratorReadWriteLockMap<CacheKey, CachedExists>(new PurgeOnWriteSoftValueHashMap<CacheKey, CachedExists>(), cacheLock);

	/**Caches the given exists status for this resource.
	@param exists The existence status.
	*/
	protected void cacheExists(final boolean exists)
	{
		cachedExistsMap.put(new CacheKey(getClient(), getURI()), new CachedExists(exists));	//cache the information
	}

	/**Removes all cached information for a given resource.
	This version calls uncaches exists information.
	@param resourceURI The URI of the resource for which cached information should be removed.
	*/
	protected void uncacheInfo(final URI resourceURI)
	{
		cachedExistsMap.remove(new CacheKey(getClient(), resourceURI));	//uncache the exists status for the given resource
	}

	/**Removes all cached information for this resource.
	This is a convenience method that delegates to {@link #uncacheInfo(URI)}.
	*/
	protected void uncacheInfo()
	{
		uncacheInfo(getURI());	//uncache the information for this resource
	}
		
	/**Constructs an HTTP resource at a particular URI using the default client.
	@param referenceURI The URI of the HTTP resource this object represents.
	@exception IllegalArgumentException if the given reference URI is not absolute, the reference URI has no host, or the scheme is not an HTTP scheme.
	@exception NullPointerException if the given reference URI is <code>null</code>.
	*/
	public HTTPResource(final URI referenceURI) throws IllegalArgumentException, NullPointerException
	{
		this(referenceURI, (PasswordAuthentication)null);	//construct the resource with no preset password authentication
	}

	/**Constructs an HTTP resource at a particular URI using specified password authentication.
	@param referenceURI The URI of the HTTP resource this object represents.
	@param passwordAuthentication The password authentication to use in connecting to this resource, or <code>null</code> if there should be no preset password authentication.
	@exception IllegalArgumentException if the given reference URI is not absolute, the reference URI has no host, or the scheme is not an HTTP scheme.
	@exception NullPointerException if the given reference URI is <code>null</code>.
	*/
	public HTTPResource(final URI referenceURI, final PasswordAuthentication passwordAuthentication) throws IllegalArgumentException, NullPointerException
	{
		this(referenceURI, HTTPClient.getInstance(), passwordAuthentication);	//construct the resource with the default client		
	}

	/**Constructs an HTTP resource at a particular URI using a particular client.
	@param referenceURI The URI of the HTTP resource this object represents.
	@param client The client used to create a connection to this resource.
	@exception IllegalArgumentException if the given reference URI is not absolute, the reference URI has no host, or the scheme is not an HTTP scheme.
	@exception NullPointerException if the given reference URI and/or client is <code>null</code>.
	*/
	public HTTPResource(final URI referenceURI, final HTTPClient client) throws IllegalArgumentException, NullPointerException
	{
		this(referenceURI, client, null);	//construct the class with no preset password authentication
	}

	/**Constructs an HTTP resource at a particular URI using a particular client and specified password authentication.
	@param referenceURI The URI of the HTTP resource this object represents.
	@param client The client used to create a connection to this resource.
	@param passwordAuthentication The password authentication to use in connecting to this resource, or <code>null</code> if there should be no preset password authentication.
	@exception IllegalArgumentException if the given reference URI is not absolute, the reference URI has no host, or the scheme is not an HTTP scheme.
	@exception NullPointerException if the given reference URI and/or client is <code>null</code>.
	*/
	public HTTPResource(final URI referenceURI, final HTTPClient client, final PasswordAuthentication passwordAuthentication) throws IllegalArgumentException, NullPointerException
	{
		super(referenceURI);	//construct the parent class
		if(!referenceURI.isAbsolute())	//if the URI is not absolute
		{
			throw new IllegalArgumentException("URI "+referenceURI+" is not absolute.");
		}
		if(referenceURI.getHost()==null)	//if the URI has no host
		{
			throw new IllegalArgumentException("URI "+referenceURI+" has no host specified.");
		}
		final String scheme=referenceURI.getScheme();	//get the URI scheme
		if(!HTTP_SCHEME.equals(scheme) && !HTTPS_SCHEME.equals(scheme))	//if this isn't a HTTP or HTTPS resource
		{
			throw new IllegalArgumentException("Invalid HTTP scheme "+scheme);
		}
		this.client=checkInstance(client, "Client cannot be null.");	//save the client
		this.passwordAuthentication=passwordAuthentication;	//save the password authentication
	}

	/**Deletes the resource using the DELETE method.
	@exception IOException if there was an error invoking the method.
	*/
	public void delete() throws IOException
	{
		final HTTPRequest request=new DefaultHTTPRequest(DELETE_METHOD, getURI());	//create a DELETE request
		final HTTPResponse response=sendRequest(request);	//get the response
		if(isCached())	//if we're caching this resource
		{
			cacheExists(false);	//indicate that the resource no longer exists
		}
	}

	/**Determines if a resource exists.
	This implementation checks for existence by invoking the {@value HTTPConstants#HEAD_METHOD} method if values are not cached.
	@return <code>true</code> if the resource is present on the server.
	@exception IOException if there was an error invoking a method.
	*/
	public boolean exists() throws IOException
	{
		if(isCached())	//if we're caching values
		{
			final CacheKey cacheKey=new CacheKey(getClient(), getURI());	//create a new cache key
			CachedExists cachedExists=cachedExistsMap.get(cacheKey);	//get cached exists state from the map
			if(cachedExists!=null && !cachedExists.isStale())	//there is cached exists information that isn't stale
			{
				return cachedExists.exists();	//return the new exists information
			}
		}
		final boolean exists=getExists();	//determine if the resource exists
		if(isCached())	//if we are caching information
		{
			cachedExistsMap.put(new CacheKey(getClient(), getURI()), new CachedExists(exists));	//cache the exists status
		}
		return exists;	//return the exists status
	}

	/**Determines the exists state for this resource
	The value is not retrieved from the cache.
	This version invokes the {@value HTTPConstants#HEAD_METHOD} method to determine existence.
	@return The latest determined existence status.
	@exception IOException if there was an error invoking a method.
	*/
	protected boolean getExists() throws IOException
	{
		try
		{
			final HTTPRequest request=new DefaultHTTPRequest(HEAD_METHOD, getURI());	//create a HEAD request
			final HTTPResponse response=sendRequest(request);	//get the response
			return true;	//if no exceptions were thrown, assume the resource exists
		}
		catch(final HTTPNotFoundException notFoundException)	//404 Not Found
		{
			return false;	//show that the resource is not there
		}
		catch(final HTTPGoneException goneException)	//410 Gone
		{
			return false;	//show that the resource is permanently not there
		}
	}

	/**Retrieves the contents of a resource using the GET method.
	@return An input stream to the server.
	@exception IOException if there was an error invoking the method.
	*/
	public InputStream getInputStream() throws IOException
	{
		return new ByteArrayInputStream(get());	//return an input stream to the result of the GET method
	}

	/**Retrieves the contents of a resource using the {@value HTTPConstants#GET_METHOD} method.
	The cached existence property is updated if information is being cached.
	@return The content received from the server.
	@exception IOException if there was an error invoking the method.
	@see #cachedExists
	*/
	public byte[] get() throws IOException
	{
		Boolean exists=null;	//we'll see if we can determine existence
		try
		{
			try
			{
				final HTTPRequest request=new DefaultHTTPRequest(GET_METHOD, getURI());	//create a GET request
				final HTTPResponse response=sendRequest(request);	//get the response
				exists=Boolean.TRUE;	//if GET succeeds, the resource exists
				return response.getBody();	//return the bytes received from the server
			}
			catch(final HTTPNotFoundException notFoundException)	//404 Not Found
			{
				exists=Boolean.FALSE;	//show that the resource is not there
				throw notFoundException;	//rethrow the exception
			}
			catch(final HTTPGoneException goneException)	//410 Gone
			{
				exists=Boolean.FALSE;	//show that the resource is permanently not there
				throw goneException;	//rethrow the exception
			}
		}
		finally
		{
			if(isCached() && exists!=null)	//if information is being cached and we know the latest existence state
			{
				if(isCached())	//if we are caching information
				{
					cacheExists(exists.booleanValue());	//update the exists status
				}
			}
		}
	}

	/**Accesses a resource using the HEAD method.
	The cached existence property is updated.
	@exception IOException if there was an error invoking the method.
	@see #cachedExists
	*/
	public void head() throws IOException
	{
		Boolean exists=null;	//we'll see if we can determine existence
		try
		{
			try
			{
				final HTTPRequest request=new DefaultHTTPRequest(HEAD_METHOD, getURI());	//create a HEAD request
				final HTTPResponse response=sendRequest(request);	//get the response
				exists=Boolean.TRUE;	//if no exceptions were thrown, assume the resource exists
			}
			catch(final HTTPNotFoundException notFoundException)	//404 Not Found
			{
				exists=Boolean.FALSE;	//show that the resource is not there
				throw notFoundException;	//rethrow the exception
			}
			catch(final HTTPGoneException goneException)	//410 Gone
			{
				exists=Boolean.FALSE;	//show that the resource is permanently not there
				throw goneException;	//rethrow the exception
			}
		}
		finally
		{
			if(isCached() && exists!=null)	//if information is being cached and we know the latest existence state
			{
				cacheExists(exists.booleanValue());	//update the exists status
			}
		}
	}

	/**Stores the contents of a resource using the PUT method.
	@param content The bytes to store at the resource location. 
	@exception IOException if there was an error invoking the method.
	*/
	public void put(final byte[] content) throws IOException
	{
//TODO del Debug.trace("ready to put bytes:", content.length);
		final HTTPRequest request=new DefaultHTTPRequest(PUT_METHOD, getURI());	//create a PUT request
		request.setBody(content);	//set the content of the request 
		final HTTPResponse response=sendRequest(request);	//get the response
	}
	
	/**Reads an object from the resource using HTTP GET with the given I/O support.
	@param io The I/O support for reading the object.
	@return The object read from the resource.
	@throws IOException if there is an error reading the data.
	*/ 
	public <T> T get(final IO<T> io) throws IOException
	{
		final InputStream inputStream=getInputStream();	//get an input stream to the resource
		try
		{
			return io.read(inputStream, getURI());	//read the object, using the resource reference URI as the base URI
		}
		finally
		{
			inputStream.close();	//always close the input stream
		}
	}
	
	/**Writes an object to the resource using HTTP PUT with the given I/O support.
	@param object The object to write to the resource.
	@param io The I/O support for writing the object.
	@throws IOException if there is an error writing the data.
	*/
	public <T> void put(final T object, final IO<T> io) throws IOException
	{
		final OutputStream outputStream=getOutputStream();//get an output stream to the resource
		try
		{
			io.write(outputStream, getURI(), object);	//write the object, using the resource reference URI as the base URI
		}
		finally
		{
			outputStream.close();	//always close the output stream
		}
	}

	/**Retrieves an output stream which, upon closing, will store the contents of a resource using the PUT method.
	@return An output stream to the resource.
	@exception IOException if there was an error invoking the method.
	*/
	public OutputStream getOutputStream() throws IOException
	{
		return new OutputStreamAdapter();	//create and return a new output stream adapter which will accumulate bytes and send them when closed
	}

	/**Sends a request to the server.
	@param request The request to send to the server.
	@return The response from the server.
	@exception IOException if there was an error sending the request or receiving the response.
	*/
	protected HTTPResponse sendRequest(final HTTPRequest request) throws IOException	//TODO add connection peristence
	{
		final URI referenceURI=getURI();	//get the reference URI
		final boolean secure=HTTPS_SCHEME.equals(referenceURI.getScheme());	//see if this connection should be secure
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

	/**Sends a request to the server using a custom authenticator.
	@param request The request to send to the server.
	@return The response from the server.
	@exception IOException if there was an error sending the request or receiving the response.
	*/
	protected HTTPResponse sendRequest(final HTTPRequest request, final Authenticable authenticator) throws IOException	//TODO add connection peristence
	{
		final URI referenceURI=getURI();	//get the reference URI
		final boolean secure=HTTPS_SCHEME.equals(referenceURI.getScheme());	//see if this connection should be secure
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

	/**Creates an output stream that simply collects bytes until closed,
	 	at which point the data is written to the HTTP resource using the PUT method.
	@author Garret Wilson
	@see HTTPResource#put(byte[])
	*/
	protected class OutputStreamAdapter extends OutputStreamDecorator<ByteArrayOutputStream>
	{
	
		/**Default constructor.*/
		public OutputStreamAdapter()
		{
			super(new ByteArrayOutputStream());	//collect bytes in a decorated byte array output stream
		}
	
		//TODO maybe improve flush() at some point to actually send data to the HTTP Resource

	  /**Called before the stream is closed.
	  This version writes the accumulated data to the HTTP resource, and unconditionally releases the accumulated bytes.
		@exception IOException if an I/O error occurs.
		*/
	  protected void beforeClose() throws IOException 
	  {
			final ByteArrayOutputStream byteArrayOutputStream=getOutputStream();	//get the decorated output stream
			assert byteArrayOutputStream!=null : "Missing decorated stream.";
			final byte[] bytes=byteArrayOutputStream.toByteArray();	//get the collected bytes
			put(bytes);	//put the bytes to the HTTP resource
	  }
	}

	/**A key for cached resource information.
	@author Garret Wilson
	*/
	protected static class CacheKey extends AbstractHashObject
	{
		
		/**HTTP client and resource URI contstructor.
		@param httpClient The HTTP client.
		@param resourceURI The resource URI.
		@exception NullPointerException if the given HTTP client and/or resource URI is <code>null</code>.
		*/
		public CacheKey(final HTTPClient httpClient, final URI resourceURI)
		{
			super(checkInstance(httpClient, "HTTP client cannot be null."), checkInstance(resourceURI, "Resource URI cannot be null."));
		}		
	}

	/**Abstract class for information stored in an HTTP resource cache.
	@author Garret Wilson
	*/
	protected static class AbstractCachedInfo
	{

		/**The length of time, in milliseconds, to keep cached information.*/
		private final static long CACHE_EXPIRATION_MILLISECONDS=10000;

		/**The time the cached information was created.*/
		private final long createdTime;

			/**@return The time the cached information was created.*/
			public long getCreatedTime() {return createdTime;}

		/**@return <code>true</code> if the cached information has expired.*/
		public boolean isStale() {return System.currentTimeMillis()-getCreatedTime()>CACHE_EXPIRATION_MILLISECONDS;}

		/**Default constructor.*/
		public AbstractCachedInfo()
		{
			createdTime=System.currentTimeMillis();	//record the time this information was created
		}
	}

	/**Existence information stored in an HTTP resource cache.
	@author Garret Wilson
	*/
	protected static class CachedExists extends AbstractCachedInfo
	{

		/**The cached record of whether the resource exists.*/
		private final boolean exists;

			/**@return The cached record of whether the resource exists.*/
			public boolean exists() {return exists;}

		/**Constructor.
		@param exists Whether the resource is currently known to exist.
		*/
		public CachedExists(final boolean exists)
		{
			this.exists=exists;	//save the cached existence state
		}
	}

}
