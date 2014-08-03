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

import java.io.*;
import java.text.ParseException;
import java.util.*;

import static com.globalmentor.net.http.HTTP.*;
import static com.globalmentor.net.http.HTTPFormatter.*;
import static com.globalmentor.net.http.HTTPParser.*;

import com.globalmentor.collections.DecoratorIDedMappedList;
import com.globalmentor.collections.MappedList;
import com.globalmentor.io.ParseReader;
import com.globalmentor.model.NameValuePair;
import com.globalmentor.text.SyntaxException;
import com.globalmentor.util.*;

/**
 * An abstract implementation of an HTTP request or response as defined by <a href="http://www.ietf.org/rfc/rfc2616.txt">RFC 2616</a>,
 * "Hypertext Transfer Protocol -- HTTP/1.1".
 * @author Garret Wilson
 */
public class AbstractHTTPMessage implements HTTPMessage {

	/** The default HTTP version supported, 1.1. */
	protected final static HTTPVersion DEFAULT_VERSION = new HTTPVersion(1, 1);

	/** A convenience constant signifying no header values. */
	protected final String[] NO_HEADERS = new String[0];

	/** The HTTP version. */
	private final HTTPVersion version;

	/** @return The HTTP version. */
	public HTTPVersion getVersion() {
		return version;
	}

	/**
	 * The mapped list of name-value pairs representing header name and a list of header values. Keeping a list allows us to remember the order in which the
	 * headers were added, while still keeping similarly-named headers together. Headers names are stored in lowercase as a canonical representation.
	 */
	private MappedList<String, NameValuePair<String, List<String>>> headerMappedList = new DecoratorIDedMappedList<String, NameValuePair<String, List<String>>>(
			new HashMap<String, NameValuePair<String, List<String>>>(), new ArrayList<NameValuePair<String, List<String>>>());

	/**
	 * Constructs a message with a version.
	 * @param version The HTTP version being used.
	 */
	public AbstractHTTPMessage(final HTTPVersion version) {
		this.version = version;
	}

	/**
	 * Retrieves a list of header values with the given name.
	 * @param name The case-insensitive name of the headers to retrieve.
	 * @param create <code>true</code> if a header list should be added if one doesn't exist for this header.
	 * @return A modifiable list of header values, or <code>null</code> if there are no header values for the given header
	 */
	protected List<String> getHeaderList(final String name, final boolean create) {
		final String canonicalName = name.toLowerCase(); //we'll use lowercase as a canonical representation of the header names
		NameValuePair<String, List<String>> headerNameListPair = headerMappedList.get(canonicalName); //get the name-value pair with the list of header values, if any
		if(headerNameListPair == null && create) { //if there is no name-value pair containing a list of header values, but we should create one
			headerNameListPair = new NameValuePair<String, List<String>>(canonicalName, new ArrayList<String>()); //create a new name-value pair with a list of headers
			headerMappedList.add(headerNameListPair); //add the name-value pair to our list, which will map the canonical header name
		}
		return headerNameListPair != null ? headerNameListPair.getValue() : null; //if there is a name-value pair for this header, return its list of values
	}

	/**
	 * Adds a header to the list of headers.
	 * @param name The case-insensitive name of the header to retrieve.
	 * @param value The value of the header to set.
	 * @param clear Whether any existing headers with the given name should first be cleared.
	 */
	protected void addHeader(final String name, final String value, final boolean clear) {
		final List<String> headerList = getHeaderList(name, true); //get the list of header values, creating the list if needed
		if(clear) { //if we should clear the list first
			headerList.clear(); //clear all existing headers.
		}
		headerList.add(value); //add this header value to the list
	}

	/**
	 * Retrieves a list of header values with the given name.
	 * @param name The case-insensitive name of the headers to retrieve.
	 * @return An array of header values.
	 */
	public String[] getHeaders(final String name) {
		final List<String> headerList = getHeaderList(name, false); //get the list of header values, but only if there are such headers
		return headerList != null ? headerList.toArray(new String[headerList.size()]) : NO_HEADERS; //return an array of headers if there are any, or an empty array
	}

	/**
	 * Retrieves the value of the first header with the given name.
	 * @param name The case-insensitive name of the headers to retrieve.
	 * @return The header value, or <code>null</code> if no such header is present.
	 */
	public String getHeader(final String name) {
		final List<String> headerList = getHeaderList(name, false); //get the list of header values, but only if there are such headers
		return headerList != null && headerList.size() > 0 ? headerList.get(0) : null; //return value of the first header if there are headers with this name
	}

	/**
	 * Retrieves a list of name-value pairs representing all the headers of this message.
	 * @return The header names and values.
	 */
	@SuppressWarnings("unchecked")
	public NameValuePair<String, String>[] getHeaders() {
		final List<NameValuePair<String, String>> headerList = new ArrayList<NameValuePair<String, String>>(); //create a new list of name-value pairs
		for(final NameValuePair<String, List<String>> headerNameListPair : headerMappedList) { //for each header name-value-list pair
			for(final String headerValue : headerNameListPair.getValue()) { //for each header value in the list
				headerList.add(new NameValuePair<String, String>(headerNameListPair.getName(), headerValue)); //add a new name-value pair with the header name and value
			}
		}
		return (NameValuePair<String, String>[])headerList.toArray(new NameValuePair[headerList.size()]); //return an array of headers from the list
	}

	/**
	 * Adds a header to the list of headers. Any existing headers with the same name will not be modified.
	 * @param name The case-insensitive name of the header to add.
	 * @param value The value of the header to set.
	 */
	public void addHeader(final String name, final String value) {
		addHeader(name, value, false); //add this header value, leaving other same-name headers untouched
	}

	/**
	 * Sets a header. Any existing headers with the same name will be removed.
	 * @param name The case-insensitive name of the header to set.
	 * @param value The value of the header to set.
	 */
	public void setHeader(final String name, final String value) {
		addHeader(name, value, true); //add this header value, removing any other headers with the same name
	}

	/**
	 * Removes all headers with the given name.
	 * @param name The case-insensitive name of the header to remove.
	 */
	public void removeHeaders(final String name) {
		headerMappedList.removeKey(name.toLowerCase()); //remove any mapping of this header from the mapped list
	}

	//TODO create a normalizeHeaders() method that converts all multiple headers to single headers

	//Connection header

	/**
	 * @return An array of connection tokens indicating whether the connection should be persistent, or <code>null</code> if there is no connection header.
	 * @see HTTP#CONNECTION_HEADER
	 */
	public String[] getConnection() {
		final String connectionHeader = getHeader(CONNECTION_HEADER); //get the connection header
		try {
			return connectionHeader != null ? parseList(new ParseReader(connectionHeader)) : null; //return the list of connection tokens, if there is a connection header
		} catch(final IOException ioException) { //we shouldn't have I/O errors parsing a list
			throw new AssertionError(ioException);
		}
	}

	/**
	 * Determines whether the Connection header is present with the token "close".
	 * @return <code>true</code> if the Connection header contains the "close" token.
	 * @see #getConnection()
	 * @see HTTP#CONNECTION_HEADER
	 * @see HTTP#CONNECTION_CLOSE
	 */
	public boolean isConnectionClose() {
		final String[] connectionTokens = getConnection(); //get the connection tokens
		if(connectionTokens != null) { //if connection tokens are present
			for(final String token : connectionTokens) { //for each connection token
				if(CONNECTION_CLOSE.equals(token)) { //if this token is "close"
					return true; //indicate that the connection is marked to be closed
				}
			}
		}
		return false; //with no "close" connection indication, this connection should be persistent
	}

	/**
	 * Sets the Connection header with the given connection token.
	 * @param connection The connection token such as "close".
	 * @see HTTP#CONNECTION_HEADER
	 * @see HTTP#CONNECTION_CLOSE
	 */
	public void setConnection(final String connection) {
		setHeader(CONNECTION_HEADER, connection); //set the connection header with the given token
	}

	/**
	 * Sets whether the connection should be closed after the response.
	 * @param close <code>true</code> if the connection flagged to be closed after the response.
	 * @see #setConnection(String)
	 * @see HTTP#CONNECTION_HEADER
	 * @see HTTP#CONNECTION_CLOSE
	 */
	public void setConnectionClose(final boolean close) {
		setConnection(CONNECTION_CLOSE); //set the connection to "close"
	}

	//Content-Length header

	/**
	 * @return The content length, or <code>-1</code> if no content length is given.
	 * @throws SyntaxException if the content length is given but in an invalid format.
	 * @see HTTP#CONTENT_LENGTH_HEADER
	 */
	public long getContentLength() throws SyntaxException {
		final String contentLengthString = getHeader(CONTENT_LENGTH_HEADER); //get the content length value
		if(contentLengthString != null) { //if a content length is present
			try {
				return Long.valueOf(contentLengthString); //convert the string value to a long
			} catch(final NumberFormatException numberFormatException) { //if the string couldn't be parsed as a number
				throw new SyntaxException(numberFormatException, contentLengthString);
			}
		} else { //if no content length is present
			return -1; //show that there is no content length specified
		}
	}

	/**
	 * Sets the content length header.
	 * @param contentLength The length of the body content.
	 * @throws IllegalArgumentException if the given content length is less than zero.
	 * @see HTTP#CONTENT_LENGTH_HEADER
	 */
	public void setContentLength(final long contentLength) {
		if(contentLength < 0) { //if the content length is less than zero
			throw new IllegalArgumentException("Invalid content length " + contentLength);
		}
		setHeader(CONTENT_LENGTH_HEADER, Long.toString(contentLength)); //set the content length
	}

	//Content-MD5 header

	/**
	 * @return The Base64 encoding of the 128-bit MD5 digest of the message body as per RFC 1864, or <code>null</code> if no content MD5 digest is given.
	 * @see HTTP#CONTENT_MD5_HEADER
	 */
	public String getContentMD5() {
		return getHeader(CONTENT_MD5_HEADER); //return the content MD5 value, if any
	}

	//Date header

	/**
	 * @return The date of message, or <code>null</code> if there is no date header.
	 * @throws SyntaxException if the date header does not contain a valid RFC 1123 date.
	 * @see HTTP#DATE_HEADER
	 */
	public Date getDate() throws SyntaxException {
		final String dateHeader = getHeader(DATE_HEADER); //get the date header
		try {
			return dateHeader != null ? new HTTPDateFormat(HTTPDateFormat.Style.RFC1123).parse(dateHeader) : null; //parse the date, if there is a date header
		} catch(final ParseException parseException) {
			throw new SyntaxException(parseException, dateHeader);
		}
	}

	/**
	 * Sets the date of the message.
	 * @param date The date to set.
	 * @throws NullPointerException if the given date is <code>null</code>.
	 * @see HTTP#DATE_HEADER
	 */
	public void setDate(final Date date) {
		setHeader(DATE_HEADER, new HTTPDateFormat(HTTPDateFormat.Style.RFC1123).format(date)); //set the date header with the given date
	}

	//Transfer-Encoding header

	/**
	 * @return An array of specified transfer encodings, or <code>null</code> if no transfer encodings are specified.
	 * @see HTTP#TRANSFER_ENCODING_HEADER
	 */
	public String[] getTransferEncoding() {
		final String transferEncodingHeader = getHeader(TRANSFER_ENCODING_HEADER); //get the transfer encoding header
		try {
			return transferEncodingHeader != null ? parseList(new ParseReader(transferEncodingHeader)) : null; //return the list of transfer encodings, if there is a transfer encoding header
		} catch(final IOException ioException) { //we shouldn't have I/O errors parsing a list TODO fix---we may, if the server isn't well written
			throw new AssertionError(ioException); //TODO fix all these errors
		}
	}

	/**
	 * Sets the transfer encoding header.
	 * @param transferEncodings The transfer encodings to use.
	 * @throws NullPointerException if the given transfer encodings is <code>null</code>.
	 * @throws IllegalArgumentException if no transfer encodings are given.
	 * @see HTTP#TRANSFER_ENCODING_HEADER
	 */
	public void setTransferEncoding(final String... transferEncodings) {
		if(transferEncodings.length == 0) { //if no transfer encodings are given
			throw new IllegalArgumentException("No transfer encodings given.");
		}
		setHeader(TRANSFER_ENCODING_HEADER, formatList(new StringBuilder(), (Object[])transferEncodings).toString()); //set the transfer encodings
	}

}
