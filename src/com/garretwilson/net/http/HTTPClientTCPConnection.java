package com.garretwilson.net.http;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import static java.nio.channels.Channels.*;
import java.util.*;
import java.util.concurrent.*;

import static com.garretwilson.net.http.HTTPConstants.*;
import static com.garretwilson.net.http.HTTPFormatter.*;
import static com.garretwilson.net.URIUtilities.*;
import static com.garretwilson.text.CharacterEncodingConstants.*;
import com.garretwilson.util.Debug;
import com.garretwilson.util.NameValuePair;

/**Represents a connection from a client to a server using HTTP over TCP as defined by
<a href="http://www.ietf.org/rfc/rfc2616.txt">RFC 2616</a>,	"Hypertext Transfer Protocol -- HTTP/1.1".
@author Garret Wilson
*/
public class HTTPClientTCPConnection
{

	/**The channel to the server.*/
	private final SocketChannel channel;

		/**@return The channel to the server.*/
		private Channel getChannel() {return channel;}

	/**The input stream from the channel.*/
	private final InputStream inputStream;

		/**@return The input stream from the channel.*/
		protected InputStream getInputStream() {return inputStream;}

	/**The output stream to the channel.*/
	private final OutputStream outputStream;

		/**@return The output stream from the channel.*/
		protected OutputStream getOutputStream() {return outputStream;}

	/**The queue of outgoing requests.*/
	private final Queue<HTTPRequest> requestQueue=new ConcurrentLinkedQueue<HTTPRequest>();

		/**@return The queue of outgoing requests.*/
		public Queue<HTTPRequest> getRequestQueue() {return requestQueue;}

	/**The queue of outgoing responses.*/
//G***fix	private final Queue<HTTPRequest> requestQueue=new ConcurrentLinkedQueue<HTTPRequest>();

		/**@return The queue of outgoing responses.*/
//G***fix		public Queue<HTTPRequest> getRequestQueue() {return requestQueue;}

	/**The buffer for writing requests.*/
//G***fix	private ByteBuffer writeBuffer=ByteBuffer.allocate(16*1024);	//create a 16k buffer

		/**@return The buffer for writing requests.*/
//	G***fix		protected ByteBuffer getWriteBuffer() {return writeBuffer;}

	/**The buffer for reading responses.*/
//	G***fix	private ByteBuffer readBuffer=ByteBuffer.allocate(16*1024);	//create a 16k buffer

		/**@return The buffer for reading responses.*/
//	G***fix		protected ByteBuffer getReadBuffer() {return writeBuffer;}

	/**URI constructor.
	@param uri The server URI to use when making requests.
	@exception IllegalArgumentException if the given URI does not specify a host.
	@exception IOException if there was an error opening a connection to the server.
	*/
	public HTTPClientTCPConnection(final URI uri) throws IOException
	{
		final int port=uri.getPort();	//get the URI's specified port
		final String host=uri.getHost();	//get the URI's specified host
		if(host==null)	//if no host is specified
		{
			throw new IllegalArgumentException("URI "+uri+" contains no host.");
		}
		final InetSocketAddress socketAddress=new InetSocketAddress(host, port>=0 ? port : DEFAULT_PORT);	//create a new socket address
		channel=SocketChannel.open(socketAddress);	//open a channel to the address
		//TODO later turn on non-blocking access when we have a separate client which will on a separate thread feed requests and retrieve responses
		inputStream=new BufferedInputStream(newInputStream(channel));	//create a new input stream from the channel
		outputStream=new BufferedOutputStream(newOutputStream(channel));	//create a new output stream to the channel
	}

	/**Queues an HTTP request for sending.
	@param request The HTTP request to send.
	*/
	public void makeRequest(final HTTPRequest request)
	{
		getRequestQueue().add(request);	//add this request to our queue of outgoing requests
	}

	public HTTPResponse getResponse() throws IOException, NoSuchElementException
	{
		final HTTPRequest request=getRequestQueue().remove();	//get the next request
		writeRequest(request);	//write the request
		return readResponse();	//read and return the response TODO check for redirects and authentication requests
	}

	/**Reads a response from the input stream.
	@return A response parsed from the input stream data.
	@exception IOException if there is an error writing the data.
	*/
	protected HTTPResponse readResponse() throws IOException
	{
		final InputStream inputStream=getInputStream();	//get the input stream of the response
		
		throw new UnsupportedOperationException();	//TODO fix		
	}
	
	/**Writes a request to the output stream.
	The request's Host and Content-Length headers will be updated.
	@param request The request to write.
	@exception IOException if there is an error writing the data.
	*/
	protected void writeRequest(final HTTPRequest request) throws IOException
	{
		final URI uri=request.getURI();	//get the URI of the request object
		final String requestURI=getRawPathQueryFragment(uri);	//get the unencoded path?query#fragment
		final URI hostURI=getHostURI(uri);	//get the host
		request.setHost(hostURI);	//set the host header to be identical to the host in our request URI
		final byte[] requestBody=request.getBody();	//get the request body
		if(requestBody!=null)	//if there is a request body
		{
			request.setContentLength(requestBody.length);	//set the content length
		}
		else	//if there is no request body
		{
			request.removeHeaders(CONTENT_LENGTH_HEADER);	//remove the content length header
		}
		final StringBuilder headerBuilder=new StringBuilder();	//create a new string builder for formatting the headers
		formatRequestLine(headerBuilder, request.getMethod(), requestURI, request.getVersion());	//Request-Line
		for(final NameValuePair<String, String> header:request.getHeaders())	//look at each header
		{
			formatHeaderLine(headerBuilder, header);	//format this header line
		}
		headerBuilder.append(CRLF);	//append a blank line, signifying the end of the headers
		Debug.trace(headerBuilder);
		final OutputStream outputStream=getOutputStream();	//get the output stream
		outputStream.write(getBytes(headerBuilder));	//write the header
		if(requestBody!=null)	//if there is a request body
		{
			outputStream.write(requestBody);	//write the body
		}
	}

	/**Writes a HTTP header to the output stream, followed by CRLF.
	@param outputStream The data destination.
	@param header The HTTP header to write.
	@exception IOException if there is an error writing the data.
	*/
/*G***del if not needed
	protected static void writeHeader(final OutputStream outputStream, final NameValuePair<String, String> header) throws IOException
	{
		writeLine(outputStream, formatHeader(new StringBuilder(), header));	//format and write out a header line
	}
*/

	/**Writes a line of HTTP content to the output stream, followed by CRLF.
	@param outputStream The data destination.
	@param line The line of HTTP content to write.
	@exception IOException if there is an error writing the data.
	*/
/*G***del
	protected static void writeLine(final OutputStream outputStream, final CharSequence line) throws IOException
	{
		outputStream.write(getBytes(line));	//write the line
		outputStream.write(getBytes(CRLF));	//CRLF
	}
*/
	
	/**Converts a sequence of characters to bytes using the UTF-8 encoding.
	@param characters The characters to convert to bytes.
	@return An array of bytes representing the given characters encoded in UTF-8.
	*/
	protected static byte[] getBytes(final CharSequence characters)
	{
		try
		{
			return characters.toString().getBytes(UTF_8);	//return the characters encoded as UTF-8
		}
		catch(final UnsupportedEncodingException unsupportedEncodingException)	//we should always support UTF-8
		{
			throw new AssertionError(unsupportedEncodingException);
		}		
	}
}
