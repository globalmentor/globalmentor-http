package com.garretwilson.net.http;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import static java.nio.channels.Channels.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;

import com.garretwilson.io.InputStreamUtilities;
import com.garretwilson.io.ParseIOException;
import static com.garretwilson.io.InputStreamUtilities.*;
import com.garretwilson.net.Host;
import static com.garretwilson.net.http.HTTPConstants.*;
import static com.garretwilson.net.http.HTTPFormatter.*;
import static com.garretwilson.net.http.HTTPParser.*;
import static com.garretwilson.net.URIUtilities.*;
import static com.garretwilson.text.CharacterEncodingConstants.*;
import com.garretwilson.util.Debug;
import com.garretwilson.util.NameValuePair;
import com.garretwilson.util.SyntaxException;

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

	/**Convenience method to sends a request and get a response.
	@param request The request to send to the server.
	@return The response to get from the server
	@exception IOException if there is an error writing the request or reading the response.
	@exception IllegalStateException If other requests have been queued, and the next response
		would not correspond to the provided request.
	*/
	public HTTPResponse sendRequest(final HTTPRequest request) throws IOException, IllegalStateException
	{
		if(getRequestQueue().size()!=0)	//if the request queue is not empty
		{
			throw new IllegalStateException("Queued requests prevent retrieval of response for provided request.");
		}
		addRequest(request);	//add the request to the queue
		return getResponse();	//get and return the next response
	}

	/**Queues an HTTP request for sending.
	@param request The HTTP request to send.
	*/
	public void addRequest(final HTTPRequest request)
	{
		getRequestQueue().add(request);	//add this request to our queue of outgoing requests
	}

	public HTTPResponse getResponse() throws IOException, NoSuchElementException
	{
Debug.trace("getting response");
		final HTTPRequest request=getRequestQueue().remove();	//get the next request
		long nonceCount=0;	//G***testing
		try
		{
			writeRequest(request);	//write the request
			HTTPResponse response=readResponse();	//read the response TODO check for redirects
			while(response.getStatusCode()==SC_UNAUTHORIZED)	//if the request requires authorization
			{
				final AuthenticateChallenge challenge=response.getWWWAuthenticate();	//get the challenge
				final AuthenticationScheme scheme=challenge.getScheme();	//get the scheme
				if(scheme==AuthenticationScheme.DIGEST)	//digest
				{
					final DigestAuthenticateChallenge digestChallenge=(DigestAuthenticateChallenge)challenge;	//get the challenge as a digest challenge
					//TODO make sure that QOP.AUTH is allowed in the challenge
					final String username="user1";	//TODO get these from somewhere else
					final char[] password={'1'};
					if(++nonceCount>3)	//increase our nonce count; only allow three attempts
					{
						break;	//G***testing
					}
						//generate credentials for the client
					final DigestAuthenticateCredentials digestCredentials=new DigestAuthenticateCredentials(request.getMethod(), username, digestChallenge.getRealm(), password,
							digestChallenge.getNonce(), request.getURI(), "cnonce", digestChallenge.getOpaque(), QOP.AUTH, nonceCount, digestChallenge.getMessageDigest().getAlgorithm());	//TODO fix cnonce
					request.setAuthorization(digestCredentials);	//store the credentials in the request 
					writeRequest(request);	//write the modified request
					response=readResponse();	//read the new response
				}
				else	//if we don't recognize the scheme
				{
					break;	//we can't authenticate ourselves
				}
			}
Debug.trace("ready to check response status");
			response.checkStatus();	//check the status of our response before return it
Debug.trace("ready to send back response");			
			return response;	//return the response we received
		}
		catch(final IllegalArgumentException illegalArgumentException)
		{
			throw (IOException)new IOException(illegalArgumentException.getMessage()).initCause(illegalArgumentException);
		}
		catch(final NoSuchAlgorithmException noSuchAlgorithmException)
		{
			throw (IOException)new IOException(noSuchAlgorithmException.getMessage()).initCause(noSuchAlgorithmException);	//TODO decide if this is the best thing to do here
		}
		catch(final SyntaxException syntaxException)
		{
			throw (IOException)new IOException(syntaxException.getMessage()).initCause(syntaxException);
		}
	}

	/**Reads a response from the input stream.
	@return A response parsed from the input stream data.
	@exception IOException if there is an error writing the data.
	*/
	protected HTTPResponse readResponse() throws IOException
	{
		final InputStream inputStream=getInputStream();	//get the input stream of the response
		final HTTPStatus status=parseStatusLine(inputStream);	//parse the status line
		final HTTPResponse response=new DefaultHTTPResponse(status.getVersion(), status.getStatusCode(), status.getReasonPhrase());	//create a new response TODO use a factory
		for(final NameValuePair<String, String> header:parseHeaders(inputStream))	//parse the headers
		{
			response.addHeader(header.getName(), header.getValue());	//add this header to the response
		}
		try
		{
			final long contentLength=response.getContentLength();	//get the content length
			if(contentLength>=0)	//if there is a content length
			{
				assert contentLength<=Integer.MAX_VALUE : "Unsupported content length.";
				final byte[] responseBody=InputStreamUtilities.getBytes(inputStream, (int)contentLength);
				response.setBody(responseBody);	//set the body we read
			}
			else	//if there is no content length
			{		
				throw new UnsupportedOperationException("Missing content length in HTTP response.");	//TODO fix chunked type
			}
			return response;	//return the response
		}
		catch(final SyntaxException syntaxException)	//if there is a syntax error
		{
			throw new ParseIOException(syntaxException.getMessage());	//TODO fix better parse IO exception
		}
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
		final Host host=getHost(uri);	//get the host
		request.setHost(host);	//set the host header to be identical to the host in our request URI
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
		outputStream.flush();	//flush the data to the server
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

	/**Closes the connection.
	@exception IOException if there is a problem closing the connection.
	*/
	public void close() throws IOException
	{
		getChannel().close();	//close the channel
	}
}
