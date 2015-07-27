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

import com.globalmentor.model.ObjectDecorator;
import com.globalmentor.text.xml.XML;

import org.w3c.dom.DocumentFragment;

/**
 * A WebDAV value representing a non-empty XML document fragment. Contexts in which a document fragment would contain no child nodes should be represented by
 * <code>null</code>, not by an empty document fragment in a {@link WebDAVDocumentFragmentPropertyValue}.
 * @author Garret Wilson
 */
public class WebDAVDocumentFragmentPropertyValue extends ObjectDecorator<DocumentFragment> implements WebDAVPropertyValue {

	/**
	 * Document fragment constructor.
	 * @param documentFragment The document fragment this value represents.
	 * @throws NullPointerException if the given document fragment is <code>null</code>.
	 * @throws IllegalArgumentException if the given document fragment has no child nodes.
	 */
	public WebDAVDocumentFragmentPropertyValue(final DocumentFragment documentFragment) {
		super(documentFragment); //construct the parent class
		if(documentFragment.getChildNodes().getLength() == 0) { //if the document fragment has no child nodes
			throw new IllegalArgumentException("Document fragment cannot be empty.");
		}
	}

	/** @return The document fragment this value represents. */
	public DocumentFragment getDocumentFragment() {
		return getObject();
	}

	/** @return A non-<code>null</code> literal representing plain text contained in this WebDAV value, which may be the empty string. */
	public String getText() {
		return XML.getText(getDocumentFragment());
	}
}
