package com.garretwilson.net.http;

import java.io.*;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.garretwilson.io.ParseReader;
import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.net.http.HTTPConstants.*;
import static com.garretwilson.net.http.HTTPParser.*;
import static com.garretwilson.text.CharacterEncodingConstants.*;
import com.garretwilson.text.CharacterEncoding;
import com.garretwilson.text.SyntaxException;
import com.garretwilson.text.xml.XMLProcessor;
import com.garretwilson.text.xml.XMLSerializer;
import static com.garretwilson.text.xml.XMLUtilities.*;
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

	/**Retrieves a list of name-value pairs representing all the headers of this message.
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

//TODO create a normalizeHeaders() method that converts all multiple headers to single headers

	/**The bytes making up the body of the message.*/
	private byte[] body=null;

		/**@return The bytes making up the body of the message.*/
		public byte[] getBody() {return body;}
	
		/**Sets the bytes to make up the body of the message.
		Updates the Content-Length header.
		@param body The body content.
		@exception NullPointerException if the given body is <code>null</code>.
		@see HTTPConstants#CONTENT_LENGTH_HEADER
		*/
		public void setBody(final byte[] body)
		{
			this.body=checkInstance(body, "Body is null.");	//save the body
			if(body.length>0)	//if there is a request body
			{
				setContentLength(body.length);	//set the content length
			}
			else	//if there is no request body
			{
				removeHeaders(CONTENT_LENGTH_HEADER);	//remove the content length header
			}
		}

	//Connection header

	/**@return An array of connection tokens indicating whether the connection should be persistent,
	 	or <code>null</code> if there is no connection header.
	@see HTTPConstants#CONNECTION_HEADER
	*/
	public String[] getConnection()
	{
		final String connectionHeader=getHeader(CONNECTION_HEADER);	//get the connection header
		try
		{
			return connectionHeader!=null ? parseList(new ParseReader(connectionHeader)) : null;	//return the list of connection tokens, if there is a connection header
		}
		catch(final IOException ioException)	//we shouldn't have I/O errors parsing a list
		{
			throw new AssertionError(ioException);
		}		
	}

	/**Determines whether the Connection header is present with the token "close".
	@return	<code>true</code> if the Connection header contains the "close" token.
	@see #getConnection()
	@see HTTPConstants#CONNECTION_HEADER
	@see HTTPConstants#CONNECTION_CLOSE
	*/
	public boolean isConnectionClose()
	{
		final String[] connectionTokens=getConnection();	//get the connection tokens
		if(connectionTokens!=null)	//if connection tokens are present
		{
			for(final String token:connectionTokens)	//for each connection token
			{
				if(CONNECTION_CLOSE.equals(token))	//if this token is "close"
				{
					return true;	//indicate that the connection is marked to be closed
				}
			}
		}
		return false;	//with no "close" connection indication, this connection should be persistent
	}

	/**Sets the Connection header with the given connection token.
	@param connection The connection token such as "close".
	@see HTTPConstants#CONNECTION_HEADER
	@see HTTPConstants#CONNECTION_CLOSE
	*/
	public void setConnection(final String connection)
	{
		setHeader(CONNECTION_HEADER, connection);	//set the connection header with the given token
	}

	/**Sets whether the connection should be closed after the response.
	@param close <code>true</code> if the connection flagged to be closed after the response.
	@see #setConnection(String)
	@see HTTPConstants#CONNECTION_HEADER
	@see HTTPConstants#CONNECTION_CLOSE
	*/
	public void setConnectionClose(final boolean close)
	{
		setConnection(CONNECTION_CLOSE);	//set the connection to "close"
	}

	//Content-Length header

	/**@return The content length, or <code>-1</code> if no content length is given.
	@exception SyntaxException if the content length is given but in an invalid format.
	@see HTTPConstants#CONTENT_LENGTH_HEADER
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

	/**Sets the content length header.
	@param contentLength The length of the body content.
	@exception IllegalArgumentException if the given content length is less than zero.
	@see HTTPConstants#CONTENT_LENGTH_HEADER
	*/
	public void setContentLength(final long contentLength)
	{
		if(contentLength<0)	//if the content length is less than zero
		{
			throw new IllegalArgumentException("Invalid content length "+contentLength);
		}
		setHeader(CONTENT_LENGTH_HEADER, Long.toString(contentLength));	//set the content length
	}

	//Transfer-Encoding header

	/**@return An array of specified transfer encodings, or <code>null</code> if no transfer encodings are specified.
	@see HTTPConstants#TRANSFER_ENCODING_HEADER
	*/
	public String[] getTransferEncoding()
	{
		final String transferEncodingHeader=getHeader(TRANSFER_ENCODING_HEADER);	//get the transfer encoding header
		try
		{
			return transferEncodingHeader!=null ? parseList(new ParseReader(transferEncodingHeader)) : null;	//return the list of transfer encodings, if there is a transfer encoding header
		}
		catch(final IOException ioException)	//we shouldn't have I/O errors parsing a list TODO fix---we may, if the server isn't well written
		{
			throw new AssertionError(ioException);	//TODO fix all these errors
		}		
	}

	/**Retrieves an XML document from the body of the HTTP message.
	@param namespaceAware <code>true</code> if the document should support for XML namespaces, else <code>false</code>.
	@param validated <code>true</code> if the document should be validated as it is parsed, else <code>false</code>.
	@return A document representing the XML information, or <code>null</code> if there is no content.
	@exception IOException if there is an error reading the XML.
	@exception ParserConfigurationException if an appropriate parser could not be found for parsing the XML.
	@exception SAXException if there was an error parsing the XML.
	*/
	public Document getXML(final boolean namespaceAware, final boolean validated) throws IOException, ParserConfigurationException, SAXException
	{
		final byte[] body=getBody();	//get the body of the message
		if(body!=null)	//if a body was given
		{
			final InputStream xmlInputStream=new ByteArrayInputStream(body);	//create a new input stream from the body bytes
			return createDocumentBuilder(namespaceAware, validated).parse(xmlInputStream);	//parse the document				
		}
		return null;	//show that there is no content to return
	}

	/**Places an XML document into the body of the HTTP message.
	@param document The XML document to place into the message.
	@exception IOException if there is an error writing the XML.
	*/
	public void setXML(final Document document) throws IOException
	{
		final ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();	//create a byte array output stream to hold our outgoing data
		try
		{
			new XMLSerializer(true).serialize(document, byteArrayOutputStream, new CharacterEncoding(UTF_8, null, NO_BOM));	//serialize the document to the byte array with no byte order mark
			final byte[] bytes=byteArrayOutputStream.toByteArray();	//get the bytes we serialized
			setBody(bytes);	//set the bytes of the XML document into the body of the message
		}
		finally
		{
			byteArrayOutputStream.close();	//always close the stream as good practice			
		}
	}

}
