package com.globalmentor.net.http.webdav;

import java.io.*;
import java.net.*;
import java.util.*;

import static java.util.Collections.*;

import javax.xml.parsers.ParserConfigurationException;

import static com.globalmentor.java.CharSequences.*;
import static com.globalmentor.java.Objects.*;
import static com.globalmentor.net.URIs.*;
import com.globalmentor.net.http.*;
import static com.globalmentor.net.http.webdav.WebDAVConstants.*;
import com.globalmentor.util.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**A client's view of a WebDAV resource on the server.
For many error conditions, a subclass of {@link HTTPException} will be thrown.
@author Garret Wilson
@see HTTPException
*/
public class WebDAVResource extends HTTPResource
{

	/**The soft value map containing cached properties. This map is made thread-safe through the use of {@link #cacheLock}.*/
	protected final static Map<CacheKey, CachedProperties> cachedPropertiesMap=new DecoratorReadWriteLockMap<CacheKey, CachedProperties>(new PurgeOnWriteSoftValueHashMap<CacheKey, CachedProperties>(), cacheLock);

	/**Caches the given exists status for this resource.
	This version removes cached properties if the new exists status is <code>false</code>.
	@param exists The existence status.
	*/
	protected void cacheExists(final boolean exists)
	{
		cacheLock.writeLock().lock();	//lock the cache for writing
		try
		{
			super.cacheExists(exists);	//do the default caching
			if(!exists)	//if the resource no longer exists
			{
				cachedPropertiesMap.remove(new CacheKey(getClient(), getURI()));	//uncache the properties, as the resource no longer exists
			}
		}
		finally
		{
			cacheLock.writeLock().unlock();	//always release the write lock
		}
	}

	/**Removes all cached information for a given resource.
	This version, in addition to the default functionality, uncaches properties.
	@param resourceURI The URI of the resource for which cached information should be removed.
	*/
	protected void uncacheInfo(final URI resourceURI)
	{
		cacheLock.writeLock().lock();	//lock the cache for writing
		try
		{
			super.uncacheInfo(resourceURI);	//remove the default cached info
			cachedPropertiesMap.remove(new CacheKey(getClient(), resourceURI));	//uncache the properties for the given resource
		}
		finally
		{
			cacheLock.writeLock().unlock();	//always release the write lock
		}		
	}

	/**Constructs a WebDAV resource at a particular URI using the default client.
	@param referenceURI The URI of the HTTP resource this object represents.
	@exception IllegalArgumentException if the given reference URI is not absolute, the reference URI has no host, or the scheme is not an HTTP scheme.
	@exception NullPointerException if the given reference URI is <code>null</code>.
	*/
	public WebDAVResource(final URI referenceURI) throws IllegalArgumentException, NullPointerException
	{
		this(referenceURI, (PasswordAuthentication)null);	//construct the resource with no preset password authentication
	}

	/**Constructs a WebDAV resource at a particular URI using specified password authentication.
	@param referenceURI The URI of the HTTP resource this object represents.
	@param passwordAuthentication The password authentication to use in connecting to this resource, or <code>null</code> if there should be no preset password authentication.
	@exception IllegalArgumentException if the given reference URI is not absolute, the reference URI has no host, or the scheme is not an HTTP scheme.
	@exception NullPointerException if the given reference URI is <code>null</code>.
	*/
	public WebDAVResource(final URI referenceURI, final PasswordAuthentication passwordAuthentication) throws IllegalArgumentException, NullPointerException
	{
		this(referenceURI, HTTPClient.getInstance(), passwordAuthentication);	//construct the resource with the default client		
	}

	/**Constructs a WebDAV resource at a particular URI using a particular client.
	@param referenceURI The URI of the HTTP resource this object represents.
	@param client The client used to create a connection to this resource.
	@exception IllegalArgumentException if the given reference URI is not absolute, the reference URI has no host, or the scheme is not an HTTP scheme.
	@exception NullPointerException if the given reference URI and/or client is <code>null</code>.
	*/
	public WebDAVResource(final URI referenceURI, final HTTPClient client) throws IllegalArgumentException, NullPointerException
	{
		this(referenceURI, client, null);	//construct the class with no preset password authentication
	}

	/**Constructs a WebDAV resource at a particular URI using a particular client and specified password authentication.
	@param referenceURI The URI of the HTTP resource this object represents.
	@param client The client used to create a connection to this resource.
	@param passwordAuthentication The password authentication to use in connecting to this resource, or <code>null</code> if there should be no preset password authentication.
	@exception IllegalArgumentException if the given reference URI is not absolute, the reference URI has no host, or the scheme is not an HTTP scheme.
	@exception NullPointerException if the given reference URI and/or client is <code>null</code>.
	*/
	public WebDAVResource(final URI referenceURI, final HTTPClient client, final PasswordAuthentication passwordAuthentication) throws IllegalArgumentException, NullPointerException
	{
		super(referenceURI, client, passwordAuthentication);	//construct the parent class
	}

	/**Determines the exists state for this resource
	The value is not retrieved from the cache.
	If this resource uses a cache, this version invokes the {@value WebDAVConstants#PROPFIND_METHOD} method to determine existence and cache child properties at one time.
	@return The latest determined existence status.
	@exception IOException if there was an error invoking a method.
	*/
	protected boolean getExists() throws IOException
	{
		if(isCached())	//if we are caching values, let's cache all we can
		{
			try
			{
				propFind(Depth.ONE);	//do a PROPFIND with a depth of one to get all children resource properties, proactively caching their values as well
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
		else	//if we're not caching values
		{
			return super.getExists();	//do the default functionality, which is more efficient if we're not caching values
		}
	}

	/**Determines if a resource is a collection.
	The cached collection property is updated.
	@return <code>true</code> if the resource is present on the server and is a collection.
	@exception IOException if there was an error invoking a method.
	@see #cachedCollection
	@see #propFind()
	*/
	public boolean isCollection() throws IOException
	{
		if(isCached())	//if we are using cached info
		{
			final CacheKey cacheKey=new CacheKey(getClient(), getURI());	//create a new cache key
			final CachedProperties cachedProperties=cachedPropertiesMap.get(cacheKey);	//get cached properties from the map
			if(cachedProperties!=null && !cachedProperties.isStale())	//if information is cached that isn't stale
			{
				return cachedProperties.isCollection();	//return whether the resource is a collection
			}
		}
		try
		{
			final List<WebDAVProperty> properties=propFind();	//get properties for this resource, which will cache the properties along with the existence and collection states
			return WebDAVUtilities.isCollection(properties);	//send back whether the resource is a collection
		}
		catch(final HTTPNotFoundException notFoundException)	//404 Not Found
		{
			return false;	//a resource that doesn't exist isn't a collection
		}
		catch(final HTTPGoneException goneException)	//ignore 410 Gone
		{
			return false;	//a resource that doesn't exist isn't a collection
		}
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
		final WebDAVRequest request=new DefaultWebDAVRequest(COPY_METHOD, getURI());	//create a COPY request
		request.setDestination(destinationURI);	//set the destination URI
		checkInstance(depth, "Depth cannot be null.");
		if(depth!=Depth.ZERO && depth!=Depth.INFINITY)	//if the depth is not ZERO or INFINITY
		{
			throw new IllegalArgumentException("Depth of "+depth+" is not allowed for the "+COPY_METHOD+" method.");
		}
		request.setDepth(depth);	//set the depth
		request.setOverwrite(overwrite);	//set the overwrite option
		final HTTPResponse response=sendRequest(request);	//get the response
		uncacheInfo(destinationURI);	//remove the cache information of the destination, because a copy of this resource replaced it
	}

	/**Creates a collection using the MKCOL method.
	@exception IOException if there was an error invoking the method.
	@see #mkCols()
	*/
	public void mkCol() throws IOException
	{
		final WebDAVRequest request=new DefaultWebDAVRequest(MKCOL_METHOD, getURI());	//create a MKCOL request
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
		mkCols(changeRawPath(getURI(), ROOT_PATH));	//make the collections starting from the root
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
		final String referenceURIString=getURI().toString();	//get the string version of the resource's reference URI
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
	The cached information is cleared.
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
	The cached information is cleared.
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
		final WebDAVRequest request=new DefaultWebDAVRequest(MOVE_METHOD, getURI());	//create a MOVE request
		request.setDestination(destinationURI);	//set the destination URI
		request.setDepth(Depth.INFINITY);	//set the depth to infinity
		request.setOverwrite(overwrite);	//set the overwrite option
		final HTTPResponse response=sendRequest(request);	//get the response
		uncacheInfo();	//remove the cache information, because this resource is moving
		uncacheInfo(destinationURI);	//remove the cache information of the destination, because this resource replaced it
	}
		
	/**Retrieves properties using the {@value WebDAVConstants#PROPFIND_METHOD} method.
	Cached properties are never used for any depth except {@link Depth#ZERO}, although cached properties are updated if caching is enabled.	
	The URI of each resource is canonicized to be an absolute URI.
	Returned property values may be <code>null</code>.
	@param depth The requested depth.
	@return A list of all properties of all requested resources, each representing the URI of the resource paired by a list of its properties.
	@exception IOException if there was an error invoking the method.
	*/
	public List<NameValuePair<URI, List<WebDAVProperty>>> propFind(final Depth depth) throws IOException
	{
		final HTTPClient httpClient=getClient();	//get the client we are using
		final URI referenceURI=getURI();	//get the reference URI of this resource
			//return cached values if we can
		if(depth==Depth.ZERO && isCached())	//if we're caching values and a depth of zero is requested
		{
			final CacheKey cacheKey=new CacheKey(httpClient, referenceURI);	//create a new cache key
			final CachedProperties cachedProperties=cachedPropertiesMap.get(cacheKey);	//get cached properties from the map
			if(cachedProperties!=null && !cachedProperties.isStale())	//if we have cached properties that is not stale
			{
				final List<NameValuePair<URI, List<WebDAVProperty>>> cachedPropFindList=new ArrayList<NameValuePair<URI, List<WebDAVProperty>>>();
				cachedPropFindList.add(new NameValuePair<URI, List<WebDAVProperty>>(referenceURI, cachedProperties.getProperties()));	//add the property list for this resource to the list, paired with its URI TODO make sure it doesn't hurt to use our own URI---will forwarding affect this?
				return cachedPropFindList;	//return the manufactured property list from our cached properyy list
			}
		}
		try
		{
			final WebDAVRequest request=new DefaultWebDAVRequest(PROPFIND_METHOD, referenceURI);	//create a PROPFIND request
			request.setDepth(depth);	//set the requested depth
			final WebDAVXMLGenerator webdavXMLGenerator=new WebDAVXMLGenerator();	//create a WebDAV XML generator
			final Document propfindDocument=webdavXMLGenerator.createPropfindDocument();	//create a propfind document	//TODO check DOM exceptions here
			webdavXMLGenerator.addPropertyNames(propfindDocument.getDocumentElement(), ALL_PROPERTIES);	//show that we want to know about all properties
			request.setXML(propfindDocument);	//set the XML in the body of our request
			final HTTPResponse response=sendRequest(request);	//get the response
			//TODO check response; expect 207 Multi-Status
			final Document document=response.getXML(true, false);	//get the XML from the response body, aware of namespaces but not validating
			if(document!=null)	//if there was an XML document in the request
			{
				final Element documentElement=document.getDocumentElement();	//get the document element
					//TODO check to make sure the document element is correct
				final List<NameValuePair<URI, List<WebDAVProperty>>> propertyLists=WebDAVXMLProcessor.getMultistatusProperties(documentElement, referenceURI);	//get the properties from the multistatus document, relative to this resource URI			
				if(isCached())	//if we're caching information, cache the properties for this resource
				{
					cacheLock.writeLock().lock();	//lock the cache for writing
					try
					{
						for(final NameValuePair<URI, List<WebDAVProperty>> propertyList:propertyLists)	//look at each property list
						{
							final List<WebDAVProperty> properties=propertyList.getValue();	//cache the list of properties for this resource
							final boolean isCollection=WebDAVUtilities.isCollection(properties);	//see if this resource is a collection
							final CacheKey cacheKey=new CacheKey(httpClient, propertyList.getName());	//create a key for the caches
							cachedExistsMap.put(cacheKey, new CachedExists(true));	//show that this resource exists
							cachedPropertiesMap.put(cacheKey, new CachedProperties(properties, isCollection));	//cache the properties for this resource
						}
					}
					finally
					{
						cacheLock.writeLock().unlock();	//always release the write lock
					}					
				}
				return propertyLists;	//return all the properties requested
			}
			return emptyList();	//return an empty list, because there was no XML returned
		}
		catch(final HTTPNotFoundException notFoundException)	//404 Not Found
		{
			if(isCached())	//if we are caching information
			{
				cacheExists(false);	//indicate that the resource is missing
			}
			throw notFoundException;	//rethrow the exception
		}
		catch(final HTTPGoneException goneException)	//410 Gone
		{
			if(isCached())	//if we are caching information
			{
				cacheExists(false);	//indicate that the resource is permanently missing
			}
			throw goneException;	//rethrow the exception
		}
		catch(final ParserConfigurationException parserConfigurationException)
		{
			throw (IOException)new IOException(parserConfigurationException.getMessage()).initCause(parserConfigurationException);
		}
		catch(final SAXException saxException)
		{
			throw (IOException)new IOException(saxException.getMessage()).initCause(saxException);			
		}
	}

	/**Retrieves properties of this resource using the PROPFIND method.
	This is a convenience method for <code>propFind(Depth)</code>.
	Cached properties are used if possible.
	The cached property list is updated.
	The URI of each resource is canonicized to be an absolute URI.
	Returned property values may be <code>null</code>.
	@return A list of all properties of this resource.
	@exception IOException if there was an error invoking the method.
	@see #propFind(Depth)
	*/
	public List<WebDAVProperty> propFind() throws IOException
	{
		final URI referenceURI=getURI();	//get the reference URI of this resource
		final List<NameValuePair<URI, List<WebDAVProperty>>> propertyLists=propFind(Depth.ZERO);	//do a propfind with no depth
		for(final NameValuePair<URI, List<WebDAVProperty>> propertyList:propertyLists)	//look at each property list
		{
			if(propertyList.getName().equals(referenceURI))	//if this property list is for this resource
			{
				return propertyList.getValue();	//return the list of properties for this resource
			}
		}
		return emptyList();	//return an empty list; for some reason, no properties were returned		
	}

	/**Removes properties using the PROPPATCH method.
	If properties are being removed and others are being set at the same time, the {@link #propPatch(Collection, Collection)} method should be used.
	The cached information is cleared.
	The URI of each resource is canonicized to be an absolute URI.
	@param removeProperties The list of properties to remove.
	@exception IOException if there was an error invoking the method.
	*/
	public void removeProperties(final Collection<WebDAVPropertyName> removePropertyNames) throws IOException
	{
		propPatch(removePropertyNames, (Set<WebDAVProperty>)EMPTY_SET);	//perform a PROPPATCH with no properties to set		
	}

	/**Sets properties using the PROPPATCH method.
	If properties are being removed and others are being set at the same time, the {@link #propPatch(Collection, Collection)} method should be used.
	The cached information is cleared.
	The URI of each resource is canonicized to be an absolute URI.
	@param setPropertyNames The list of properties and values to set.
	@exception IOException if there was an error invoking the method.
	*/
	public void setProperties(final Collection<WebDAVProperty> setProperties) throws IOException
	{
		propPatch((Set<WebDAVPropertyName>)EMPTY_SET, setProperties);	//perform a PROPPATCH with no properties to remove
	}

	/**Updates properties using the PROPPATCH method.
	Rquested properties will first be removed, then requested properties will be set, in that order.
	The cached information is cleared.
	The URI of each resource is canonicized to be an absolute URI.
	@param removeProperties The list of properties to remove.
	@param setPropertyNames The list of properties and values to set.
	@exception IOException if there was an error invoking the method.
	*/
	public void propPatch(final Collection<WebDAVPropertyName> removePropertyNames, final Collection<WebDAVProperty> setProperties) throws IOException
	{
		final URI referenceURI=getURI();	//get the reference URI of this resource
		uncacheInfo();	//empty the cache
		final WebDAVRequest request=new DefaultWebDAVRequest(PROPPATCH_METHOD, referenceURI);	//create a PROPPATCH request
		final WebDAVXMLGenerator webdavXMLGenerator=new WebDAVXMLGenerator();	//create a WebDAV XML generator
		final Document propertyupdateDocument=webdavXMLGenerator.createPropertyupdateDocument();	//create a propertyupdate document	//TODO check DOM exceptions here
		final Element propertyupdateElement=propertyupdateDocument.getDocumentElement();	//get the document element
		for(final WebDAVPropertyName removePropertyName:removePropertyNames)	//for each property to remove
		{
			final Element removeElement=webdavXMLGenerator.addRemove(propertyupdateElement);	//add a remove element
			final Element propElement=webdavXMLGenerator.addProp(removeElement);	//add a property element
			webdavXMLGenerator.addPropertyName(propElement, removePropertyName);	//add the property name for removal
		}
		for(final WebDAVProperty setProperty:setProperties)	//for each property to set
		{
			final Element setElement=webdavXMLGenerator.addSet(propertyupdateElement);	//add a set element
			final Element propElement=webdavXMLGenerator.addProp(setElement);	//add a property element
			webdavXMLGenerator.addProperty(propElement, setProperty);	//add the property
		}
		request.setXML(propertyupdateDocument);	//set the XML in the body of our request
		final HTTPResponse response=sendRequest(request);	//get the response
			//TODO check response; expect 207 Multi-Status
	}

	/**Property information stored in a WebDAV resource cache.
	@author Garret Wilson
	*/
	protected static class CachedProperties extends AbstractCachedInfo
	{
		/**The cached list of properties.*/
		private final List<WebDAVProperty> properties;

			/**@return The cached list of properties.*/
			public final List<WebDAVProperty> getProperties() {return properties;}

		/**The cached record of whether the resource is a collection.*/
		private final boolean isCollection;

			/**@return The cached record of whether the resource is a collection.*/
			public boolean isCollection() {return isCollection;}

		/**Constructor.
		@param properties The list of properties to cache.
		@param isCollection Whether the resource is a collection.
		@exception NullPointerException if the given list of properties is <code>null</code>.
		*/
		public CachedProperties(final List<WebDAVProperty> properties, final boolean isCollection)
		{
			this.isCollection=isCollection;	//save the cached collection state
			this.properties=unmodifiableList(checkInstance(properties, "Properties cannot be null."));
		}
	}

}
