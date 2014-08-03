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

import java.net.URI;

import static com.globalmentor.java.Objects.*;

import com.globalmentor.model.IDed;
import com.globalmentor.net.DefaultResource;

/**
 * The name of a WebDAV property, consisting of a property namespace and a property local name. The reference URI indicates the concatenation of the namespace
 * and the local name.
 * <p>
 * This class does not currently use true URIs for namespaces, primarily because WebDAV uses a namespace ("DAV:") which is not a true URI.
 * </p>
 * @author Garret Wilson
 */
public class WebDAVPropertyName extends DefaultResource implements IDed<URI> {

	/** The property namespace. */
	private final String namespace;

	/** @return The property namespace. */
	public String getNamespace() {
		return namespace;
	}

	/** @return The unique identifier of the object. */
	public URI getID() {
		return getURI();
	}

	/** The property local name. */
	private final String localName;

	/** @return The property local name. */
	public String getLocalName() {
		return localName;
	}

	/**
	 * Namespace and local name constructor. Both components are expected to be already encoded for inclusion in a URI.
	 * @param namespace The property namespace.
	 * @param localName The property local name.
	 * @throws NullPointerException if the given namespace and/or local name is <code>null</code>.
	 */
	public WebDAVPropertyName(final String namespace, final String localName) {
		super(createPropertyURI(namespace, localName)); //create the property URI by combining the property namespace and the property local name
		this.namespace = namespace;
		this.localName = localName;
	}

	/**
	 * Namespace URI and local name constructor. The local name is expected to be already encoded for inclusion in a URI.
	 * @param namespaceURI The property namespace URI.
	 * @param localName The property local name.
	 * @throws NullPointerException if the given namespace URI and/or local name is <code>null</code>.
	 */
	public WebDAVPropertyName(final URI namespaceURI, final String localName) {
		this(namespaceURI.toString(), localName); //create the property name using the string form of the namespace URI
	}

	/** @return A hash code for this object. */
	public int hashCode() {
		return 31 * getNamespace().hashCode() + getLocalName().hashCode();
	}

	/**
	 * Determines if this object is equal to another object. This implementation considers another object equal if it is another WebDAV property name with the
	 * same namespace and local name.
	 */
	public boolean equals(final Object object) {
		if(this == object) { //identical objects are always equal
			return true;
		}
		if(!(object instanceof WebDAVPropertyName)) {
			return false;
		}
		final WebDAVPropertyName webdavPropertyName = (WebDAVPropertyName)object;
		return getNamespace().equals(webdavPropertyName.getNamespace()) && getLocalName().equals(webdavPropertyName.getLocalName());
	}

	/**
	 * Creates a WebDAV reference URI from a property namespace and local name. Both components are expected to be already encoded for inclusion in a URI.
	 * @param propertyNamespace The WebDAV property namespace.
	 * @param propertyLocalName The WebDAV property namespace.
	 * @return A reference URI constructed from the given namespace and local name.
	 * @throws NullPointerException if the given property namespace and/or property local name is <code>null</code>.
	 */
	public static URI createPropertyURI(final String propertyNamespace, final String propertyLocalName) {
		return URI.create(checkInstance(propertyNamespace, "Property namespace cannot be null.")
				+ checkInstance(propertyLocalName, "Property local name cannot be null.")); //concatenate the property namespace and local name after ensuring they are not null
	}
}
