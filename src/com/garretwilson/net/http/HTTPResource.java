package com.garretwilson.net.http;

import java.io.*;
import java.net.*;

import static com.garretwilson.net.URIConstants.*;
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

	/**Constructs an HTTP resource at a particular URI.
	@param referenceURI The URI of the HTTP resource this object represents.
	@exception IllegalArgumentException if the given reference URI is not absolute,
		or the scheme is not an HTTP scheme.
	@exception NullPointerException if the given reference URI is <code>null</code>.
	*/
	public HTTPResource(final URI referenceURI) throws IllegalArgumentException, NullPointerException
	{
		super(referenceURI);	//construct the parent class
		if(!referenceURI.isAbsolute())	//if the URI is not absolute
		{
			throw new IllegalArgumentException("URI "+referenceURI+" is not absolute.");
		}
		final String scheme=referenceURI.getScheme();	//get the URI scheme
		if(!HTTP_SCHEME.equals(scheme) && !HTTPS_SCHEME.equals(scheme))	//if this isn't a HTTP or HTTPS resource
		{
			throw new IllegalArgumentException("Invalid HTTP scheme "+scheme);
		}
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
/*G***fix
	public InputStream get() throws IOException
	{
		final HttpURLConnection connection=createConnection(GET_METHOD, false);	//create a GET connection to the server
		connection.connect();	//make the request
		checkResponse(connection);	//check the response
		return connection.getInputStream();	//return the input stream
	}
*/

	/**Accesses a resource using the HEAD method.
	@exception IOException if there was an error invoking the method.
	*/
	public void head() throws IOException
	{
		final HTTPRequest request=new DefaultHTTPRequest(HEAD_METHOD, getReferenceURI());	//create a head request
		final HTTPResponse response=sendRequest(request);	//get the response
Debug.trace("got back response with status:", response.getStatusCode(), response.getReasonPhrase());
	}

	/**Stores the contents of a resource using the PUT method.
	@param content The bytes to put store at the resource location. 
	@return An output stream to the server.
	@exception IOException if there was an error invoking the method.
	*/
	public void put(final byte[] content) throws IOException
	{
/*TODO fix
		final HttpURLConnection connection=createConnection(PUT_METHOD, true);	//create a PUT connection to the server
		connection.connect();	//make the request
		checkResponse(connection);	//check the response
		return connection.getOutputStream();	//return the output stream
*/
	}

	/**Sends a request to the server.
	@exception IOException if there was an error sending the request or receiving the response.
	*/
	protected HTTPResponse sendRequest(final HTTPRequest request) throws IOException
	{
		final HTTPClientTCPConnection connection=new HTTPClientTCPConnection(getReferenceURI());	//get a connection to the URI
		try
		{
			return connection.sendRequest(request);	//send the request and return the response
		}
		finally
		{
			connection.close();	//always close the connection
		}
	}

}
