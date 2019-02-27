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
import java.util.*;

import javax.xml.parsers.DocumentBuilder;

import static com.globalmentor.net.http.webdav.WebDAV.*;
import static com.globalmentor.xml.XML.*;
import static com.globalmentor.xml.spec.XML.*;

import com.globalmentor.model.ConfigurationException;
import com.globalmentor.xml.XMLNamespacePrefixManager;
import com.globalmentor.xml.spec.XML;

import org.w3c.dom.*;

/**
 * Class to generate XML trees for WebDAV as defined by <a href="http://www.ietf.org/rfc/rfc2518.txt">RFC 2518</a>, "HTTP Extensions for Distributed Authoring
 * -- WEBDAV". This class is not thread safe.
 * @author Garret Wilson
 */
public class WebDAVXMLGenerator extends XMLNamespacePrefixManager {

	/**
	 * Creates and returns a default document builder for WebDAV, with namespace awareness but no validation.
	 * @return A new XML document builder for handling WebDAV XML content.
	 * @throws ConfigurationException if a document builder cannot be created which satisfies the configuration requested.
	 */
	private static DocumentBuilder createWebDAVDocumentBuilder() {
		return createDocumentBuilder(true); //create a document builder with namespace awareness
	}

	/** The document builder used as an XML document factory. */
	private final DocumentBuilder documentBuilder;

	/** @return The document builder used as an XML document factory. */
	public DocumentBuilder getDocumentBuilder() {
		return documentBuilder;
	}

	/** Default constructor. */
	public WebDAVXMLGenerator() {
		this.documentBuilder = createWebDAVDocumentBuilder(); //create the WebDAV document builder
	}

	/**
	 * Creates an XML document representing propfind.
	 * @return A new XML document representing propfind.
	 * @throws DOMException if there is an error creating the document.
	 */
	public Document createPropfindDocument() throws DOMException {
		return getDocumentBuilder().getDOMImplementation().createDocument(WEBDAV_NAMESPACE, createQName(getNamespacePrefix(WEBDAV_NAMESPACE), ELEMENT_PROPFIND),
				null); //create a propfind document
	}

	/**
	 * Creates an XML document representing propertyupdate.
	 * @return A new XML document representing propertyupdate.
	 * @throws DOMException if there is an error creating the document.
	 */
	public Document createPropertyupdateDocument() throws DOMException {
		return getDocumentBuilder().getDOMImplementation().createDocument(WEBDAV_NAMESPACE,
				createQName(getNamespacePrefix(WEBDAV_NAMESPACE), ELEMENT_PROPERTYUPDATE), null); //create a propertyupdate document
	}

	/**
	 * Creates an XML document representing multistatus.
	 * @return A new XML document representing multistatus.
	 * @throws DOMException if there is an error creating the document.
	 */
	public Document createMultistatusDocument() throws DOMException {
		return getDocumentBuilder().getDOMImplementation().createDocument(WEBDAV_NAMESPACE, createQName(getNamespacePrefix(WEBDAV_NAMESPACE), ELEMENT_MULTISTATUS),
				null); //create a multistatus document
	}

	/**
	 * Creates a response and appends it to the given element.
	 * @param element An XML element representing a multistatus.
	 * @return A WebDAV response XML element.
	 * @throws DOMException if there is an error creating the element.
	 */
	public Element addResponse(final Element element) throws DOMException {
		return appendElementNS(element, WEBDAV_NAMESPACE, createQName(getNamespacePrefix(WEBDAV_NAMESPACE), ELEMENT_RESPONSE)); //create a response element
	}

	/**
	 * Creates an href element with the URI as its content and adds it to the given element.
	 * @param element An XML element representing, for example, a response.
	 * @param uri The URI to use as the href.
	 * @return A WebDAV href XML element with the URI as its content.
	 * @throws DOMException if there is an error creating the element.
	 */
	public Element addHref(final Element element, final URI uri) throws DOMException {
		//create an href element with the URI as its content and append it to the given element
		return appendElementNS(element, WEBDAV_NAMESPACE, createQName(getNamespacePrefix(WEBDAV_NAMESPACE), ELEMENT_HREF), uri.toString());
	}

	/**
	 * Creates a property container element.
	 * @param element An XML element representing, for example, a response.
	 * @return A WebDAV propstat XML element.
	 * @throws DOMException if there is an error creating the element.
	 */
	public Element addPropstat(final Element element) throws DOMException {
		//create a propstat element and append it to the given element
		return appendElementNS(element, WEBDAV_NAMESPACE, createQName(getNamespacePrefix(WEBDAV_NAMESPACE), ELEMENT_PROPSTAT));
	}

	/**
	 * Creates a property element.
	 * @param element An XML element representing, for example, a property container.
	 * @return A WebDAV prop XML element.
	 * @throws DOMException if there is an error creating the element.
	 */
	public Element addProp(final Element element) throws DOMException {
		return appendElementNS(element, WEBDAV_NAMESPACE, createQName(getNamespacePrefix(WEBDAV_NAMESPACE), ELEMENT_PROP)); //create a prop element and append it to the given element
	}

	/**
	 * Adds property requests to a property element.
	 * @param propElement An XML element representing a property element.
	 * @param propertyList A list of all requested properties, or {@link WebDAV#ALL_PROPERTIES} or {@link WebDAV#PROPERTY_NAMES} indicating all properties or all
	 *          property names, respectively.
	 * @throws DOMException if there is an error creating the child elements.
	 * @see WebDAV#ALL_PROPERTIES
	 * @see WebDAV#PROPERTY_NAMES
	 */
	public void addPropertyNames(final Element propElement, final List<WebDAVPropertyName> propertyList) throws DOMException {
		if(ALL_PROPERTIES == propertyList) { //if all properties are requested
			addPropertyName(propElement, new WebDAVPropertyName(WEBDAV_NAMESPACE, ELEMENT_ALLPROP)); //add the D:allprop element
		} else if(PROPERTY_NAMES == propertyList) { //if all property names are requested
			addPropertyName(propElement, new WebDAVPropertyName(WEBDAV_NAMESPACE, ELEMENT_PROPNAME)); //add the D:propname element
		} else { //for normal property lists
			for(final WebDAVPropertyName property : propertyList) { //for each requested property
				addPropertyName(propElement, property); //add the property element
			}
		}
	}

	/**
	 * Adds a property and a value to a property element.
	 * @param propElement An XML element representing a property element.
	 * @param property A property URI and its value, may be <code>null</code> to indicate that no content should be added.
	 * @throws DOMException if there is an error creating the child elements.
	 * @return A WebDAV property XML element with its serialized value.
	 */
	public Element addProperty(final Element propElement, final WebDAVProperty property) throws DOMException {
		final WebDAVPropertyName propertyName = property.getName(); //get the property name
		final String propertyNamespace = propertyName.getNamespace(); //get the property namespace
		//create a property element and append it to the given prop element
		final Element propertyElement = appendElementNS(propElement, propertyNamespace,
				createQName(getNamespacePrefix(propertyNamespace), propertyName.getLocalName()));
		final WebDAVPropertyValue value = property.getValue(); //get the value of the property
		if(value != null) { //if there is a value
			if(value instanceof WebDAVDocumentFragmentPropertyValue) { //if this is a document fragment value
				appendImportedChildNodes(propertyElement, ((WebDAVDocumentFragmentPropertyValue)value).getDocumentFragment()); //import and append all child nodes of the document fragment
			} else if(value instanceof WebDAVLiteralPropertyValue) { //if this is a literal value
				appendText(propertyElement, ((WebDAVLiteralPropertyValue)value).toString()); //add the literal text to the property element
			} else { //if we don't recognize the WebDAV property type
				throw new AssertionError("Unrecognized WebDAV property value type: " + value);
			}
		}
		return propertyElement; //return the property element
	}

	/**
	 * Adds a property request to a property element.
	 * @param propElement An XML element representing a property element.
	 * @param propertyName The WebDAV property name of the property to add.
	 * @throws DOMException if there is an error creating the child elements.
	 * @return A WebDAV property XML element.
	 */
	public Element addPropertyName(final Element propElement, final WebDAVPropertyName propertyName) throws DOMException {
		final String propertyNamespace = propertyName.getNamespace(); //get the property namespace
		//create a property element and append it to the given prop element
		return appendElementNS(propElement, propertyNamespace, createQName(getNamespacePrefix(propertyNamespace), propertyName.getLocalName()));
	}

	/**
	 * Creates a remove element.
	 * @param element An XML element representing, for example, a property removal as a child of a propertyupdate document element.
	 * @return A WebDAV remove XML element.
	 * @throws DOMException if there is an error creating the element.
	 */
	public Element addRemove(final Element element) throws DOMException {
		return appendElementNS(element, WEBDAV_NAMESPACE, createQName(getNamespacePrefix(WEBDAV_NAMESPACE), ELEMENT_REMOVE)); //create a remove element and append it to the given element
	}

	/**
	 * Creates a set element.
	 * @param element An XML element representing, for example, a property setting as a child of a propertyupdate document element.
	 * @return A WebDAV set XML element.
	 * @throws DOMException if there is an error creating the element.
	 */
	public Element addSet(final Element element) throws DOMException {
		return appendElementNS(element, WEBDAV_NAMESPACE, createQName(getNamespacePrefix(WEBDAV_NAMESPACE), ELEMENT_SET)); //create a set element and append it to the given element
	}

	/**
	 * Creates a status element with the status text as its content and adds it to the given element.
	 * @param element An XML element representing, for example, a propstat.
	 * @param status The text of the status report.
	 * @return A WebDAV status XML element with the status text as its content.
	 * @throws DOMException if there is an error creating the element.
	 */
	public Element addStatus(final Element element, final String status) throws DOMException {
		//create a status element with the status text as its content and append it to the given element
		return appendElementNS(element, WEBDAV_NAMESPACE, createQName(getNamespacePrefix(WEBDAV_NAMESPACE), ELEMENT_STATUS), status);
	}

	/**
	 * Creates and adds a resource type element with a resource type child element. Properties in the WebDAV namespace will have the correct prefix determined.
	 * @param element An XML element representing, for example, a property.
	 * @param typeNamespace The namespace of the property, or <code>null</code> to indicate no type.
	 * @param typeLocalName The local name of the property, or <code>null</code> to indicate no type.
	 * @return A WebDAV resource type XML element with the optional type indicated by a child element.
	 * @throws DOMException if there is an error creating the elements.
	 */
	public Element addResourceType(final Element element, final String typeNamespace, final String typeLocalName) throws DOMException {
		//create and append a resource type element
		final Element resourceTypeElement = appendElementNS(element, RESOURCE_TYPE_PROPERTY_NAME.getNamespace(),
				createQName(getNamespacePrefix(WEBDAV_NAMESPACE), RESOURCE_TYPE_PROPERTY_NAME.getLocalName()));
		if(typeNamespace != null && typeLocalName != null) { //if a type was given
			appendElementNS(resourceTypeElement, typeNamespace, createQName(getNamespacePrefix(typeNamespace), typeLocalName)); //look up the prefix and create a subelement to represent the resource type
		}
		return resourceTypeElement; //return the element we created
	}

}
