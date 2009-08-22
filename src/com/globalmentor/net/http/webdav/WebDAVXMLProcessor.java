/*
 * Copyright © 1996-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.globalmentor.net.http.webdav;

import java.net.*;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import com.globalmentor.log.Log;
import static com.globalmentor.net.http.webdav.WebDAV.*;
import static com.globalmentor.text.xml.XML.*;
import com.globalmentor.collections.DecoratorIDedMappedList;
import com.globalmentor.model.NameValuePair;
import com.globalmentor.util.*;

import org.w3c.dom.*;

/**Class to process XML containing serialized information from WebDAV as defined by
<a href="http://www.ietf.org/rfc/rfc2518.txt">RFC 2518</a>,	"HTTP Extensions for Distributed Authoring -- WEBDAV".
This class is not thread safe.
@author Garret Wilson
*/
public class WebDAVXMLProcessor
{

	/**Retrieves a list of properties parsed from the children of the given XML element, such as a <code>D:propfind</code> element.
	The {@value WebDAV#ELEMENT_ALLPROP} and {@value WebDAV#ELEMENT_PROPNAME} conditions are supported.
	@param request The HTTP request in response to which properties are being retrieved.
	@param element The XML element parent of the property list.
	@return A list of all requested properties, or {@link WebDAV#ALL_PROPERTIES} or
		{@link WebDAV#PROPERTY_NAMES} indicating all properties or all property names, respectively.
	@see WebDAV#ALL_PROPERTIES
	@see WebDAV#PROPERTY_NAMES
	*/
	public static DecoratorIDedMappedList<URI, WebDAVPropertyName> getPropfindProperties(final HttpServletRequest request, final Element element)
	{
		final DecoratorIDedMappedList<URI, WebDAVPropertyName> propertyList=new DecoratorIDedMappedList<URI, WebDAVPropertyName>(new HashMap<URI, WebDAVPropertyName>(), new ArrayList<WebDAVPropertyName>());	//create a list of WebDAV property names
		final NodeList childList=element.getChildNodes();	//get a list of element children
		for(int childIndex=0; childIndex<childList.getLength(); ++childIndex)	//look at each child node
		{
			final Node childNode=childList.item(childIndex);	//get this child node
			if(childNode.getNodeType()==Node.ELEMENT_NODE)	//if this is an element
			{
				if(WEBDAV_NAMESPACE.equals(childNode.getNamespaceURI()))	//if this is a WebDAV element
				{
					final String childLocalName=childNode.getLocalName();	//get the child element's local name
					if(ELEMENT_PROP.equals(childLocalName))	//D:prop
					{
						final NodeList propertyChildList=childNode.getChildNodes();	//get a list of property element children
						for(int propertyChildIndex=0; propertyChildIndex<propertyChildList.getLength(); ++propertyChildIndex)	//look at each property child node
						{
							final Node propertyChildNode=propertyChildList.item(propertyChildIndex);	//get this property child node
							if(propertyChildNode.getNodeType()==Node.ELEMENT_NODE)	//if this is an element
							{
								propertyList.add(new WebDAVPropertyName(propertyChildNode.getNamespaceURI(), propertyChildNode.getLocalName()));	//add the property to the list
							}
						}
					}
					else if(ELEMENT_ALLPROP.equals(childLocalName))	//allprop
					{
						return ALL_PROPERTIES;	//show that all properties were requested
					}
					else if(ELEMENT_PROPNAME.equals(childLocalName))	//propname
					{
						return PROPERTY_NAMES;	//show that properties names were requested
					}
				}
			}
		}
		return propertyList;	//return our list of properties
	}

	/**Retrieves maps of property values parsed from the children of the given <code>D:multistatus</code> element.
	Each name-value pair in the list represents the URI of a resource along with a map of its properties.
	@param element The XML element parent of the resource and property list designations.
	@param baseURI The base URI relative to which the URI references should be resolved, or <code>null</code> if the URI references should be returned as provided by the server.
	@return A list of all properties of all included resources, each representing the URI of the resource paired by a map of its properties. 
	*/
	public static List<NameValuePair<URI, Map<WebDAVPropertyName, WebDAVProperty>>> getMultistatusProperties(final Element element, final URI baseURI)
	{
//	TODO del Log.trace("looking at multistatus response");
		final List<NameValuePair<URI, Map<WebDAVPropertyName, WebDAVProperty>>> resourcesPropertyMaps=new ArrayList<NameValuePair<URI, Map<WebDAVPropertyName, WebDAVProperty>>>();	//create a list of resource URIs associated with property maps
		final NodeList childList=element.getChildNodes();	//get a list of element children
		for(int childIndex=0; childIndex<childList.getLength(); ++childIndex)	//look at each child node
		{
			final Node childNode=childList.item(childIndex);	//get this child node
			if(childNode.getNodeType()==Node.ELEMENT_NODE && WEBDAV_NAMESPACE.equals(childNode.getNamespaceURI()) && ELEMENT_RESPONSE.equals(childNode.getLocalName()))	//D:response
			{
//			TODO del Log.trace("found response element");
				final NameValuePair<URI, Map<WebDAVPropertyName, WebDAVProperty>> resourceProperties=getResponseProperties((Element)childNode, baseURI);	//get the resource URI and properties
				if(resourceProperties!=null)	//if we got a resource and properties
				{
					resourcesPropertyMaps.add(resourceProperties);	//add this resource properties pair to the list
				}
			}
		}
		return resourcesPropertyMaps;	//return the maps of properties
	}
	
	/**Retrieves a map of WebDAV properties parsed from the children of the given <code>D:response</code> element.
	If a base URI is specified, the URI references will be resolved to the base URI to compensate for servers that return relative URIs.
	@param element The XML element parent of the resource and property list designations.
	@param baseURI The base URI relative to which the URI references should be resolved, or <code>null</code> if the URI references should be returned as provided by the server.
	@return A name-value pair of the resource's URI, along with a map of its properties, or <code>null</code> if no resource URI was found.
	@see <a href="http://www.webdav.org/mod_dav/#imp">mod_dav implementation details</a>
	*/
	public static NameValuePair<URI, Map<WebDAVPropertyName, WebDAVProperty>> getResponseProperties(final Element element, final URI baseURI)
	{
		URI uri=null;	//we'll try to determine the URI of the resource
		final Map<WebDAVPropertyName, WebDAVProperty> propertyMap=new HashMap<WebDAVPropertyName, WebDAVProperty>();	//create a map to hold the properties
		final NodeList childList=element.getChildNodes();	//get a list of element children
		final int childCount=childList.getLength();	//find out how many children there are
		for(int childIndex=0; childIndex<childCount; ++childIndex)	//look at each child node
		{
			final Node childNode=childList.item(childIndex);	//get this child node
			if(childNode.getNodeType()==Node.ELEMENT_NODE)	//if this is an element
			{
				final Element childElement=(Element)childNode;	//get a reference to this element
				if(WEBDAV_NAMESPACE.equals(childElement.getNamespaceURI()))	//if this is a WebDAV element
				{
					final String childLocalName=childElement.getLocalName();	//get the child element's local name
					if(ELEMENT_HREF.equals(childLocalName) && uri==null)	//D:href (only look at the first one)
					{
						final String uriString=getText(childElement, false);	//get the URI
						try
						{
							uri=new URI(uriString);	//create a URI from the value
							if(baseURI!=null)	//if we have a base URI
							{
								uri=baseURI.resolve(uri);	//resolve the URI to the base URI to compensate for servers that return relative URIs
							}
						}
						catch(final URISyntaxException uriSyntaxException)	//if the URI is not in proper form
						{
							Log.warn(uriSyntaxException);	//TODO return an appropriate error
						}
					}
					else if(ELEMENT_PROPSTAT.equals(childLocalName))	//D:propstat
					{
						propertyMap.putAll(getPropstatProperties(childElement));	//get the properties and add them to our map (there can be multiple propstat properties) TODO later perhaps group the propstat properties, each of which can have a status
					}
/*TODO fix for D:status
					else	//if we don't understand the response child element
					{
						Log.warn("Unexpected response child element "+childLocalName);
					}
*/
				}
			}
		}
		if(uri!=null)	//if we found a URI
		{
			return new NameValuePair<URI, Map<WebDAVPropertyName, WebDAVProperty>>(uri, propertyMap);	//return the URI and its associated property map
		}
		return null;	//show that no URI was found
	}
	
	/**Retrieves a map of properties parsed from the children of the given XML element, such as a <code>D:propstat</code> element.
	@param element The XML element parent of the property list.
	@return A map of all requested properties. 
	*/
	public static Map<WebDAVPropertyName, WebDAVProperty> getPropstatProperties(final Element element)
	{
//TODO del Log.trace("getting propstat properties");
		final Map<WebDAVPropertyName, WebDAVProperty> propertyMap=new HashMap<WebDAVPropertyName, WebDAVProperty>();	//create a map to hold the properties
		final NodeList childList=element.getChildNodes();	//get a list of element children
		for(int childIndex=0; childIndex<childList.getLength(); ++childIndex)	//look at each child node
		{
			final Node childNode=childList.item(childIndex);	//get this child node
			if(childNode.getNodeType()==Node.ELEMENT_NODE)	//if this is an element
			{
				if(WEBDAV_NAMESPACE.equals(childNode.getNamespaceURI()))	//if this is a WebDAV element
				{
					final String childLocalName=childNode.getLocalName();	//get the child element's local name
					if(ELEMENT_PROP.equals(childLocalName))	//D:prop
					{
//TODO del Log.trace("found D:prop");
						final NodeList propertyChildList=childNode.getChildNodes();	//get a list of property element children
						final int propertyChildCount=propertyChildList.getLength();	//find out how many property children there are
						for(int propertyChildIndex=0; propertyChildIndex<propertyChildCount; ++propertyChildIndex)	//look at each property child node
						{
							final Node propertyChildNode=propertyChildList.item(propertyChildIndex);	//get this property child node
							if(propertyChildNode.getNodeType()==Node.ELEMENT_NODE)	//if this is an element
							{
								final WebDAVProperty property=getProperty((Element)propertyChildNode);	//parse this property
								propertyMap.put(property.getName(), property);	//add the property to our map
							}
						}
					}
					//TODO check for and deal with D:status
				}
			}
		}
		//TODO check the status element
		return propertyMap;	//return our map of properties
	}
	
	/**Retrieves a WebDAV property from an element that is a direct <code>D:prop</code>.
	@param element The XML element that is a child of the <code>D:prop</code> element.
	@return A WebDAV property indicating the qualified name of the property and the value, which is an XML document fragment.
	*/
	public static WebDAVProperty getProperty(final Element element)
	{
		final WebDAVPropertyValue webdavPropertyValue;	//we'll determine the value of the WebDAV property
		if(element.getChildNodes().getLength()>0)	//if there are child nodes
		{
			if(getChildNodeNot(element, Node.TEXT_NODE)==null)	//if all child nodes are text nodes
			{
				webdavPropertyValue=new WebDAVLiteralPropertyValue(getText(element));	//use all the text as a literal WebDAV property value
			}
			else	//if there non-text child elements, gather all the child elements as is into a document fragment
			{
				final DocumentFragment documentFragment=extractChildren(element);	//extract the children of the element to a document fragment
				webdavPropertyValue=new WebDAVDocumentFragmentPropertyValue(documentFragment);	//create a WebDAV property value from the document fragment
			}
		}
		else	//if there are no child nodes
		{
			webdavPropertyValue=null;	//indicate that the WebDAV property value is null
		}
		return new WebDAVProperty(new WebDAVPropertyName(element.getNamespaceURI(), element.getLocalName()), webdavPropertyValue);	//create and return a new WebDAV property 
	}

}
