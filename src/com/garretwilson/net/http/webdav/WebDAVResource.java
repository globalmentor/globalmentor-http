package com.garretwilson.net.http.webdav;

import java.io.*;
import java.net.*;
import java.util.*;
import static java.util.Collections.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static com.garretwilson.lang.CharSequenceUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.net.URIConstants.*;
import static com.garretwilson.net.URIUtilities.*;
import com.garretwilson.net.http.*;
import static com.garretwilson.net.http.webdav.WebDAVConstants.*;
import static com.garretwilson.net.http.webdav.WebDAVUtilities.*;

import com.garretwilson.text.xml.QualifiedName;
import com.garretwilson.text.xml.XMLUtilities;
import com.garretwilson.util.CollectionUtilities;
import com.garretwilson.util.Debug;
import com.garretwilson.util.IDMappedList;
import com.garretwilson.util.NameValuePair;

/**A client's view of a WebDAV resource on the server.
For many error conditions, a subclass of <code>HTTPException</code>
	will be thrown.
@author Garret Wilson
@see HTTPException
*/
public class WebDAVResource extends HTTPResource
{
		
	/**Removes all cached values.*/
	public void emptyCache()
	{
		super.emptyCache();	//empty the parent's cache
		cachedPropertyList=null;	//uncache the properties
	}

	/**The cached list of no-depth properties for this resource, or <code>null</code> if no properties have been cached.
	Properties are only cached if all properties are requested.
	*/
	protected List<NameValuePair<QualifiedName, ?>> cachedPropertyList=null;
		
	/**Constructs a WebDAV resource at a particular URI using the default client.
	@param referenceURI The URI of the WebDAV resource this object represents.
	@exception IllegalArgumentException if the given reference URI is not absolute,
		the reference URI has no host, or the scheme is not an HTTP scheme.
	@exception NullPointerException if the given reference URI or client is <code>null</code>.
	*/
	public WebDAVResource(final URI referenceURI) throws IllegalArgumentException, NullPointerException
	{
		super(referenceURI);	//construct the parent class
	}

	/**Constructs a WebDAV resource at a particular URI using a particular client.
	@param referenceURI The URI of the WebDAV resource this object represents.
	@param client The client used to create a connection to this resource.
	@exception IllegalArgumentException if the given reference URI is not absolute,
		the reference URI has no host, or the scheme is not an HTTP scheme.
	@exception NullPointerException if the given reference URI or client is <code>null</code>.
	*/
	public WebDAVResource(final URI referenceURI, final HTTPClient client) throws IllegalArgumentException, NullPointerException
	{
		super(referenceURI, client);	//construct the parent class
	}

	/**Copies the resource using the COPY method with an infinite depth, overwriting any resource at the given destination URI.
	This implementation delegates to {@link #copy(URI, boolean)}.
	@param destinationURI The destination to which the resource will be copied.
	@exception NullPointerException if the given destination is <code>null</code>.
	@exception IllegalArgumentException if the given destination URI is not absolute.
	@exception HTTPPreconditionFailedException if the operation failed because of the overwrite setting.
	@exception IOException if there was an error invoking the method.
	@see Depth#INFINITY
	*/
	public void copy(final URI destinationURI) throws IOException
	{
		copy(destinationURI, true);	//copy the resource with overwrite
	}

	/**Copies the resource using the COPY method, overwriting any resource at the given destination URI, specifying the depth.
	This implementation delegates to {@link #copy(URI, Depth, boolean)}.
	@param destinationURI The destination to which the resource will be copied.
	@param depth The depth to copy; either {@link Depth#ZERO} or {@link Depth#INFINITY}.
	@exception NullPointerException if the given destination and/or depth is <code>null</code>.
	@exception IllegalArgumentException if the given destination URI is not absolute.
	@exception IllegalArgumentException if the given given depth is not {@link Depth#ZERO} or {@link Depth#INFINITY}.
	@exception HTTPPreconditionFailedException if the operation failed because of the overwrite setting.
	@exception IOException if there was an error invoking the method.
	*/
	public void copy(final URI destinationURI, final Depth depth) throws IOException
	{
		copy(destinationURI, depth, true);	//copy the resource with overwrite
	}

	/**Copies the resource using the COPY method with an infinite depth, specifying whether overwrite should occur.
	This implementation delegates to {@link #copy(URI, Depth, boolean)}.
	@param destinationURI The destination to which the resource will be copied.
	@param overwrite Whether overwrite should occur if a resource already exists at the given destination URI.
	@exception NullPointerException if the given destination is <code>null</code>.
	@exception IllegalArgumentException if the given destination URI is not absolute.
	@exception HTTPPreconditionFailedException if the operation failed because of the overwrite setting.
	@exception IOException if there was an error invoking the method.
	@see Depth#INFINITY
	*/
	public void copy(final URI destinationURI, final boolean overwrite) throws IOException
	{
		copy(destinationURI, Depth.INFINITY, overwrite);	//copy the resource with infinite depth
	}

	/**Copies the resource using the COPY method, specifying the depth and whether overwrite should occur.
	@param destinationURI The destination to which the resource will be copied.
	@param depth The depth to copy; either {@link Depth#ZERO} or {@link Depth#INFINITY}.
	@param overwrite Whether overwrite should occur if a resource already exists at the given destination URI.
	@exception NullPointerException if the given destination and/or depth is <code>null</code>.
	@exception IllegalArgumentException if the given destination URI is not absolute.
	@exception IllegalArgumentException if the given given depth is not {@link Depth#ZERO} or {@link Depth#INFINITY}.
	@exception HTTPPreconditionFailedException if the operation failed because of the overwrite setting.
	@exception IOException if there was an error invoking the method.
	*/
	public void copy(final URI destinationURI, final Depth depth, final boolean overwrite) throws IOException
	{
		final WebDAVRequest request=new DefaultWebDAVRequest(COPY_METHOD, getReferenceURI());	//create a COPY request
		request.setDestination(destinationURI);	//set the destination URI
		checkInstance(depth, "Depth cannot be null.");
		if(depth!=Depth.ZERO && depth!=Depth.INFINITY)	//if the depth is not ZERO or INFINITY
		{
			throw new IllegalArgumentException("Depth of "+depth+" is not allowed for the "+COPY_METHOD+" method.");
		}
		request.setDepth(depth);	//set the depth
		request.setOverwrite(overwrite);	//set the overwrite option
		final HTTPResponse response=sendRequest(request);	//get the response
	}

	/**Creates a collection using the MKCOL method.
	@exception IOException if there was an error invoking the method.
	@see #mkCols()
	*/
	public void mkCol() throws IOException
	{
		final WebDAVRequest request=new DefaultWebDAVRequest(MKCOL_METHOD, getReferenceURI());	//create a MKCOL request
		final HTTPResponse response=sendRequest(request);	//get the response
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
//				TODO del Debug.trace("looking at segment URI", segmentURI);
					final WebDAVResource segmentWebDAVResource=new WebDAVResource(segmentURI, getClient());	//create a WebDAV resource for this segment of the path, using the same client
					if(!segmentWebDAVResource.exists())	//if this segment collection doesn't exist //TODO later use an isCollection() or something
					{
//					TODO del Debug.trace("making collection for segment URI", segmentURI);
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

	
	
	
	/**Moves the resource using the MOVE method with an infinite depth, overwriting any resource at the given destination URI.
	This implementation delegates to {@link #move(URI, boolean)}.
	@param destinationURI The destination to which the resource will be moved.
	@exception NullPointerException if the given destination is <code>null</code>.
	@exception IllegalArgumentException if the given destination URI is not absolute.
	@exception HTTPPreconditionFailedException if the operation failed because of the overwrite setting.
	@exception IOException if there was an error invoking the method.
	@see Depth#INFINITY
	*/
	public void move(final URI destinationURI) throws IOException
	{
		move(destinationURI, true);	//move the resource with overwrite
	}

	/**Moves the resource using the MOVE method with an infinite depth, specifying whether overwrite should occur.
	@param destinationURI The destination to which the resource will be moved.
	@param overwrite Whether overwrite should occur if a resource already exists at the given destination URI.
	@exception NullPointerException if the given destination is <code>null</code>.
	@exception IllegalArgumentException if the given destination URI is not absolute.
	@exception HTTPPreconditionFailedException if the operation failed because of the overwrite setting.
	@exception IOException if there was an error invoking the method.
	@see Depth#INFINITY
	*/
	public void move(final URI destinationURI, final boolean overwrite) throws IOException
	{
		final WebDAVRequest request=new DefaultWebDAVRequest(MOVE_METHOD, getReferenceURI());	//create a MOVE request
		request.setDestination(destinationURI);	//set the destination URI
		request.setDepth(Depth.INFINITY);	//set the depth to infinity
		request.setOverwrite(overwrite);	//set the overwrite option
		final HTTPResponse response=sendRequest(request);	//get the response
	}
		
	/**Retrieves properties using the PROPFIND method.
	The cached property list is updated.
	The URI of each resource is canonicized to be an absolute URI.
	@param depth The requested depth.
	@return A list of all properties of all requested resources, each representing the URI of the resource paired by a list of its
		properties, each property representing  the qualified name of the property and the value.
	@exception IOException if there was an error invoking the method.
	@see #cachedPropertyList
	*/
	public List<NameValuePair<URI, List<NameValuePair<QualifiedName, ?>>>> propFind(final Depth depth) throws IOException
	{
		final URI referenceURI=getReferenceURI();	//get the reference URI of this resource
		if(isCached() && depth==Depth.ZERO && cachedPropertyList!=null)	//if we can return cached values, only the properties for this resource are requested, and we have a cached property list
		{
			final List<NameValuePair<URI, List<NameValuePair<QualifiedName, ?>>>> cachedPropFindList=new ArrayList<NameValuePair<URI, List<NameValuePair<QualifiedName, ?>>>>();
			cachedPropFindList.add(new NameValuePair<URI, List<NameValuePair<QualifiedName, ?>>>(referenceURI, cachedPropertyList));	//add the property list for this resource to the list, paired with its URI TODO make sure it doesn't hurt to use our own URI---will forwarding affect this?
			return cachedPropFindList;	//return the manufactured property list from our cached properyy list
		}
		final WebDAVRequest request=new DefaultWebDAVRequest(PROPFIND_METHOD, referenceURI);	//create a MKCOL request
		request.setDepth(depth);	//set the requested depth
		final Document propfindDocument=createPropfindDocument(getDOMImplementation());	//create a multistatus document	//TODO check DOM exceptions here
		addProperties(propfindDocument.getDocumentElement(), ALL_PROPERTIES);	//show that we want to know about all properties
		request.setXML(propfindDocument);	//set the XML in the body of our request
		final HTTPResponse response=sendRequest(request);	//get the response
		final Document document=response.getXML();	//get the XML from the response body
		if(document!=null)	//if there was an XML document in the request
		{
			
//		TODO del Debug.trace(XMLUtilities.toString(document));
			
			final Element documentElement=document.getDocumentElement();	//get the document element
				//TODO check to make sure the document element is correct
			final List<NameValuePair<URI, List<NameValuePair<QualifiedName, ?>>>> propertyLists=getMultistatusProperties(documentElement, referenceURI);	//get the properties from the multistatus document, relative to this resource URI			
			for(final NameValuePair<URI, List<NameValuePair<QualifiedName, ?>>> propertyList:propertyLists)	//look at each property list
			{
				if(propertyList.getName().equals(referenceURI))	//if this property list is for this resource
				{
					cachedPropertyList=propertyList.getValue();	//cache the list of properties for this resource
					break;	//stop looking for properties to cache
				}
			}
			return propertyLists;	//return all the properties requested
		}
		return CollectionUtilities.emptyList();	//return an empty list, because there was no XML returned
	}

	/**Retrieves properties of this resource using the PROPFIND method.
	This is a convenience method for <code>propFind(Depth)</code>.
	The cached property list is updated.
	The URI of each resource is canonicized to be an absolute URI.
	@return A list of all properties of this resource, each property representing  the qualified name of the property and the value.
	@exception IOException if there was an error invoking the method.
	@see #propFind(Depth)
	@see #cachedPropertyList
	*/
	public List<NameValuePair<QualifiedName, ?>> propFind() throws IOException
	{
		final URI referenceURI=getReferenceURI();	//get the reference URI of this resource
		final List<NameValuePair<URI, List<NameValuePair<QualifiedName, ?>>>> propertyLists=propFind(Depth.ZERO);	//do a propfind with no depth
		for(final NameValuePair<URI, List<NameValuePair<QualifiedName, ?>>> propertyList:propertyLists)	//look at each property list
		{
			if(propertyList.getName().equals(referenceURI))	//if this property list is for this resource
			{
				return propertyList.getValue();	//return the list of properties for this resource
			}
		}
		return CollectionUtilities.emptyList();	//return an empty list; for some reason, no properties were returned		
	}

}
