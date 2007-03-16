package com.garretwilson.net.http;

import java.io.*;
import java.net.*;

import com.garretwilson.io.IO;
import com.garretwilson.io.OutputStreamDecorator;
import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.net.URIConstants.*;
import static com.garretwilson.net.URIUtilities.*;

import static com.garretwilson.net.http.HTTPConstants.*;

import com.garretwilson.util.Debug;
import com.garretwilson.model.DefaultResource;
import com.garretwilson.net.Authenticable;

/**A client's view of an HTTP resource on the server.
For many error conditions, a subclass of {@link HTTPException} will be thrown.
This class is not thread safe.
@author Garret Wilson
@see HTTPException
*/
public class HTTPResource extends DefaultResource	//TODO update class to have a cache timeout
{

	/**The client used to create a connection to this resource.*/
	private final HTTPClient client;

		/**@return The client used to create a connection to this resource.*/
		protected HTTPClient getClient() {return client;}

	/**The preset password authentication, or <code>null</code> if this resource specifies no preset password authentication.*/
	private final PasswordAuthentication passwordAuthentication;
	
		/**@return The preset password authentication, or <code>null</code> if this connection specifies no preset password authentication.*/
		protected PasswordAuthentication getPasswordAuthentication() {return passwordAuthentication;}

	/**The length of time, in milliseconds, to keep cached information.*/
	private final static long CACHE_EXPIRATION_MILLISECONDS=5000;
		
	/**The last time the cache was updated, in milliseconds, or -1 if no information has ever been cached.*/
	protected long lastCachedMilliseconds=-1;
		
	/**Whether cached properties are to be returned; the default is <code>true</code>.*/
	private boolean cached=true;

		/**@return Whether cached properties are to be returned; the default is <code>true</code>.*/
		public boolean isCached() {return cached;}

		/**Sets whether cached properties should be used.
		Properties are always cached (but not returned) even when caching is turned off.
		@param cached Whether cached properties are to be returned.
		@see #emptyCache()
		*/
		public void setCached(final boolean cached)
		{
			if(this.cached!=cached)	//if the caching state is changing
			{
				this.cached=cached;	//update the cache state
			}
		}

		/**Determines if the cache is valid.
		@return <code>true</code> if information is being cached and the cache is not stale.
		*/
		protected boolean isCacheValid()
		{
			return isCached() && System.currentTimeMillis()-lastCachedMilliseconds<CACHE_EXPIRATION_MILLISECONDS;	//see if information is being cached and the cache isn't yet expired
		}

	/**Removes all cached values.*/
	public void emptyCache()
	{
		cachedExists=null;	//uncache the existence state
		lastCachedMilliseconds=-1;	//indicate that the cache has expired
	}

	/**The cached existence state, or <code>null</code> if existence has not yet been cached.*/
	protected Boolean cachedExists=null;

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
		final HTTPRequest request=new DefaultHTTPRequest(DELETE_METHOD, getReferenceURI());	//create a DELETE request
		final HTTPResponse response=sendRequest(request);	//get the response
		//TODO change the cached exists() status
	}

	/**Determines if a resource exists.
	This implementation checks for existence by invoking the HEAD method and then looking at the cached existence value.
	The cached existence property is updated.
	@return <code>true</code> if the resource is present on the server.
	@exception IOException if there was an error invoking a method.
	@see #cachedExists
	*/
	public boolean exists() throws IOException
	{
		if(!isCacheValid() || cachedExists==null)	//if the cache is stale or we don't have a cached existence value
		{
			try
			{
				head();	//invoke the HEAD method to update the cached existence value
			}
			catch(final HTTPNotFoundException notFoundException)	//ignore 404 Not Found
			{
			}
			catch(final HTTPGoneException goneException)	//ignore 410 Gone
			{
			}
			assert cachedExists!=null : "Expected head() to cache existence value.";
		}
		return cachedExists.booleanValue();	//return the cached existence value TODO fix the race condition here
	}

	/**Retrieves the contents of a resource using the GET method.
	@return An input stream to the server.
	@exception IOException if there was an error invoking the method.
	*/
	public InputStream getInputStream() throws IOException
	{
		return new ByteArrayInputStream(get());	//return an input stream to the result of the GET method
	}

	/**Retrieves the contents of a resource using the GET method.
	The cached existence property is updated.
	@return The content received from the server.
	@exception IOException if there was an error invoking the method.
	@see #cachedExists
	*/
	public byte[] get() throws IOException
	{
		try
		{
			final HTTPRequest request=new DefaultHTTPRequest(GET_METHOD, getReferenceURI());	//create a GET request
			final HTTPResponse response=sendRequest(request);	//get the response
			cachedExists=Boolean.TRUE;	//if no exceptions were thrown, assume the resource exists
			lastCachedMilliseconds=System.currentTimeMillis();	//update the cache clock
			return response.getBody();	//return the bytes received from the server
		}
		catch(final HTTPNotFoundException notFoundException)	//404 Not Found
		{
			cachedExists=Boolean.FALSE;	//show that the resource is not there
			lastCachedMilliseconds=System.currentTimeMillis();	//update the cache clock
			throw notFoundException;	//rethrow the exception
		}
		catch(final HTTPGoneException goneException)	//410 Gone
		{
			cachedExists=Boolean.FALSE;	//show that the resource is permanently not there
			lastCachedMilliseconds=System.currentTimeMillis();	//update the cache clock
			throw goneException;	//rethrow the exception
		}
	}

	/**Accesses a resource using the HEAD method.
	The cached existence property is updated.
	@exception IOException if there was an error invoking the method.
	@see #cachedExists
	*/
	public void head() throws IOException
	{
		try
		{
			final HTTPRequest request=new DefaultHTTPRequest(HEAD_METHOD, getReferenceURI());	//create a HEAD request
			final HTTPResponse response=sendRequest(request);	//get the response
			cachedExists=Boolean.TRUE;	//if no exceptions were thrown, assume the resource exists
			lastCachedMilliseconds=System.currentTimeMillis();	//update the cache clock
		}
		catch(final HTTPNotFoundException notFoundException)	//404 Not Found
		{
			cachedExists=Boolean.FALSE;	//show that the resource is not there
			lastCachedMilliseconds=System.currentTimeMillis();	//update the cache clock
			throw notFoundException;	//rethrow the exception
		}
		catch(final HTTPGoneException goneException)	//410 Gone
		{
			cachedExists=Boolean.FALSE;	//show that the resource is permanently not there
			lastCachedMilliseconds=System.currentTimeMillis();	//update the cache clock
			throw goneException;	//rethrow the exception
		}
	}

	/**Stores the contents of a resource using the PUT method.
	The cache is emptied.
	@param content The bytes to store at the resource location. 
	@exception IOException if there was an error invoking the method.
	*/
	public void put(final byte[] content) throws IOException
	{
//TODO del Debug.trace("ready to put bytes:", content.length);
		emptyCache();	//empty any cached information about the resource, as we'll be changing the resource
		final HTTPRequest request=new DefaultHTTPRequest(PUT_METHOD, getReferenceURI());	//create a PUT request
		request.setBody(content);	//set the content of the request 
		final HTTPResponse response=sendRequest(request);	//get the response
//TODO del Debug.trace("response:", response.getStatusCode());
		cachedExists=Boolean.TRUE;	//if no exceptions were thrown, assume the resource exists because we just created it
		lastCachedMilliseconds=System.currentTimeMillis();	//update the cache clock
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
			return io.read(inputStream, getReferenceURI());	//read the object, using the resource reference URI as the base URI
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
			io.write(outputStream, getReferenceURI(), object);	//write the object, using the resource reference URI as the base URI
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
		final URI referenceURI=getReferenceURI();	//get the reference URI
		final boolean secure=HTTPS_SCHEME.equals(referenceURI.getScheme());	//see if this connection should be secure
		final HTTPClientTCPConnection connection=getClient().createConnection(getHost(getReferenceURI()), getPasswordAuthentication(), secure);	//get a connection to the URI
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
		final URI referenceURI=getReferenceURI();	//get the reference URI
		final boolean secure=HTTPS_SCHEME.equals(referenceURI.getScheme());	//see if this connection should be secure
		final HTTPClientTCPConnection connection=getClient().createConnection(getHost(getReferenceURI()), getPasswordAuthentication(), secure);	//get a connection to the URI
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
}
