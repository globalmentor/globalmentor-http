package com.garretwilson.net.http;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import static java.nio.channels.Channels.*;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.garretwilson.io.*;
import static com.garretwilson.io.InputStreamUtilities.*;
import com.garretwilson.net.*;

import static com.garretwilson.net.URIs.*;
import static com.garretwilson.net.http.HTTPConstants.*;
import static com.garretwilson.net.http.HTTPFormatter.*;
import static com.garretwilson.net.http.HTTPParser.*;
import com.garretwilson.security.DefaultNonce;
import com.garretwilson.security.Nonce;
import com.garretwilson.swing.BasicOptionPane;
import com.garretwilson.swing.PasswordPanel;
import com.garretwilson.text.CharacterEncoding;
import com.garretwilson.text.SyntaxException;

import static com.garretwilson.text.CharacterEncoding.*;

import com.globalmentor.util.*;

import static com.globalmentor.java.Objects.*;
import static com.globalmentor.util.Arrays.*;

/**Represents a connection from a client to a server using HTTP over TCP as defined by
<a href="http://www.ietf.org/rfc/rfc2616.txt">RFC 2616</a>,	"Hypertext Transfer Protocol -- HTTP/1.1".
This client supports logging.
If authentication is needed, authentication is attempted to be retrieved from the following sources, in this order:
<ol>
	<li>The {@link PasswordAuthentication}, if any, associated with this connection.</li>
	<li>The {@link PasswordAuthentication}, if any, cached by the associated client.</li>
	<li>The {@link Authenticable}, if any, specified by the associated client.</li>
	<li>The {@link Authenticable}, if any, specified by the default client.</li>
</ol>
@author Garret Wilson
@see HTTPClient
@see Client#isLogged()
*/
public class HTTPClientTCPConnection
{

	/**The client with which this connection is associated.*/
	private final HTTPClient client;

		/**@return The client with which this connection is associated.*/
		protected HTTPClient getClient() {return client;}

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

	/**Whether the connection is secure.*/
	private final boolean secure;

		/**@return Whether the connection is secure.*/
		public boolean isSecure() {return secure;}
	
	/**The socket to the server.*/
	private Socket socket=null;

		/**@return The socket to the server.*/
		private Socket getSocket() {return socket;}

	/**The channel to the server.*/
//G***del	private SocketChannel channel=null;

		/**@return The channel to the server.*/
//G***del		private Channel getChannel() {return channel;}

		
	/**The connection-specific password authentication, or <code>null</code> if this connection specifies no password authentication.*/
	private final PasswordAuthentication passwordAuthentication;
	
		/**@return The connection-specific password authentication, or <code>null</code> if this connection specifies no password authentication.*/
		protected PasswordAuthentication getPasswordAuthentication() {return passwordAuthentication;}

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
//		TODO del Debug.trace("ready to make connection, with secure:", isSecure());
			if(isSecure())	//if this is a secure connection
			{
					//TODO testing ignore all certificate problems
			  TrustManager[] trustAllCerts = new TrustManager[]{
		        new X509TrustManager() {
		            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		                return null;
		            }
		            public void checkClientTrusted(
		                java.security.cert.X509Certificate[] certs, String authType) {
		            }
		            public void checkServerTrusted(
		                java.security.cert.X509Certificate[] certs, String authType) {
		            }
		        }
		    };
		    
		    // Install the all-trusting trust manager
			  
			  //TODO see http://www.javaalmanac.com/egs/javax.net.ssl/TrustAll.html
			  try
			  {
	        SSLContext sc = SSLContext.getInstance("SSL");
	        sc.init(null, trustAllCerts, new java.security.SecureRandom());
					final SSLSocketFactory sslSocketFactory=sc.getSocketFactory();
					socket=sslSocketFactory.createSocket(host.getName(), port>=0 ? port : DEFAULT_SECURE_PORT);	//open a secure socket to the host
		    }
			  catch(final NoSuchAlgorithmException noSuchAlgorithmException)	//we should always recognize SSL
			  {
			  	throw new AssertionError(noSuchAlgorithmException);
		    }
			  catch(final KeyManagementException keyManagementException)
			  {
			  	throw new AssertionError(keyManagementException);
			  }
/*TODO fix when certificate is renewed
				final SSLSocketFactory sslSocketFactory=(SSLSocketFactory)SSLSocketFactory.getDefault();	//get the default SSL Socket factory TODO maybe keep one of these around for multiple use
				socket=sslSocketFactory.createSocket(host.getName(), port>=0 ? port : DEFAULT_SECURE_PORT);	//open a secure socket to the host
*/
//TODO do extra certificate checks
//TODO see to disable certificate checks: http://www.javaalmanac.com/egs/javax.net.ssl/TrustAll.html
//TODO see http://javaboutique.internet.com/resources/books/JavaNut/javanut3_1.html
//TODO see http://jirc.hick.org/cgi-bin/raffi.cgi?ACTION=VIEW&PAGE=SSL_Java
//TODO see http://javaalmanac.com/egs/javax.net.ssl/Client.html?l=rel
			}
			else	//if this is not a secure connection
			{
				socket=new Socket(host.getName(), port>=0 ? port : DEFAULT_PORT);	//open a socket to the host
			}
			//TODO later turn on non-blocking access when we have a separate client which will on a separate thread feed requests and retrieve responses
//G***fix			inputStream=new BufferedInputStream(newInputStream(channel));	//create a new input stream from the channel
//		G***fix			outputStream=new BufferedOutputStream(newOutputStream(channel));	//create a new output stream to the channel
			inputStream=new BufferedInputStream(socket.getInputStream());	//get an input stream from the socket
			outputStream=new BufferedOutputStream(socket.getOutputStream());	//get an output stream from the socket
			if(getClient().isLogged())	//if we're using a logged client
			{
				inputStream=new LogInputStream(inputStream);	//log all communication from the input stream
				outputStream=new LogOutputStream(outputStream);	//log all communication to the output stream
			}
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
	@param client The client with which this connection is associated.
	@param uri The URI indicating the host to which to connect.
	@exception NullPointerException if the given client is <code>null</code>.
	@exception IllegalArgumentException if the given URI does not contain a valid host.
	*/
/*G***del if not needed
	HTTPClientTCPConnection(final HTTPClient client, final URI uri)
	{
		this.client=client;	//save the client
		final String host=uri.getHost();	//get the URI's specified host
		if(host==null)	//if no host is specified
		{
			throw new IllegalArgumentException("URI "+uri+" contains no host.");
		}
		this.host=URIUtilities.getHost(uri);	//save the host host
	}
*/
		
	/**Host, authenticator, and secure constructor.
	@param client The client with which this connection is associated.
	@param host The host to which to connect.
	@param passwordAuthentication The connection-specific password authentication, or <code>null</code> if there should be no connection-specific password authentication.
	@param secure Whether the connection should be secure.
	@exception NullPointerException if the given client or host is <code>null</code>.
	*/
	HTTPClientTCPConnection(final HTTPClient client, final Host host, final PasswordAuthentication passwordAuthentication, final boolean secure)
	{
		this.client=checkInstance(client, "Client cannot be null");	//save the client
		this.host=checkInstance(host, "Host cannot be null");	//save the host
		this.passwordAuthentication=passwordAuthentication;	//save the authentication, if any
		this.secure=secure;	//save whether the connection should be secure
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
//	TODO del Debug.trace("getting response");
		final HTTPRequest request=getRequestQueue().remove();	//get the next request
		long nonceCount=0;	//G***testing
		try
		{
					//TODO find a way to store authentication information in the request if we already have it

//		TODO del Debug.trace("writing request");
			writeRequest(request);	//write the request
//		TODO del Debug.trace("reading response");
			HTTPResponse response=readResponse(request);	//read the response TODO check for redirects
//		TODO del Debug.trace("response connection header:", response.getConnection());
			while(response.getStatusCode()==SC_UNAUTHORIZED)	//if the request requires authorization
			{
				disconnect();	//disconnect from the server so that the server won't time out while we look for credentials and throw a SocketException TODO improve
//			TODO del Debug.trace("unauthorized; looking for challenge");
				final AuthenticateChallenge challenge=response.getWWWAuthenticate();	//get the challenge
				if(challenge!=null)	//if there is a challenge
				{
//				TODO del Debug.trace("found a challenge");
					final AuthenticationScheme scheme=challenge.getScheme();	//get the scheme
					if(scheme==AuthenticationScheme.BASIC || scheme==AuthenticationScheme.DIGEST)	//if this is basic or digest authentication
					{
//					TODO del Debug.trace("found a basic or digest challenge");
						final URI rootURI=getRootURI(request.getURI());	//get the root URI of the host we were trying to connect to
						final String realm=challenge.getRealm();	//get the challenge realm
						PasswordAuthentication passwordAuthentication=getPasswordAuthentication();	//see if password authentication has been specified specifically for this connection
						if(passwordAuthentication==null)	//if there is no connection-specific password authentication, try to find some
						{
							final HTTPClient client=getClient();	//get the client with which we're associated
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
						}
						if(passwordAuthentication==null)	//if we have no password authentication, yet, either specified for this connection or cached in the client
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
							final AuthenticateCredentials credentials;	//try to get credentials based upon the authentication type
							if(challenge instanceof BasicAuthenticateChallenge)	//if this is basic authentication
							{
								credentials=new BasicAuthenticateCredentials(passwordAuthentication.getUserName(), realm, passwordAuthentication.getPassword());	//create basic authentication credentials
							}
							else if(challenge instanceof DigestAuthenticateChallenge)	//if this is digest authentication
							{
								final DigestAuthenticateChallenge digestChallenge=(DigestAuthenticateChallenge)challenge;	//get the challenge as a digest challenge
								final Nonce cnonce=new DefaultNonce(getClass().getName());	//create our own nonce value to send back
								credentials=new DigestAuthenticateCredentials(request.getMethod(),	//generate credentials for the client
										passwordAuthentication.getUserName(), realm, passwordAuthentication.getPassword(),
										digestChallenge.getNonce(), request.getRequestURI(), "cnonce", digestChallenge.getOpaque(), QOP.AUTH, nonceCount,
										digestChallenge.getMessageDigest().getAlgorithm());	//TODO fix cnonce
							}
							else	//if we don't recognize the challenge type
							{
								throw new AssertionError("Unrecognized challenge type: "+challenge.getClass());
							}
							request.setAuthorization(credentials);	//store the credentials in the request
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
//TODO del Debug.trace("ready to check response status");
			response.checkStatus();	//check the status of our response before return it
//TODO del Debug.trace("ready to send back response");			
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
	Written data will be logged if requested by the client.
	@param request The request in which the response is a response.
	@return A response parsed from the input stream data.
	@exception IOException if there is an error reading the data.
	@see Client#isLogged()
	*/
	protected HTTPResponse readResponse(final HTTPRequest request) throws IOException
	{
		try
		{
//TODO del 	Debug.trace("before reading response, are we connected?", isConnected());
			final InputStream inputStream=getInputStream();	//get the input stream of the response
			final HTTPStatus status=parseStatusLine(inputStream);	//parse the status line
//		TODO del 	Debug.trace("response status:", status);
//TODO do something about errors, such as 400 No Host matches server name
			final HTTPResponse response=new DefaultHTTPResponse(status.getVersion(), status.getStatusCode(), status.getReasonPhrase());	//create a new response TODO use a factory
//		TODO del 	Debug.trace("created response; now parsing headers");
			readHeaders(response);	//read the headers into the response
			response.setBody(readResponseBody(inputStream, request, response));	//read and set the response body
			if(getClient().isLogged() && inputStream instanceof LogInputStream)	//if the client wants logging to occur, and the input stream was already logging
			{
				Debug.log("HTTP Response:\n", new String(((LogInputStream)inputStream).getLoggedBytes(true), UTF_8));	//get the logged bytes, clearing the buffer, and log the bytes as a UTF-8 string
			}
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
//			TODO del 				Debug.trace("staying alive");
			}
		}
	}

	/**Reads headers from the current position in the input stream and places them in the given response.
	@param response The response to contain the read headers.
	@exception IOException if there is an error reading the data.
	*/
	protected void readHeaders(final HTTPResponse response) throws IOException
	{
		for(final NameValuePair<String, String> header:parseHeaders(inputStream))	//parse the headers
		{
//TODO del Debug.trace("header", header.getName(), header.getValue());
			response.addHeader(header.getName(), header.getValue());	//add this header to the response
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
				final String[] transferEncoding=response.getTransferEncoding();	//get the list of transfer encodings, which takes precedence over any Content-Length header (RFC 2616 4.4.2)
				if(transferEncoding!=null && transferEncoding.length>0 && !contains(transferEncoding, IDENTITY_TRANSFER_CODING))	//if the transfer encoding contains anything other than "identity", use the chunked encoding algorithm to get the body contents (RFC 2616 4.4.2)
				{
//TODO del Debug.trace("using chunked encoding");
					final ByteArrayOutputStream bodyBuffer=new ByteArrayOutputStream();	//create a buffer in which to store chunks
					byte[] chunk;	//we'll store each chunk here as we read it
					while((chunk=parseChunk(inputStream))!=null)	//read chunks until there are no more chunks
					{
						bodyBuffer.write(chunk);	//add this chunk to the buffer
					}
//TODO del Debug.trace("reading post-chunk headers");
					readHeaders(response);	//read any post-chunk headers into the response
					return bodyBuffer.toByteArray();	//return the body we read as chunks
				}
				else	//if chunked encoding is not used
				{
//TODO fix					final long contentLength=response.getContentLength();	//get the content length
					long contentLength=response.getContentLength();	//get the content length
//TODO del		Debug.trace("content length", contentLength);
					
					if(contentLength<0)	//TODO fix; does Tomcat send back no content length if there is no message? even if the response length is set to zero? is this correct?
					{
						contentLength=0;
					}

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
			}
			catch(final SyntaxException syntaxException)	//if there is a syntax error
			{
				throw new ParseIOException(syntaxException);
			}
		}
	}
	
	/**Writes a request to the output stream.
	A connection will be made to the appropriate host if needed.
	The request's Host header will be updated.
	Written data will be logged if requested by the client.
	@param request The request to write.
	@exception IOException if there is an error writing the data.
	@see Client#isLogged()
	*/
	protected void writeRequest(final HTTPRequest request) throws IOException
	{
		final URI uri=request.getURI();	//get the URI of the request object
		final String requestURI=getRawPathQueryFragment(uri);	//get the unencoded path?query#fragment
		request.setRequestURI(requestURI);	//set the request-uri 
		final Host host=URIs.getHost(uri);	//get the host
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
//TODO del Debug.trace(headerBuilder);
		connect(host);	//make sure we're connected to the same host as the request
		final OutputStream outputStream=getOutputStream();	//get the output stream
		outputStream.write(getBytes(headerBuilder));	//write the header
		if(requestBody!=null)	//if there is a request body
		{
			outputStream.write(requestBody);	//write the body
		}
		outputStream.flush();	//flush the data to the server
		if(getClient().isLogged() && outputStream instanceof LogOutputStream)	//if the client wants logging to occur, and the output stream was already logging
		{
			Debug.log("HTTP Request:\n", new String(((LogOutputStream)outputStream).getLoggedBytes(true), UTF_8));	//get the logged bytes, clearing the buffer, and log the bytes as a UTF-8 string
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

	/**Closes the connection.
	@exception IOException if there is a problem closing the connection.
	*/
/*G***del
	public void close() throws IOException
	{
		getChannel().close();	//close the channel
	}
*/

	/**Asks a user for password authentication information, using the client's registered authenticator, if any.
	@param request The request for which authentication is required.
	@param reseponse The response indicating authentication is required.
	@param challenge The challenge describing the needed authentication.
	@return Password authentication information from the user, or <code>null</code> if no information was given.
	*/ 
	protected PasswordAuthentication askPasswordAuthentication(final HTTPRequest request, final HTTPResponse reseponse, final AuthenticateChallenge challenge)
	{
		final StringBuilder promptBuilder=new StringBuilder("Please enter a username and password");	//TODO i18n
		final String realm=challenge.getRealm();	//get the realm
		if(realm!=null)	//if we know the realm
		{
			promptBuilder.append(" for \""+realm+"\"");	//indicate the realm TODO i18n
		}
		promptBuilder.append('.');
		return getClient().getPasswordAuthentication(request.getURI(), promptBuilder.toString());	//ask the client to ask the user for a password
	}

}
