package com.globalmentor.net.http.webdav;

import static com.globalmentor.java.Objects.*;

import com.globalmentor.util.NameValuePair;

/**A WebDAV property, representing a WebDAV property name and a value (such as a literal string value or a document fragment value).
@author Garret Wilson
*/
public class WebDAVProperty extends NameValuePair<WebDAVPropertyName, WebDAVPropertyValue>
{

	/**Constructor specifying the name and value.
	@param name The WebDAV property's name.
	@param value The WebDAV property's value, which may be <code>null</code>.
	@exception NullPointerException if the given name is <code>null</code>.
	*/
	public WebDAVProperty(final WebDAVPropertyName name, final WebDAVPropertyValue value)
	{
		super(checkInstance(name, "WebDAV property name cannot be null."), value);	//construct the parent class with the name and value
	}

}
