package com.garretwilson.net.http.webdav;

import com.garretwilson.util.NameValuePair;

/**A WebDAV property, representing a WebDAV property name and a value (such as a literal string value or a document fragment value).
Although a WebDAV property may not have <code>null</code> for a value,
	in some instances this class allows <code>null</code>, to express property deletions for example.
@author Garret Wilson
*/
public class WebDAVProperty extends NameValuePair<WebDAVPropertyName, WebDAVPropertyValue>
{

	/**Constructor specifying the name and value.
	@param name The WebDAV property's name.
	@param value The WebDAV property's value, which may be <code>null</code> in some contexts.
	*/
	public WebDAVProperty(final WebDAVPropertyName name, final WebDAVPropertyValue value)
	{
		super(name, value);	//construct the parent class with the name and value
	}

}
