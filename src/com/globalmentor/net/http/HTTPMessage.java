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

package com.globalmentor.net.http;

import java.util.Date;

import com.globalmentor.text.SyntaxException;
import com.globalmentor.util.NameValuePair;

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

	/**Retrieves a list of name-value pairs representing all the headers of this message.
	@return The header names and values.
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

	//Connection header

	/**@return An array of connection tokens indicating whether the connection should be persistent,
	 	or <code>null</code> if there is no connection header.
	@see HTTP#CONNECTION_HEADER
	*/
	public String[] getConnection();

	/**Determines whether the Connection header is present with the token "close".
	@return	<code>true</code> if the Connection header contains the "close" token.
	@see #getConnection()
	@see HTTP#CONNECTION_HEADER
	@see HTTP#CONNECTION_CLOSE
	*/
	public boolean isConnectionClose();

	/**Sets the Connection header with the given connection token.
	@param connection The connection token such as "close".
	@see HTTP#CONNECTION_HEADER
	@see HTTP#CONNECTION_CLOSE
	*/
	public void setConnection(final String connection);

	/**Sets whether the connection should be closed after the response.
	@param close <code>true</code> if the connection flagged to be closed after the response.
	@see #setConnection(String)
	@see HTTP#CONNECTION_HEADER
	@see HTTP#CONNECTION_CLOSE
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

	//Content-MD5 header

	/**@return The Base64 encoding of the 128-bit MD5 digest of the message body as per RFC 1864, or <code>null</code> if no content MD5 digest is given.
	@see HTTP#CONTENT_MD5_HEADER
	*/
	public String getContentMD5();

	//Date header

	/**@return The date of message, or <code>null</code> if there is no date header.
	@exception SyntaxException if the date header does not contain a valid RFC 1123 date. 
	@see HTTP#DATE_HEADER
	*/
	public Date getDate() throws SyntaxException;

	/**Sets the date of the message.
	@param date The date to set.
	@throws NullPointerException if the given date is <code>null</code>.
	@see HTTP#DATE_HEADER
	*/
	public void setDate(final Date date);

	//Transfer-Encoding header

	/**@return An array of specified transfer encodings, or <code>null</code> if no transfer encodings are specified.
	@see HTTP#TRANSFER_ENCODING_HEADER
	*/
	public String[] getTransferEncoding();

	/**Sets the transfer encoding header.
	@param transferEncodings The transfer encodings to use.
	@exception NullPointerException if the given transfer encodings is <code>null</code>.
	@exception IllegalArgumentException if no transfer encodings are given.
	@see HTTP#TRANSFER_ENCODING_HEADER
	*/
	public void setTransferEncoding(final String... transferEncodings);

}
