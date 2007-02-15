package com.garretwilson.net.http.webdav;

import com.garretwilson.text.xml.QualifiedName;
import com.garretwilson.util.NameValuePair;

/**A WebDAV property, representing an XML qualified name and a value.
WebDAV properties are also WebDAV property values, as they can appears as values of other WebDAV properties.
@author Garret Wilson
*/
public class WebDAVProperty extends NameValuePair<QualifiedName, WebDAVPropertyValue> implements WebDAVPropertyValue
{

	/**Constructor specifying the name and value.
	@param name The WebDAV property's name.
	@param value The WebDAV property's value
	*/
	public WebDAVProperty(final QualifiedName name, final WebDAVPropertyValue value)
	{
		super(name, value);	//construct the parent class with the name and value
	}

}
