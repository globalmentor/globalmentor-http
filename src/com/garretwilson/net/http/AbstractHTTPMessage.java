package com.garretwilson.net.http;

import java.util.*;

import static com.garretwilson.net.http.HTTPConstants.CONTENT_LENGTH_HEADER;
import com.garretwilson.util.*;

/**An abstract implementation of an HTTP request or response as defined by
<a href="http://www.ietf.org/rfc/rfc2616.txt">RFC 2616</a>,	"Hypertext Transfer Protocol -- HTTP/1.1".
@author Garret Wilson
*/
public class AbstractHTTPMessage implements HTTPMessage
{
	
	/**The default HTTP version supported, 1.1.*/
	protected final static HTTPVersion DEFAULT_VERSION=new HTTPVersion(1, 1);

	/**A convenience constant signifying no header values.*/
	protected final String[] NO_HEADERS=new String[0];

	/**The HTTP version.*/
	private final HTTPVersion version;

		/**@return The HTTP version.*/
		public HTTPVersion getVersion() {return version;}

	/**The mapped list of name-value pairs representing header name and a list of header values.
	Keeping a list allows us to remember the order in which the headers were added,
		while still keeping similarly-named headers together.
	Headers names are stored in lowercase as a canonical representation.
	*/
	private MappedList<String, NameValuePair<String, List<String>>> headerMappedList=new IDMappedList<String, NameValuePair<String, List<String>>>(new HashMap<String, NameValuePair<String, List<String>>>(), new ArrayList<NameValuePair<String, List<String>>>());

	/**Constructs a message with a version.
	@param version The HTTP version being used.
	*/
	public AbstractHTTPMessage(final HTTPVersion version)
	{
		this.version=version;
	}

	/**Retrieves a list of header values with the given name.
	@param name The case-insensitive name of the headers to retrieve.
	@param create <code>true</code> if a header list should be added
		if one doesn't exist for this header.
	@return A modifiable list of header values, or <code>null</code> if there are no header values for the given header
	*/
	protected List<String> getHeaderList(final String name, final boolean create)
	{
		final String canonicalName=name.toLowerCase();	//we'll use lowercase as a canonical representation of the header names
		NameValuePair<String, List<String>> headerNameListPair=headerMappedList.get(canonicalName);	//get the name-value pair with the list of header values, if any
		if(headerNameListPair==null && create)	//if there is no name-value pair containing a list of header values, but we should create one
		{
			headerNameListPair=new NameValuePair<String, List<String>>(canonicalName, new ArrayList<String>());	//create a new name-value pair with a list of headers
			headerMappedList.add(headerNameListPair);	//add the name-value pair to our list, which will map the canonical header name
		}
		return headerNameListPair!=null ? headerNameListPair.getValue() : null;	//if there is a name-value pair for this header, return its list of values
	}

	/**Adds a header to the list of headers.
	@param name The case-insensitive name of the header to retrieve.
	@param value The value of the header to set.
	@param clear Whether any existing headers with the given name should first be cleared.
	*/
	protected void addHeader(final String name, final String value, final boolean clear)
	{
		final List<String> headerList=getHeaderList(name, true);	//get the list of header values, creating the list if needed
		if(clear)	//if we should clear the list first
		{
			headerList.clear();	//clear all existing headers.
		}
		headerList.add(value);	//add this header value to the list
	}

	/**Retrieves a list of header values with the given name.
	@param name The case-insensitive name of the headers to retrieve.
	@return An array of header values.
	*/
	public String[] getHeaders(final String name)
	{
		final List<String> headerList=getHeaderList(name, false);	//get the list of header values, but only if there are such headers
		return headerList!=null ? headerList.toArray(new String[headerList.size()]) : NO_HEADERS;	//return an array of headers if there are any, or an empty array
	}

	/**Retrieves the value of the first header with the given name.
	@param name The case-insensitive name of the headers to retrieve.
	@return The header value, or <code>null</code> if no such header is present.
	*/
	public String getHeader(final String name)
	{
		final List<String> headerList=getHeaderList(name, false);	//get the list of header values, but only if there are such headers
		return headerList!=null && headerList.size()>0 ? headerList.get(0) : null;	//return value of the first header if there are headers with this name
	}

	/**@return Retrieves a list of name-value pairs representing all the headers of this message.
	@return The header value, or <code>null</code> if no such header is present.
	*/
	public NameValuePair<String, String>[] getHeaders()
	{
		final List<NameValuePair<String, String>> headerList=new ArrayList<NameValuePair<String, String>>();	//create a new list of name-value pairs
		for(final NameValuePair<String, List<String>> headerNameListPair:headerMappedList)	//for each header name-value-list pair
		{
			for(final String headerValue:headerNameListPair.getValue())	//for each header value in the list
			{
				headerList.add(new NameValuePair<String, String>(headerNameListPair.getName(), headerValue));	//add a new name-value pair with the header name and value
			}
		}
		return headerList.toArray(new NameValuePair[headerList.size()]);	//return an array of headers from the list
	}
	
	/**Adds a header to the list of headers.
	Any existing headers with the same name will not be modified.
	@param name The case-insensitive name of the header to add.
	@param value The value of the header to set.
	*/
	public void addHeader(final String name, final String value)
	{
		addHeader(name, value, false);	//add this header value, leaving other same-name headers untouched
	}

	/**Sets a header.
	Any existing headers with the same name will be removed.
	@param name The case-insensitive name of the header to set.
	@param value The value of the header to set.
	*/
	public void setHeader(final String name, final String value)
	{
		addHeader(name, value, true);	//add this header value, removing any other headers with the same name
	}
	
	/**Removes all headers with the given name.
	@param name The case-insensitive name of the header to remove.
	*/
	public void removeHeaders(final String name)
	{
		headerMappedList.removeKey(name.toLowerCase());	//remove any mapping of this header from the mapped list
	}

	/**The bytes making up the body of the message, or <code>null</code>
	 	if there is no body and will be no content-length indication.
	*/
	private byte[] body=null;

		/**@return The bytes making up the body of the message, or <code>null</code>
		 	if there is no body and there will consequently be no content length indication.
		*/
		public byte[] getBody() {return body;}
	
		/**Sets the bytes to make up the body of the message.
		@param body The body content, or <code>null</code>
			if there is no body and there will consequently be no content length indication.
			*/
		public void setBody(final byte[] body) {this.body=body;}

	/**Sets the content length header.
	@param contentLength The length of the body content.
	@exception IllegalArgumentException if the given content length is less than zero.
	*/
	public void setContentLength(final long contentLength)
	{
		if(contentLength<0)	//if the content length is less than zero
		{
			throw new IllegalArgumentException("Invalid content length "+contentLength);
		}
		setHeader(CONTENT_LENGTH_HEADER, Long.toString(contentLength));	//set the content length
	}

	/**@return The content length, or <code>-1</code> if no content length is given.
	@exception SyntaxException if the content length is given but in an invalid format.
	*/
	public long getContentLength() throws SyntaxException
	{
		final String contentLengthString=getHeader(CONTENT_LENGTH_HEADER);	//get the content length value
		if(contentLengthString!=null)	//if a content length is present
		{
			try
			{
				return Long.valueOf(contentLengthString);	//convert the string value to a long
			}
			catch(final NumberFormatException numberFormatException)	//if the string couldn't be parsed as a number
			{
				throw new SyntaxException(contentLengthString, numberFormatException);
			}
		}
		else	//if no content length is present
		{
			return -1;	//show that there is no content length specified
		}
	}

}
