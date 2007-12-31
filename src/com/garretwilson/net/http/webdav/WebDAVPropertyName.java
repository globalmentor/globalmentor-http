package com.garretwilson.net.http.webdav;

import java.net.URI;

import static com.garretwilson.lang.Objects.*;

import com.garretwilson.net.DefaultResource;
import com.garretwilson.util.IDable;

/**The name of a WebDAV property, consisting of a property namespace and a property local name.
The reference URI indicates the concatenation of the namespace and the local name.
<p>This class does not currently use true URIs for namespaces, primarily because WebDAV uses a namespace ("DAV:") which is not a true URI.</p>
@author Garret Wilson
*/
public class WebDAVPropertyName extends DefaultResource implements IDable<URI>
{

	/**The property namespace.*/
	private final String namespace;

		/**@return The property namespace.*/
		public String getNamespace() {return namespace;}

		/**@return The unique identifier of the object.*/
		public URI getID() {return getURI();}

	/**The property local name.*/
	private final String localName;

		/**@return The property local name.*/
		public String getLocalName() {return localName;}

	/**Namespace and local name constructor.
	Both components are expected to be already encoded for inclusion in a URI.
	@param namespace The property namespace.
	@param localName The property local name.
	@exception NullPointerException if the given namespace and/or local name is <code>null</code>.
	*/
	public WebDAVPropertyName(final String namespace, final String localName)
	{
		super(createPropertyURI(namespace, localName));	//create the property URI by combining the property namespace and the property local name
		this.namespace=namespace;
		this.localName=localName;
	}

	/**Namespace URI and local name constructor.
	The local name is expected to be already encoded for inclusion in a URI.
	@param namespaceURI The property namespace URI.
	@param localName The property local name.
	@exception NullPointerException if the given namespace URI and/or local name is <code>null</code>.
	*/
	public WebDAVPropertyName(final URI namespaceURI, final String localName)
	{
		this(namespaceURI.toString(), localName);	//create the property name using the string form of the namespace URI
	}

	/**Creates a WebDAV reference URI from a property namespace and local name.
	Both components are expected to be already encoded for inclusion in a URI.
	@param propertyNamespace The WebDAV property namespace.
	@param propertyLocalName The WebDAV property namespace.
	@return A reference URI constructed from the given namespace and local name.
	@exception NullPointerException if the given property namespace and/or property local name is <code>null</code>.
	*/
	public static URI createPropertyURI(final String propertyNamespace, final String propertyLocalName)
	{
		return URI.create(checkInstance(propertyNamespace, "Property namespace cannot be null.")+checkInstance(propertyLocalName, "Property local name cannot be null."));	//concatenate the property namespace and local name after ensuring they are not null
	}
}
