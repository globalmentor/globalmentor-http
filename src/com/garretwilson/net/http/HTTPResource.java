package com.garretwilson.net.http;

import java.io.*;
import java.net.*;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.net.URIConstants.*;
import static com.garretwilson.net.URIUtilities.*;
import com.garretwilson.net.http.*;
import static com.garretwilson.net.http.HTTPConstants.*;
import static com.garretwilson.net.http.webdav.WebDAVConstants.*;
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
		this.client=checkNull(client, "Client cannot be null.");	//save the client
	}

	/**Determines if a resource exists.
	This implementation checks for existence by invoking the HEAD method.
	@return <code>true</code> if the resource is present on the server.
	@exception IOException if there was an error invoking a method.
	*/
	public boolean exists() throws IOException
	{
		try
		{
			head();	//invoke the HEAD method
		}
		catch(final HTTPNotFoundException notFoundException)	//404 Not Found
		{
			return false;	//show that the resource is not there
		}
		catch(final HTTPGoneException goneException)	//410 Gone
		{
			return false;	//show that the resource is permanently not there
		}
		return true;	//if no exceptions were thrown, assume the resource exists
	}

	/**Retrieves the contents of a resource using the GET method.
	@return An input stream to the server.
	@exception IOException if there was an error invoking the method.
	*/
/*G***del if not needed
	public InputStream get() throws IOException
	{
		final HTTPRequest request=new DefaultHTTPRequest(GET_METHOD, getReferenceURI());	//create a GET request
		final HTTPResponse response=sendRequest(request);	//get the response
		final byte[] body=response.getBody();	//get the body of the response
		return new ByteArrayInputStream(body);	//return an input stream to the response body
	}
*/

	/**Retrieves the contents of a resource using the GET method.
	@return The content received from the server.
	@exception IOException if there was an error invoking the method.
	*/
	public byte[] get() throws IOException
	{
		final HTTPRequest request=new DefaultHTTPRequest(GET_METHOD, getReferenceURI());	//create a GET request
		final HTTPResponse response=sendRequest(request);	//get the response
		return response.getBody();	//return the bytes received from the server
	}

	/**Accesses a resource using the HEAD method.
	@exception IOException if there was an error invoking the method.
	*/
	public void head() throws IOException
	{
		final HTTPRequest request=new DefaultHTTPRequest(HEAD_METHOD, getReferenceURI());	//create a HEAD request
		final HTTPResponse response=sendRequest(request);	//get the response
	}

	/**Stores the contents of a resource using the PUT method.
	@param content The bytes to store at the resource location. 
	@return An output stream to the server.
	@exception IOException if there was an error invoking the method.
	*/
	public void put(final byte[] content) throws IOException
	{
		final HTTPRequest request=new DefaultHTTPRequest(PUT_METHOD, getReferenceURI());	//create a PUT request
		request.setBody(content);	//set the content of the request 
		final HTTPResponse response=sendRequest(request);	//get the response
	}

	/**Sends a request to the server.
	@exception IOException if there was an error sending the request or receiving the response.
	*/
	protected HTTPResponse sendRequest(final HTTPRequest request) throws IOException	//TODO add connection peristence
	{
		final HTTPClientTCPConnection connection=getClient().createConnection(getHost(getReferenceURI()));	//get a connection to the URI
		try
		{
			return connection.sendRequest(request);	//send the request and return the response
		}
		finally
		{
			connection.disconnect();	//always close the connection
		}
	}

}
