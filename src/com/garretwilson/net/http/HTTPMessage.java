package com.garretwilson.net.http;

import com.garretwilson.util.NameValuePair;
import com.garretwilson.util.SyntaxException;

/**An HTTP request or response as defined by
<a href="http://www.ietf.org/rfc/rfc2616.txt">RFC 2616</a>,	"Hypertext Transfer Protocol -- HTTP/1.1".
@author Garret Wilson
*/
public interface HTTPMessage
{

	/**@return The HTTP version.*/
	public HTTPVersion getVersion();

	/**Retrieves a list of header values with the given name.
	@param name The case-insensitive name of the headers to retrieve.
	@return An array of header values.
	*/
	public String[] getHeaders(final String name);

	/**Retrieves the value of the first header with the given name.
	@param name The case-insensitive name of the headers to retrieve.
	@return The header value, or <code>null</code> if no such header is present.
	*/
	public String getHeader(final String name);

	/**@return Retrieves a list of name-value pairs representing all the headers of this message.
	@return The header value, or <code>null</code> if no such header is present.
	*/
	public NameValuePair<String, String>[] getHeaders();

	/**Adds a header to the list of headers.
	Any existing headers with the same name will not be modified.
	@param name The case-insensitive name of the header to add.
	@param value The value of the header to set.
	*/
	public void addHeader(final String name, final String value);

	/**Sets a header.
	Any existing headers with the same name will be removed.
	@param name The case-insensitive name of the header to set.
	@param value The value of the header to set.
	*/
	public void setHeader(final String name, final String value);
	
	/**Removes all headers with the given name.
	@param name The case-insensitive name of the header to remove.
	*/
	public void removeHeaders(final String name);

	/**@return The bytes making up the body of the message, or <code>null</code>
	 	if there is no body and there will consequently be no content length indication.
	*/
	public byte[] getBody();
	
	/**Sets the bytes to make up the body of the message.
	@param body The body content, or <code>null</code>
		if there is no body and there will consequently be no content length indication.
		*/
	public void setBody(final byte[] body);

	/**Sets the content length header.
	@param contentLength The length of the body content.
	@exception IllegalArgumentException if the given content length is less than zero.
	*/
	public void setContentLength(final long contentLength);

	/**@return The content length, or <code>-1</code> if no content length is given.
	@exception SyntaxException if the content length is given but in an invalid format.
	*/
	public long getContentLength() throws SyntaxException;

}
