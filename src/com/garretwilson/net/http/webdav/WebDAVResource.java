package com.garretwilson.net.http.webdav;

import java.io.*;
import java.net.*;
import java.util.StringTokenizer;

import static com.garretwilson.lang.CharSequenceUtilities.*;
import static com.garretwilson.net.URIConstants.*;
import static com.garretwilson.net.URIUtilities.*;
import com.garretwilson.net.http.*;
import static com.garretwilson.net.http.webdav.WebDAVConstants.*;
import com.garretwilson.util.Debug;

/**A client's view of a WebDAV resource on the server.
For many error conditions, a subclass of <code>HTTPException</code>
	will be thrown.
@author Garret Wilson
@see HTTPException
*/
public class WebDAVResource extends HTTPResource
{

	/**Constructs a WebDAV resource at a particular URI.
	@param referenceURI The URI of the WebDAV resource this object represents.
	@exception IllegalArgumentException if the given reference URI is not absolute,
		or the scheme is not an HTTP scheme.
	@exception NullPointerException if the given reference URI is <code>null</code>.
	*/
	public WebDAVResource(final URI referenceURI) throws IllegalArgumentException, NullPointerException
	{
		super(referenceURI);	//construct the parent class
	}

	/**Creates a collection using the MKCOL method.
	@exception IOException if there was an error invoking the method.
	@see #mkCols()
	*/
	public void mkCol() throws IOException
	{
		final HttpURLConnection connection=createConnection(MKCOL_METHOD, false);	//create a MKCOL connection to the server
		connection.connect();	//make the request
		checkResponse(connection);	//check the response
	}

	/**Creates the collection path of the URI as needed.
	If the reference URI of this resource does not end in '/',
		the last segment will not be considered a collection and will not be created. 
	@exception IOException if there is an error creating one of the path segments.
	@see #mkCols(URI)
	*/
	public void mkCols() throws IOException
	{
		mkCols(changePath(getReferenceURI(), ROOT_PATH));	//make the collections starting from the root
	}

	/**Creates the collection path of the URI as needed.
	If the reference URI of this resource does not end in '/',
		the last segment will not be considered a collection and will not be created. 
	@param baseURI The base URI to assume exists; must end in '/'. 
	@exception IOException if there is an error creating one of the path segments.
	@exception IllegalArgumentException if the base path does not end in '/',
		or the path path is not a base path of this resource's URI.
	*/
	public void mkCols(final URI baseURI) throws IOException
	{
		final String baseURIString=baseURI.toString();	//get the base URI as a string
		if(!endsWith(baseURIString, PATH_SEPARATOR))	//if the base URI doesn't end with the path separator
		{
			throw new IllegalArgumentException("Base URI "+baseURI+" does not end with '"+PATH_SEPARATOR+"'.");
		}
		final String referenceURIString=getReferenceURI().toString();	//get the string version of the resource's reference URI
		if(!referenceURIString.startsWith(baseURIString))	//if the reference URI doesn't start with the given base URI
		{
			throw new IllegalArgumentException("Resource URI "+referenceURIString+" does not begin with base URI "+baseURIString);
		}
		/*G***del
		final String remainingPath=referenceURIString.substring(baseURIString.length());	//get the remaining path to examine
		final URI baseURI=changePath(uri, basePath);	//get the base URI
		final WebdavResource baseWebdavResource=new WebdavResource(new HttpURL(baseURI.toString()), WebdavResource.NOACTION, 0);	//create a WebDAV resource representing the base URI
		if(username!=null && password!=null)		//if a username and password is given
		{
			baseWebdavResource.setUserInfo(username, new String(password));	//set the username and password for the WebDAV resource
		}
*/
		final String remainingPath=referenceURIString.substring(baseURIString.length());	//get the remaining path to examine
		final StringBuilder uriBuilder=new StringBuilder(baseURIString);	//we'll check each component of the reconstructed path as we add it to the URI builder
		final String delimiter=String.valueOf(PATH_SEPARATOR);	//determine the path delimiter
		final StringTokenizer tokenizer=new StringTokenizer(remainingPath, delimiter, true);	//get the path tokens, returning the delimiters as well
		while(tokenizer.hasMoreTokens())	//while there are more tokens
		{
			final String token=tokenizer.nextToken();	//get the next token
			uriBuilder.append(token);	//add the token to our path
			if(delimiter.equals(token))	//if we just finished a collection segment
			{
				try
				{
					final URI segmentURI=new URI(uriBuilder.toString());	//create a URI for this segment of the path
Debug.trace("looking at segment URI", segmentURI);
					final WebDAVResource segmentWebDAVResource=new WebDAVResource(segmentURI);	//create a WebDAV resource for this segment of the path
					if(!segmentWebDAVResource.exists())	//if this segment collection doesn't exist //TODO later use an isCollection() or something
					{
Debug.trace("making collection for segment URI", segmentURI);
						segmentWebDAVResource.mkCol();	//make this collection
					}
				}
				catch(final URISyntaxException uriSyntaxException)	//as we are creating this URI from an existing URI, segment by segment, we should never run into a URI syntax problem
				{
					throw new AssertionError(uriSyntaxException);
				}
			}
		}
	}

}
