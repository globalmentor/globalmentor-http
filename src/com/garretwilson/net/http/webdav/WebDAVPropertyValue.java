package com.garretwilson.net.http.webdav;

/**Indicates that an object is a value of a WebDAV resource property.
@author Garret Wilson
*/
public interface WebDAVPropertyValue
{
	/**@return A non-<code>null</code> literal representing plain text contained in this WebDAV value, which may be the empty string.*/
	public String getText();
}
