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
import com.garretwilson.net.*;
import static com.garretwilson.net.http.HTTPConstants.*;
import static com.garretwilson.net.http.HTTPFormatter.*;
import static com.garretwilson.net.http.HTTPParser.*;
import static com.garretwilson.net.URIUtilities.*;
import com.garretwilson.security.DefaultNonce;
import com.garretwilson.security.Nonce;
import com.garretwilson.swing.BasicOptionPane;
import com.garretwilson.swing.PasswordPanel;
import static com.garretwilson.text.CharacterEncodingConstants.*;
import com.garretwilson.util.*;

/**Represents a connection from a client to a server using HTTP over TCP as defined by
<a href="http://www.ietf.org/rfc/rfc2616.txt">RFC 2616</a>,	"Hypertext Transfer Protocol -- HTTP/1.1".
@author Garret Wilson
*/
public class HTTPClientTCPConnection
{

	/**The URI to connect to, or to which the connection has been permanently directed.*/
//G***del	private URI uri;

		/**@return The URI to connect to, or to which the connection has been permanently directed.*/
//G***del		public URI getURI() {return uri;}

	/**The final endpoint URI of the connection, reflecting any temporary redirects.*/
//G***del	private URI finalURI;

		/**The final endpoint URI of the connection, reflecting any temporary redirects.*/
//G***del		private URI finalURI;

	/**The host to which to connect.*/
	private final Host host;

		/**@return The host to which to connect.*/
		public Host getHost() {return host;}

	/**The socket to the server.*/
	private Socket socket=null;

		/**@return The socket to the server.*/
		private Socket getSocket() {return socket;}

	/**The channel to the server.*/
//G***del	private SocketChannel channel=null;

		/**@return The channel to the server.*/
//G***del		private Channel getChannel() {return channel;}

	/**@return The IP address to which the socket is connected, or <code>null</code> if this connection is not connected.*/
	protected InetAddress getInetAddress() {return socket.getInetAddress();}
		
	/**@return <code>true</code> if the connection is currently open.*/
	public boolean isConnected()
	{
		return socket!=null && socket.isConnected();
	}

	/**Connects to the host.
	If the connection is already connected to the host, no action is taken.
	@throws IOException If there is an error connecting to the host.
	@see #getHost()
	*/
	public void connect() throws IOException
	{
		connect(getHost());	//connect to the host
	}
	
	/**Connects to a specified host.
	Useful for when the connection needs to redirect to another host.
	If the connection is already connected to the same host, no action is taken.
	@param host The host to which to connect.
	@throws IOException If there is an error connecting to the host.
	*/
	protected void connect(final Host host) throws IOException
	{
		if(!isConnected() || !getHost().equals(host))	//if we're not connected or we're changing hosts
		{
			disconnect();	//make sure we're disconnected
			final int port=host.getPort();	//get the port, if any
//G***fix			final InetSocketAddress socketAddress=new InetSocketAddress(host.getName(), port>=0 ? port : DEFAULT_PORT);	//create a new socket address
//G***fix			channel=SocketChannel.open(socketAddress);	//open a channel to the address
			socket=new Socket(host.getName(), port>=0 ? port : DEFAULT_PORT);	//open a socket to the host
			//TODO later turn on non-blocking access when we have a separate client which will on a separate thread feed requests and retrieve responses
//G***fix			inputStream=new BufferedInputStream(newInputStream(channel));	//create a new input stream from the channel
//		G***fix			outputStream=new BufferedOutputStream(newOutputStream(channel));	//create a new output stream to the channel
			inputStream=new BufferedInputStream(socket.getInputStream());	//get an input stream from the socket
			outputStream=new BufferedOutputStream(socket.getOutputStream());	//get an output stream from the socket
		}
	}

	/**Disconnects from the host.
	@exception IOException if there is an error disconnecting from the host.
	*/
	public void disconnect() throws IOException
	{
		if(socket!=null)	//if there is a channel
		{
			if(!socket.isClosed())	//if the channel is open
			{
				socket.close();	//close the channel
			}
			socket=null;	//release the channel
			inputStream=null;	//release the input stream
			outputStream=null;	//release the output stream
		}
	}
		
	/**The input stream from the channel.*/
	private InputStream inputStream;

		/**@return The input stream from the channel, connecting if needed.
		@exception IOException if there is an error getting an input stream.
		*/
		protected InputStream getInputStream() throws IOException
		{
			if(!isConnected())	//if we're not connected
			{
				connect();	//connect to the host
			}
			return inputStream;	//return the input stream
		}

	/**The output stream to the channel.*/
	private OutputStream outputStream;

		/**@return The output stream to the channel, connecting if needed.
		@exception IOException if there is an error getting an output stream.
		*/
		protected OutputStream getOutputStream() throws IOException
		{
			if(!isConnected())	//if we're not connected
			{
				connect();	//connect to the host
			}
			return outputStream;	//return the output stream
		}

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
	@param uri The URI indicating the host to which to connect.
	@exception IllegalArgumentException if the given URI does not contain a valid host.
	*/
	public HTTPClientTCPConnection(final URI uri)
	{
		final String host=uri.getHost();	//get the URI's specified host
		if(host==null)	//if no host is specified
		{
			throw new IllegalArgumentException("URI "+uri+" contains no host.");
		}
		final int port=uri.getPort();	//get the URI's specified port
		this.host=new Host(host, uri.getPort());	//create a new host
	}

	/**Host constructor.
	@param host The host to which to connect.
	*/
	public HTTPClientTCPConnection(final Host host)
	{
		this.host=host;	//save the host
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
Debug.trace("writing request");
			writeRequest(request);	//write the request
Debug.trace("reading response");
			HTTPResponse response=readResponse(request);	//read the response TODO check for redirects
Debug.trace("response connection header:", response.getConnection());
			while(response.getStatusCode()==SC_UNAUTHORIZED)	//if the request requires authorization
			{
				disconnect();	//disconnect from the server so that the server won't time out while we look for credentials and throw a SocketException TODO improve
Debug.trace("unauthorized; looking for challenge");
				final AuthenticateChallenge challenge=response.getWWWAuthenticate();	//get the challenge
				if(challenge!=null)	//if there is a challenge
				{
Debug.trace("found a challenge");
					final AuthenticationScheme scheme=challenge.getScheme();	//get the scheme
					if(scheme==AuthenticationScheme.DIGEST)	//digest
					{
Debug.trace("found a digest challenge");
						final DigestAuthenticateChallenge digestChallenge=(DigestAuthenticateChallenge)challenge;	//get the challenge as a digest challenge
						PasswordAuthentication passwordAuthentication=null;	//we'll try to get password authentication from somewhere
						final URI rootURI=getRootURI(request.getURI());	//get the root URI of the host we were trying to connect to
						final String realm=digestChallenge.getRealm();	//get the
						final HTTPClient client=HTTPClient.getInstance();	//get the client with which we're associated TODO later store the client locally
						final Set<String> usernames=client.getUsernames(rootURI, realm);	//get users that are cached for this domain and realm
						if(usernames.size()==1)	//if we have exactly one user's authentication information
						{
							final String username=usernames.iterator().next();	//get the username
							final char[] password=client.getPassword(rootURI, realm, username);	//get the password for this user
							if(password!=null)	//if we found a cached password
							{
								passwordAuthentication=new PasswordAuthentication(username, password);	//create new password authentication
							}
						}
						if(passwordAuthentication==null)	//if we have no password authentication, yet
						{
							passwordAuthentication=askPasswordAuthentication(request, response, challenge);	//ask for a password
						}
						if(passwordAuthentication!=null)	//if we got authentication
						{
							//TODO make sure that QOP.AUTH is allowed in the challenge
							if(++nonceCount>3)	//increase our nonce count; only allow three attempts
							{
								break;	//G***testing
							}
							final Nonce cnonce=new DefaultNonce(getClass().getName());	//create our own nonce value to send back
								//generate credentials for the client
							final DigestAuthenticateCredentials digestCredentials=new DigestAuthenticateCredentials(request.getMethod(),
									passwordAuthentication.getUserName(), digestChallenge.getRealm(), passwordAuthentication.getPassword(),
									digestChallenge.getNonce(), request.getRequestURI(), "cnonce", digestChallenge.getOpaque(), QOP.AUTH, nonceCount,
									digestChallenge.getMessageDigest().getAlgorithm());	//TODO fix cnonce
							request.setAuthorization(digestCredentials);	//store the credentials in the request
							writeRequest(request);	//write the modified request
							response=readResponse(request);	//read the new response
							if(response.getResponseClass()==HTTPResponseClass.SUCCESS)	//if we succeeded
							{
								client.putPassword(rootURI, realm, passwordAuthentication.getUserName(), passwordAuthentication.getPassword());	//cache the username and password in the client
							}
							else if(response.getStatusCode()==SC_UNAUTHORIZED)	//if we're still unauthorized
							{
								client.removePassword(rootURI, realm, passwordAuthentication.getUserName());	//make sure there is no password cached for this user
							}
						}
						else	//if we can't get a password
						{
							break;	//we can't authenticate without a password
						}
					}
					else	//if we don't recognize the scheme
					{
						break;	//we can't authenticate ourselves
					}
				}
				else	//if there is no challenge
				{
					break;	//we can't authenticate without a challenge
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
	If persistent connections are not supported, the connection will be disconnected.
	@param request The request in which the response is a response.
	@return A response parsed from the input stream data.
	@exception IOException if there is an error reading the data.
	*/
	protected HTTPResponse readResponse(final HTTPRequest request) throws IOException
	{
		try
		{
	Debug.trace("before reading response, are we connected?", isConnected());
			final InputStream inputStream=getInputStream();	//get the input stream of the response
			final HTTPStatus status=parseStatusLine(inputStream);	//parse the status line
	Debug.trace("response status:", status);
			final HTTPResponse response=new DefaultHTTPResponse(status.getVersion(), status.getStatusCode(), status.getReasonPhrase());	//create a new response TODO use a factory
	Debug.trace("created response; now parsing headers");
			for(final NameValuePair<String, String> header:parseHeaders(inputStream))	//parse the headers
			{
	Debug.trace("header", header.getName(), header.getValue());
				response.addHeader(header.getName(), header.getValue());	//add this header to the response
			}
			response.setBody(readResponseBody(inputStream, request, response));	//read and set the response body
			return response;	//return the response
		}
		finally
		{
			if(request.isConnectionClose())	//if the response asks us to close
			{
				disconnect();	//always disconnect from the host
			}
			else	//G***del
			{
				Debug.trace("staying alive");
			}
		}
	}

	/**Reads the body of a response from an input stream.
	No content will be read in response to a HEAD method, as per RFC 2616, 9.4.
	@param inputStream The input stream containing the response body.
	@param request The request in which the response is a response.
	@param response The response for which a body should be read.
	@return The contents of the response body.
	@exception EOFException if the end of the stream was unexpectedly reached.
	@exception IOException if there is an error reading the data.
	*/
	protected byte[] readResponseBody(final InputStream inputStream, final HTTPRequest request, final HTTPResponse response) throws EOFException, IOException
	{
		if(HEAD_METHOD.equals(request.getMethod()))	//if this is the HEAD method
		{
			return HTTPMessage.NO_BODY;	//the HEAD method will never send content, even if there is a Content-Length header
		}
		else	//if this is any other method
		{
			try
			{
				final long contentLength=response.getContentLength();	//get the content length
	Debug.trace("content length", contentLength);
				if(contentLength>=0)	//if there is a content length
				{
					assert contentLength<=Integer.MAX_VALUE : "Unsupported content length.";
					final byte[] responseBody=InputStreamUtilities.getBytes(inputStream, (int)contentLength);
					if(responseBody.length==contentLength)	//if we read all the response body
					{
						return responseBody;	//return the response body
					}
					else	//if we couldn't read the entire body
					{
						throw new EOFException("Only read "+responseBody.length+" of "+contentLength+" expected content bytes.");	//show that we reached the end of the stream
					}
				}
				else	//if there is no content length
				{		
					throw new UnsupportedOperationException("Missing content length in HTTP response.");	//TODO fix chunked type
				}
			}
			catch(final SyntaxException syntaxException)	//if there is a syntax error
			{
				throw new ParseIOException(syntaxException.getMessage());	//TODO fix better parse IO exception
			}
		}
	}
	
	/**Writes a request to the output stream.
	A connection will be made to the appropriate host if needed.
	The request's Host header will be updated.
	@param request The request to write.
	@exception IOException if there is an error writing the data.
	*/
	protected void writeRequest(final HTTPRequest request) throws IOException
	{
		final URI uri=request.getURI();	//get the URI of the request object
		final String requestURI=getRawPathQueryFragment(uri);	//get the unencoded path?query#fragment
		request.setRequestURI(requestURI);	//set the request-uri 
		final Host host=URIUtilities.getHost(uri);	//get the host
		request.setHost(host);	//set the host header to be identical to the host in our request URI
		final byte[] requestBody=request.getBody();	//get the request body
/*G***del when works; maybe check and give an error if the content length doesn't reflect the length of the body
		if(requestBody.length>0)	//if there is a request body
		{
			request.setContentLength(requestBody.length);	//set the content length
		}
		else	//if there is no request body
		{
			request.removeHeaders(CONTENT_LENGTH_HEADER);	//remove the content length header
		}
*/
/*G***del
		if(requestBody!=null)	//if there is a request body
		{
			request.setContentLength(requestBody.length);	//set the content length
		}
		else	//if there is no request body
		{
			request.removeHeaders(CONTENT_LENGTH_HEADER);	//remove the content length header
		}
*/
		final StringBuilder headerBuilder=new StringBuilder();	//create a new string builder for formatting the headers
		formatRequestLine(headerBuilder, request.getMethod(), requestURI, request.getVersion());	//Request-Line
		for(final NameValuePair<String, String> header:request.getHeaders())	//look at each header
		{
			formatHeaderLine(headerBuilder, header);	//format this header line
		}
		headerBuilder.append(CRLF);	//append a blank line, signifying the end of the headers
Debug.trace(headerBuilder);
		connect(host);	//make sure we're connected to the same host as the request
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
/*G***del
	public void close() throws IOException
	{
		getChannel().close();	//close the channel
	}
*/

	
	public PasswordAuthentication askPasswordAuthentication(final HTTPRequest request, final HTTPResponse reseponse, final AuthenticateChallenge challenge)
	{
		final StringBuilder promptBuilder=new StringBuilder("Please enter a username and password");	//TODO i18n
		final String realm=challenge.getRealm();	//get the realm
		if(realm!=null)	//if we know the realm
		{
			promptBuilder.append(" for \""+realm+"\"");	//indicate the realm TODO i18n
		}
		promptBuilder.append('.');		
		final Authenticable authenticator=HTTPClient.getInstance().getAuthenticator();	//see if an authenticator has been specified TODO later keep a copy of which HTTPClient created this connection
		if(authenticator!=null)	//if we have an authenticator
		{
			return authenticator.getPasswordAuthentication(request.getURI(), promptBuilder.toString()); 
		}
		else
		{
			/*G***fix
			final DigestAuthenticateChallenge digestChallenge=(DigestAuthenticateChallenge)challenge;	//get the challenge as a digest challenge
		final Host host=request.getHost();	//get the host of the request, as we may have been redirected
		final int port=host.getPort()>=0 ? host.getPort() : DEFAULT_PORT;	//TODO maybe force host to have a port
Debug.trace("getting password authentication");
final PasswordAuthentication passwordAuthentication=Authenticator.requestPasswordAuthentication(host.getName(), getInetAddress(), port,
response.getVersion().toString(), "You must enter a username and password to access this resource at \""+digestChallenge.getRealm()+"\".", digestChallenge.getScheme().toString());
Debug.trace("got password authentication", passwordAuthentication);
*/
		}
		return null;	//we could not determine password authentication
	}
}
