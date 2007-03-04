package com.garretwilson.net.http.webdav;

import com.garretwilson.util.ObjectDecorator;

/**A literal WebDAV value of a resource property.
The literal value of the resource can be returned using {@link #toString()} or {@link #getText()}.
@author Garret Wilson
*/
public class WebDAVLiteralPropertyValue extends ObjectDecorator<String> implements WebDAVPropertyValue
{

	/**String literal constructor.
	@param literal The literal string this value represents.
	@exception NullPointerException if the given literal is <code>null</code>.
	*/
	public WebDAVLiteralPropertyValue(final String literal)
	{
		super(literal);	//construct the parent class
	}

	/**@return A non-<code>null</code> literal representing plain text contained in this WebDAV value, which may be the empty string.*/
	public String getText()
	{
		return getDecoratedObject();	//return the string literal itself
	}
}
