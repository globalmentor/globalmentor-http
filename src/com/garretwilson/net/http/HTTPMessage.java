package com.garretwilson.net.http;

import java.io.IOException;

import org.w3c.dom.Document;

import com.garretwilson.util.NameValuePair;
import com.garretwilson.util.SyntaxException;

/**An HTTP request or response as defined by
<a href="http://www.ietf.org/rfc/rfc2616.txt">RFC 2616</a>,	"Hypertext Transfer Protocol -- HTTP/1.1".
@author Garret Wilson
*/
public interface HTTPMessage
{

	/**A constant byte array indicating no body.*/
	public final static byte[] NO_BODY=new byte[0];

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

	/**@return The bytes making up the body of the message.*/
	public byte[] getBody();

	/**Sets the bytes to make up the body of the message.
	Updates the Content-Length header.
	@param body The body content.
	@exception NullPointerException if the given body is <code>null</code>.
	@see HTTPConstants#CONTENT_LENGTH_HEADER
	*/
	public void setBody(final byte[] body);

	//Connection header

	/**@return An array of connection tokens indicating whether the connection should be persistent,
	 	or <code>null</code> if there is no connection header.
	@see HTTPConstants#CONNECTION_HEADER
	*/
	public String[] getConnection();

	/**Determines whether the Connection header is present with the token "close".
	@return	<code>true</code> if the Connection header contains the "close" token.
	@see #getConnection()
	@see HTTPConstants#CONNECTION_HEADER
	@see HTTPConstants#CONNECTION_CLOSE
	*/
	public boolean isConnectionClose();

	/**Sets the Connection header with the given connection token.
	@param connection The connection token such as "close".
	@see HTTPConstants#CONNECTION_HEADER
	@see HTTPConstants#CONNECTION_CLOSE
	*/
	public void setConnection(final String connection);

	/**Sets whether the connection should be closed after the response.
	@param close <code>true</code> if the connection flagged to be closed after the response.
	@see #setConnection(String)
	@see HTTPConstants#CONNECTION_HEADER
	@see HTTPConstants#CONNECTION_CLOSE
	*/
	public void setConnectionClose(final boolean close);

	//Content-Length header
	
	/**@return The content length, or <code>-1</code> if no content length is given.
	@exception SyntaxException if the content length is given but in an invalid format.
	*/
	public long getContentLength() throws SyntaxException;

	/**Sets the content length header.
	@param contentLength The length of the body content.
	@exception IllegalArgumentException if the given content length is less than zero.
	*/
	public void setContentLength(final long contentLength);

		//content
	
	/**Retrieves an XML document from the body of the HTTP message.
	@return A document representing the XML information, or <code>null</code> if there is no content.
	@exception IOException if there is an error reading the XML.
	*/
	public Document getXML() throws IOException;

	/**Places an XML document into the body of the HTTP message.
	@param document The XML document to place into the message.
	@exception IOException if there is an error writing the XML.
	*/
	public void setXML(final Document document) throws IOException;

}
