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

	/**The URL used as a connection factory.*/
	private final URL url;

		/**@return The URL used as a connection factory.*/
		protected URL getURL() {return url;}

	/**Constructs an HTTP resource at a particular URI.
	@param referenceURI The URI of the HTTP resource this object represents.
	@exception IllegalArgumentException if the given reference URI is not absolute,
		or the scheme is not an HTTP scheme.
	@exception NullPointerException if the given reference URI is <code>null</code>.
	*/
	public HTTPResource(final URI referenceURI) throws IllegalArgumentException, NullPointerException
	{
		super(referenceURI);	//construct the parent class
		final String scheme=referenceURI.getScheme();	//get the URI scheme
		if(!HTTP_SCHEME.equals(scheme) && !HTTPS_SCHEME.equals(scheme))	//if this isn't a HTTP or HTTPS resource
		{
			throw new IllegalArgumentException("Invalid HTTP scheme "+scheme);
		}
		try
		{
			url=referenceURI.toURL();	//create a URL from the URI
		}
		catch(final MalformedURLException malformedURLException)	//there should always be protocol handlers for HTTP and HTTPS
		{
			throw new AssertionError(malformedURLException);
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
	public InputStream get() throws IOException
	{
		final HttpURLConnection connection=createConnection(GET_METHOD, false);	//create a GET connection to the server
		connection.connect();	//make the request
		checkResponse(connection);	//check the response
		return connection.getInputStream();	//return the input stream
	}

	/**Accesses a resource using the HEAD method.
	@exception IOException if there was an error invoking the method.
	*/
	public void head() throws IOException
	{
		final HttpURLConnection connection=createConnection(HEAD_METHOD, false);	//create a HEAD connection to the server
		connection.connect();	//make the request		
		checkResponse(connection);	//check the response
	}

	/**Creates a collection using the MKCOL method.
	@exception IOException if there was an error invoking the method.
	*/
	public void mkCol() throws IOException
	{
		final HttpURLConnection connection=createConnection(MKCOL_METHOD, false);	//create a MKCOL connection to the server
		connection.connect();	//make the request
		checkResponse(connection);	//check the response
	}

	/**Stores the contents of a resource using the PUT method.
	@return An output stream to the server.
	@exception IOException if there was an error invoking the method.
	*/
	public OutputStream put() throws IOException
	{
		final HttpURLConnection connection=createConnection(PUT_METHOD, true);	//create a PUT connection to the server
		connection.connect();	//make the request
		checkResponse(connection);	//check the response
		return connection.getOutputStream();	//return the output stream
	}

	/**Creates a connection to the server.
	The returned connetion will not be connected.
	@param method The HTTP method to use.
	@param doOutput Whether content will be sent to the server.
	@exception IOException if there was an error creating the connection.
	*/
	protected HttpURLConnection createConnection(final String method, final boolean doOutput) throws IOException
	{
		final HttpURLConnection connection=(HttpURLConnection)getURL().openConnection();	//open a connection to the URL
		connection.setDoInput(true);	//we'll usually try to get some sort of input, if only the return code
		connection.setDoOutput(doOutput);	//specify whether output will be performed
		connection.setRequestMethod(method);	//set the request method
		return connection;	//return the connection
	}

	/**Checks the given connection's response code.
	<p>This method forces the connection to make its connection request if it hasn't already.</p>
	<p>If the response code represents an error condition for which an HTTP exception
		is available, an HTTP exception is thrown.</p>
	<p>This method calls <code>checkReturnCode(int, String)</code>, and most subclasses
		should override that method instead of this one.</p>
	@param connection The connection to check.
	@exception HTTPException if the response code represents a known error condition
		for which an HTTP exception class is available.
	@exception IOException if there was some error accessing the connection's response code and message.
	*/
	protected void checkResponse(final HttpURLConnection connection) throws HTTPException, IOException
	{
		checkResponseCode(connection.getResponseCode(), connection.getResponseMessage());	//check the connection's response code and response message
	}

	/**Checks the given HTTP response code.
	If the response code represents an error condition for which an HTTP exception
	is available, an HTTP exception is thrown.
	@param responseCode The HTTP response code to check.
	@param responseMessage The response message received with the response code.
	@exception HTTPException if the response code represents a known error condition
		for which an HTTP exception class is available.
	*/
	protected static void checkResponseCode(final int responseCode, final String responseMessage) throws HTTPException
	{
		switch(responseCode)	//see which response code this is
		{
			case SC_FORBIDDEN:	//403 Forbidden
				throw new HTTPForbiddenException(responseMessage);
			case SC_NOT_FOUND:	//404 Not Found
				throw new HTTPNotFoundException(responseMessage);
			case SC_CONFLICT:	//409 Conflict
				throw new HTTPConflictException(responseMessage);
			case SC_GONE:	//410 Gone
				throw new HTTPGoneException(responseMessage);
/*TODO
			   415 (Unsupported Media Type)- The server does not support the request
			   type of the body.

			   507 (Insufficient Storage) - The resource does not have sufficient
			   space to record the state of the resource after the execution of this
			   method.
*/

		}
	}
}
