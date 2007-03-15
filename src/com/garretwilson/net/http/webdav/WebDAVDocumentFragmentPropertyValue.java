package com.garretwilson.net.http.webdav;

import org.w3c.dom.DocumentFragment;

import com.garretwilson.text.xml.XMLUtilities;
import com.garretwilson.util.ObjectDecorator;

/**A WebDAV value representing a non-empty XML document fragment.
Contexts in which a document fragment would contain no child nodes should be represented by <code>null</code>, not by an empty document fragment in a {@link WebDAVDocumentFragmentPropertyValue}.
@author Garret Wilson
*/
public class WebDAVDocumentFragmentPropertyValue extends ObjectDecorator<DocumentFragment> implements WebDAVPropertyValue
{

	/**Document fragment constructor.
	@param documentFragment The document fragment this value represents.
	@exception NullPointerException if the given document fragment is <code>null</code>.
	@exception IllegalArgumentException if the given document fragment has no child nodes.
	*/
	public WebDAVDocumentFragmentPropertyValue(final DocumentFragment documentFragment)
	{
		super(documentFragment);	//construct the parent class
		if(documentFragment.getChildNodes().getLength()==0)	//if the document fragment has no child nodes
		{
			throw new IllegalArgumentException("Document fragment cannot be empty.");
		}
	}

	/**@return The document fragment this value represents.*/
	public DocumentFragment getDocumentFragment()
	{
		return getObject();
	}

	/**@return A non-<code>null</code> literal representing plain text contained in this WebDAV value, which may be the empty string.*/
	public String getText()
	{
		return XMLUtilities.getText(getDocumentFragment());
	}
}
