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

/**A client's view of an HTTP resource on the server.
For many error conditions, a subclass of <code>HTTPException</code>
	will be thrown.
@author Garret Wilson
@see HTTPException
*/
public class HTTPResource extends DefaultResource
{

	/**The client used to create a connection to this resource.*/
	private final HTTPClient client;

		/**@return The client used to create a connection to this resource.*/
		protected HTTPClient getClient() {return client;}

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
		
	/**Removes all cached values.*/
	public void emptyCache()
	{
		cachedExists=null;	//uncache the existence state
	}

	/**The cached existence state, or <code>null</code> if existence has not yet been cached.*/
	protected Boolean cachedExists=null;

	/**Constructs an HTTP resource at a particular URI using the default client.
	@param referenceURI The URI of the HTTP resource this object represents.
	@exception IllegalArgumentException if the given reference URI is not absolute,
		the reference URI has no host, or the scheme is not an HTTP scheme.
	@exception NullPointerException if the given reference URI is <code>null</code>.
	*/
	public HTTPResource(final URI referenceURI) throws IllegalArgumentException, NullPointerException
	{
		this(referenceURI, HTTPClient.getInstance());	//construct the resource with the default client
	}

	/**Constructs an HTTP resource at a particular URI using a particular client.
	@param referenceURI The URI of the HTTP resource this object represents.
	@param client The client used to create a connection to this resource.
	@exception IllegalArgumentException if the given reference URI is not absolute,
		the reference URI has no host, or the scheme is not an HTTP scheme.
	@exception NullPointerException if the given reference URI or client is <code>null</code>.
	*/
	public HTTPResource(final URI referenceURI, final HTTPClient client) throws IllegalArgumentException, NullPointerException
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
		if(!isCached() || cachedExists==null)	//if we aren't returning cached values, or we don't have a cached existence value
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
		return cachedExists.booleanValue();	//return the cached existence value
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
			return response.getBody();	//return the bytes received from the server
		}
		catch(final HTTPNotFoundException notFoundException)	//404 Not Found
		{
			cachedExists=Boolean.FALSE;	//show that the resource is not there
			throw notFoundException;	//rethrow the exception
		}
		catch(final HTTPGoneException goneException)	//410 Gone
		{
			cachedExists=Boolean.FALSE;	//show that the resource is permanently not there
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
		}
		catch(final HTTPNotFoundException notFoundException)	//404 Not Found
		{
			cachedExists=Boolean.FALSE;	//show that the resource is not there
			throw notFoundException;	//rethrow the exception
		}
		catch(final HTTPGoneException goneException)	//410 Gone
		{
			cachedExists=Boolean.FALSE;	//show that the resource is permanently not there
			throw goneException;	//rethrow the exception
		}
	}

	/**Stores the contents of a resource using the PUT method.
	@param content The bytes to store at the resource location. 
	@exception IOException if there was an error invoking the method.
	*/
	public void put(final byte[] content) throws IOException
	{
Debug.trace("ready to put bytes:", content.length);
		final HTTPRequest request=new DefaultHTTPRequest(PUT_METHOD, getReferenceURI());	//create a PUT request
		request.setBody(content);	//set the content of the request 
		final HTTPResponse response=sendRequest(request);	//get the response
Debug.trace("response:", response.getStatusCode());
		cachedExists=Boolean.TRUE;	//if no exceptions were thrown, assume the resource exists because we just created it
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
	@exception IOException if there was an error sending the request or receiving the response.
	*/
	protected HTTPResponse sendRequest(final HTTPRequest request) throws IOException	//TODO add connection peristence
	{
		final URI referenceURI=getReferenceURI();	//get the reference URI
		final boolean secure=HTTPS_SCHEME.equals(referenceURI.getScheme());	//see if this connection should be secure
		final HTTPClientTCPConnection connection=getClient().createConnection(getHost(getReferenceURI()), secure);	//get a connection to the URI
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
	
	  /**Closes this output stream and releases any system resources associated with this stream.
	  This version writes the accumulated data to the HTTP resource, and unconditionally
	  	releases the accumulated bytes.
	  @exception IOException if an I/O error occurs.
	  */
		public void close() throws IOException	//TODO what if there is an error writing to the stream, and the client tries to close the stream out of an attempt for consistency?
		{
			final ByteArrayOutputStream byteArrayOutputStream=getOutputStream();	//get the decorated output stream, if there still is one
			if(byteArrayOutputStream!=null)	//if we had a byte array output stream before closing, this adapter was still open
			{
				final byte[] bytes=byteArrayOutputStream.toByteArray();	//get the collected bytes
				try
				{
					put(bytes);	//put the bytes to the HTTP resource
				}
				finally
				{
					super.close();	//do the default closing, releasing the decorated output stream from the decorator
				}
			}
  	}
	}

}
