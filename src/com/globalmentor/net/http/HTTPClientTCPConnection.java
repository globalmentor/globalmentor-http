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

import java.net.*;

import java.security.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.ssl.*;

import com.globalmentor.config.ConfigurationException;
import com.globalmentor.io.*;
import com.globalmentor.java.Bytes;
import com.globalmentor.model.NameValuePair;
import com.globalmentor.net.*;
import com.globalmentor.net.http.webdav.WebDAVResource;
import com.globalmentor.security.*;
import com.globalmentor.text.SyntaxException;
import com.globalmentor.text.xml.XMLSerializer;
import com.globalmentor.urf.URFResourceAlteration;
import com.globalmentor.util.*;

import static com.globalmentor.collections.Arrays.*;
import static com.globalmentor.io.Charsets.*;
import static com.globalmentor.java.Objects.*;
import static com.globalmentor.net.URIs.*;
import static com.globalmentor.net.http.HTTP.*;
import static com.globalmentor.net.http.HTTPFormatter.*;
import static com.globalmentor.net.http.HTTPParser.*;
import static com.globalmentor.text.xml.XML.createDocumentBuilder;
import static java.util.Arrays.fill;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

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
<p>This connection also provides a lock that can be used by callallows callers to ensure that only one 
@author Garret Wilson
@see HTTPClient
@see Client#isLogged()
*/
public class HTTPClientTCPConnection
{

	/**The atomic value indicating whether this connections is in the middle of a request/response exchange.*/
	private final AtomicBoolean exchanging;

		/**@return Whether this connections is in the middle of a request/response exchange.*/
		public boolean isExchanging() {return exchanging.get();}

		/**Atomically begins a request/response exchange.
		@return <code>true</code> if an exchange was started, or <code>false</code> if an exchange was already started.
		*/
		public boolean beginExchange()
		{
			return exchanging.compareAndSet(false, true);
		}

		/**Atomically and unconditionally ends a request/response exchange.
		@return <code>true</code> if an exchange was in progress, or <code>false</code> if the exchange was already ended.
		*/
		public boolean endExchange()
		{
			return exchanging.getAndSet(false);
		}

	/**The client with which this connection is associated.*/
	private final HTTPClient client;

		/**@return The client with which this connection is associated.*/
		protected HTTPClient getClient() {return client;}

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
//TODO fix			final InetSocketAddress socketAddress=new InetSocketAddress(host.getName(), port>=0 ? port : DEFAULT_PORT);	//create a new socket address
//TODO fix			channel=SocketChannel.open(socketAddress);	//open a channel to the address
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
//TODO fix			inputStream=new BufferedInputStream(newInputStream(channel));	//create a new input stream from the channel
//TODO fix			outputStream=new BufferedOutputStream(newOutputStream(channel));	//create a new output stream to the channel
			inputStream=new BufferedInputStream(socket.getInputStream());	//get an input stream from the socket
			outputStream=new BufferedOutputStream(socket.getOutputStream());	//get an output stream from the socket
			if(getClient().isLogged())	//if we're using a logged client
			{
				inputStream=new DebugInputStream(inputStream);	//log all communication from the input stream
				outputStream=new DebugOutputStream(outputStream);	//log all communication to the output stream
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

	/**URI constructor.
	@param client The client with which this connection is associated.
	@param uri The URI indicating the host to which to connect.
	@exception NullPointerException if the given client is <code>null</code>.
	@exception IllegalArgumentException if the given URI does not contain a valid host.
	*/
/*TODO del if not needed
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
		this.exchanging=new AtomicBoolean(false);	//by default the connection is not exchanging
		this.client=checkInstance(client, "Client cannot be null");	//save the client
		this.host=checkInstance(host, "Host cannot be null");	//save the host
		this.passwordAuthentication=passwordAuthentication;	//save the authentication, if any
		this.secure=secure;	//save whether the connection should be secure
	}

	/**Writes a request to the output stream along with the given request body.
	A connection will be made to the appropriate host if needed.
	The request's {@value HTTP#HOST_HEADER} header will be updated.
	The request's {@value HTTP#AUTHORIZATION_HEADER} header will be set to cached credentials if possible.
	The request's {@value HTTP#CONTENT_LENGTH_HEADER} header will be updated to the length of the given request body. 
	The request's {@value HTTP#TRANSFER_ENCODING_HEADER} header, if any, will be removed. 
	@param request The request to write.
	@param body The body of the request.
	@throws NullPointerException if the given request and/or body is <code>null</code>.
	@throws IOException if there is an error writing the data.
	*/
	public void writeRequest(final HTTPRequest request, final byte[] body) throws IOException
	{
		request.setContentLength(body.length);	//set the content length
		request.removeHeaders(TRANSFER_ENCODING_HEADER);	//remove any transfer encoding
		writeRequestMessage(request);	//write the request
		final OutputStream outputStream=getOutputStream();	//get the output stream
		outputStream.write(body);	//write the request body
		outputStream.flush();	//flush the data to the server
	}

	/**Writes a request to the output stream.
	A connection will be made to the appropriate host if needed.
	The request's {@value HTTP#HOST_HEADER} header will be updated.
	The request's {@value HTTP#AUTHORIZATION_HEADER} header will be set to cached credentials if possible.
	The request's {@value HTTP#CONTENT_LENGTH_HEADER} header, if any, will be removed 
	The request's {@value HTTP#TRANSFER_ENCODING_HEADER} header will be updated to indicate chunked encoding. 
	The returned output stream should always be closed after reading is finished.
	@param request The request to write.
	@throws NullPointerException if the given request is <code>null</code>.
	@throws IOException if there is an error writing the data.
	*/
	public OutputStream writeRequest(final HTTPRequest request) throws IOException
	{
		request.removeHeaders(CONTENT_LENGTH_HEADER);
		request.setTransferEncoding(CHUNKED_TRANSFER_CODING);
		writeRequestMessage(request);	//write the request
		return new HTTPChunkedOutputStream(getOutputStream(), false);	//return an output stream encoded as HTTP chunks; don't close the underlying stream when finished
	}

	/**Writes a request to the output stream along with the given XML as the request body.
	This is a convenience method that delegates to {@link #writeRequest(HTTPRequest, byte[])}.
	@param request The request to write.
	@param body The body of the request.
	@throws NullPointerException if the given request and/or body is <code>null</code>.
	@throws IOException if there is an error writing the data.
	*/
	public void writeRequest(final HTTPRequest request, final Document document) throws IOException
	{
		
		final ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();	//create a byte array output stream to hold our outgoing data
		try
		{
			new XMLSerializer(true).serialize(document, byteArrayOutputStream, UTF_8_CHARSET);	//serialize the document to the byte array with no byte order mark
			final byte[] bytes=byteArrayOutputStream.toByteArray();	//get the bytes we serialized
			writeRequest(request, bytes);	//write the request; don't stream the body, because it should be so short that we can deliver it all in one go
		}
		finally
		{
			byteArrayOutputStream.close();	//always close the stream as good practice			
		}
/*alternate method using chunks:
		new XMLSerializer(true).serialize(document, writeRequest(request), UTF_8_CHARSET);	//serialize the document to the byte array with no byte order mark
*/
	}

	/**Writes a request without a body to the output stream.
	A connection will be made to the appropriate host if needed.
	The request's {@value HTTP#HOST_HEADER} header will be updated.
	The request's {@value HTTP#AUTHORIZATION_HEADER} header will be set to cached credentials if possible.
	No message body will be written.
	@param request The request to write.
	@throws NullPointerException if the given request is <code>null</code>.
	@throws IOException if there is an error writing the data.
	*/
	protected void writeRequestMessage(final HTTPRequest request) throws IOException
	{
		//if there is connection-specific password authentication, see if we can send it proactively TODO improve entire caching scheme to cache basic/digest preference in client after first failure; the current technique will fail the first time, anyway, as we don't know the realms
		PasswordAuthentication passwordAuthentication=getPasswordAuthentication();	//see if password authentication has been specified specifically for this connection
		if(passwordAuthentication!=null)	//if we have connection-specific password authentication, try to find what realm to use
		{
			final URI rootURI=getRootURI(request.getURI());	//get the root URI of the host we were trying to connect to
			final Set<String> realms=client.getRealms(rootURI);	//get all the known realms for this domain
			if(!realms.isEmpty())	//if there are any realms we know about
			{
				final String realm=realms.iterator().next();	//get the first realm TODO this call can be improved
				final AuthenticateCredentials credentials=new BasicAuthenticateCredentials(passwordAuthentication.getUserName(), realm, passwordAuthentication.getPassword());	//create basic authentication credentials TODO somehow cache in the client whether basic or digest credentials are preferred; improve the entire client caching mechanism 
				request.setAuthorization(credentials);	//store the credentials in the request
			}
		}
		connect();	//connect if needed
		final URI uri=request.getURI();	//get the URI of the request object
		final String requestURI=getRawPathQueryFragment(uri);	//get the unencoded path?query#fragment
		request.setRequestURI(requestURI);	//set the request-uri 
		final Host host=URIs.getHost(uri);	//get the host
		request.setHost(host);	//set the host header to be identical to the host in our request URI
		final StringBuilder headerBuilder=new StringBuilder();	//create a new string builder for formatting the headers
		formatRequestLine(headerBuilder, request.getMethod(), requestURI, request.getVersion());	//Request-Line
		for(final NameValuePair<String, String> header:request.getHeaders())	//look at each header
		{
			formatHeaderLine(headerBuilder, header);	//format this header line
		}
		headerBuilder.append(CRLF);	//append a blank line, signifying the end of the headers
		connect(host);	//make sure we're connected to the same host as the request
		final OutputStream outputStream=getOutputStream();	//get the output stream
		outputStream.write(headerBuilder.toString().getBytes(UTF_8_CHARSET));	//write the header
		outputStream.flush();	//flush the data to the server
	}
	
	/**Reads a response from the input stream.
	If persistent connections are not supported, the connection will be disconnected.
	The response body is not read.
	@param request The request to which the response is a response.
	@return A response parsed from the input stream data.
	@exception IOException if there is an error reading the data.
	*/
	public HTTPResponse readResponse(final HTTPRequest request) throws IOException
	{
		try
		{
			final InputStream inputStream=getInputStream();	//get the input stream of the response
			final HTTPStatus status=parseStatusLine(inputStream);	//parse the status line
//TODO do something about errors, such as 400 No Host matches server name
			final HTTPResponse response=new DefaultHTTPResponse(status.getVersion(), status.getStatusCode(), status.getReasonPhrase());	//create a new response TODO use a factory
			readHeaders(response);	//read the headers into the response
			return response;	//return the response
		}
		finally
		{
/*TODO put somewhere else
			if(request.isConnectionClose())	//if the response asks us to close
			{
				disconnect();	//always disconnect from the host
			}
			else	//TODO del
			{
//			TODO del 				Debug.trace("staying alive");
			}
*/
		}
	}

	/**Reads headers from the current position in the input stream and places them in the given response.
	@param response The response to contain the read headers.
	@exception IOException if there is an error reading the data.
	*/
	protected void readHeaders(final HTTPResponse response) throws IOException
	{
		for(final NameValuePair<String, String> header:parseHeaders(getInputStream()))	//parse the headers
		{
//TODO del Debug.trace("header", header.getName(), header.getValue());
			response.addHeader(header.getName(), header.getValue());	//add this header to the response
		}
	}

	/**Retrieves an input stream to read the body of the given response.
	The returned input stream should always be closed after reading is finished.
	No content will be return in response to a HEAD request, as per RFC 2616, 9.4.
	The returned input stream will honor the {@value HTTP#CONNECTION_HEADER} response header by closing the connection if appropriate.
	The returned input stream will end the exchange if auto-exchange is enabled.
	@param request The request to which the response is a response.
	@param response The response for which a body should be read.
	@return An input stream providing access to the body of the message.
	@throws IOException if there was an error getting an input stream to the message body.
	@see #disconnect()
	@see #endExchange()
	*/
	public InputStream getResponseBodyInputStream(final HTTPRequest request, final HTTPResponse response) throws IOException
	{
		if(HEAD_METHOD.equals(request.getMethod()))	//if this is the HEAD method
		{
			return new ResponseBodyInputStreamDecorator(new ByteArrayInputStream(Bytes.NO_BYTES), response);	//the HEAD method will never send content, even if there is a Content-Length header TODO make an EmptyInputStream; make a static instance and place it in InputStreams
		}
		return new ResponseBodyInputStreamDecorator(getBodyInputStream(response), response);
	}

	/**Retrieves an input stream to read the body of the given message.
	The returned input stream should always be closed after reading is finished.
	@param message The message for which a body input stream should be retrieved.
	@return An input stream providing access to the body of the message.
	@throws IOException if there was an error getting an input stream to the message body.
	*/
	protected InputStream getBodyInputStream(final HTTPMessage message) throws IOException
	{
		final String[] transferEncoding=message.getTransferEncoding();	//get the list of transfer encodings, which takes precedence over any Content-Length header (RFC 2616 4.4.2)
		if(transferEncoding!=null && transferEncoding.length>0 && !contains(transferEncoding, IDENTITY_TRANSFER_CODING))	//if the transfer encoding contains anything other than "identity", use the chunked encoding algorithm to get the body contents (RFC 2616 4.4.2)
		{
			return new HTTPChunkedInputStream(getInputStream(), false);	//return an input stream to the chunked content; use a stream that doesn't close the underlying input stream when finished
		}
		else	//if chunked encoding is not used
		{
			long contentLength;
			try
			{
				contentLength=message.getContentLength();	//get the content length
			}
			catch(final SyntaxException syntaxException)
			{
				throw new IOException(syntaxException);
			}
			if(contentLength<0)	//if there is no content length (Tomcat appears to send send back no content length if there is no message in some cases)
			{
				contentLength=0;	//assume a content length of zero
			}
			if(contentLength>=0)	//if there is a content length
			{
				return new FixedLengthInputStream(getInputStream(), contentLength, false);	//return an input stream only to these bytes; don't close the underlying stream when finished
			}
			else	//if there is no content length
			{		
				throw new UnsupportedOperationException("Missing content length in HTTP response.");
			}
		}
	}

	/**Reads the body of a response from an input stream.
	No content will be read in response to a HEAD method, as per RFC 2616, 9.4.
	@param request The request to which the response is a response.
	@param response The response for which a body should be read.
	@return The contents of the response body.
	@exception EOFException if the end of the stream was unexpectedly reached.
	@exception IOException if there is an error reading the data.
	*/
	public byte[] readResponseBody(final HTTPRequest request, final HTTPResponse response) throws EOFException, IOException
	{
		final InputStream inputStream=getInputStream();	//get the connection's input stream
		if(HEAD_METHOD.equals(request.getMethod()))	//if this is the HEAD method
		{
			return Bytes.NO_BYTES;	//the HEAD method will never send content, even if there is a Content-Length header
		}
		else	//if this is any other method
		{
			try
			{
				final String[] transferEncoding=response.getTransferEncoding();	//get the list of transfer encodings, which takes precedence over any Content-Length header (RFC 2616 4.4.2)
				if(transferEncoding!=null && transferEncoding.length>0 && !contains(transferEncoding, IDENTITY_TRANSFER_CODING))	//if the transfer encoding contains anything other than "identity", use the chunked encoding algorithm to get the body contents (RFC 2616 4.4.2)
				{
					final ByteArrayOutputStream bodyBuffer=new ByteArrayOutputStream();	//create a buffer in which to store chunks
					byte[] chunk;	//we'll store each chunk here as we read it
					while((chunk=parseChunk(inputStream))!=null)	//read chunks until there are no more chunks
					{
						bodyBuffer.write(chunk);	//add this chunk to the buffer
					}
					readHeaders(response);	//read any post-chunk headers into the response
					afterReadBody(response);	//clean up the connection
					return bodyBuffer.toByteArray();	//return the body we read as chunks
				}
				else	//if chunked encoding is not used
				{
					long contentLength=response.getContentLength();	//get the content length
					if(contentLength<0)	//TODO fix; does Tomcat send back no content length if there is no message? even if the response length is set to zero? is this correct?
					{
						contentLength=0;
					}
					if(contentLength>=0)	//if there is a content length
					{
						assert contentLength<=Integer.MAX_VALUE : "Unsupported content length.";
						final byte[] responseBody=InputStreams.getBytes(inputStream, (int)contentLength);
						afterReadBody(response);	//clean up the connection
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

	/**Reads an XML document from the body of an HTTP response.
	This is a convenience method that delegates to {@link #getResponseBodyInputStream(HTTPRequest, HTTPResponse)}.
	@param request The request to which the response is a response.
	@param response The response for which a body should be read.
	@param namespaceAware <code>true</code> if the document should support for XML namespaces, else <code>false</code>.
	@param validated <code>true</code> if the document should be validated as it is parsed, else <code>false</code>.
	@return A document representing the XML information, or <code>null</code> if there is no content.
	@throws ConfigurationException if a document builder cannot be created which satisfies the configuration requested.
	@throws IOException if there is an error reading the XML.
	*/
	public Document readResponseBodyXML(final HTTPRequest request, final HTTPResponse response, final boolean namespaceAware, final boolean validated) throws ConfigurationException, IOException
	{
		try
		{
			return createDocumentBuilder(namespaceAware, validated).parse(getResponseBodyInputStream(request, response));	//parse the document
		}
		catch(final SAXException saxException)
		{
			throw new IOException(saxException);			
		}
	}
	
	/**Sends a fixed-length request and gets a response.
	This convenience method can retry requests with appropriate authorization if necessary.
	Once the request is successful, the body of the response will still be waiting to be read.
	If an the response results in a corresponding {@link HTTPException}, the response body will be ignored and will no longer be available in the input stream.
	@param request The request to send to the server.
	@param body The body of the request.
	@return The response to get from the server
	@throws NullPointerException if the given request and/or body is <code>null</code>.
	@exception IOException if there is an error writing the request or reading the response.
	*/
	public HTTPResponse sendRequest(final HTTPRequest request, final byte[] body) throws IOException
	{
		long nonceCount=0;	//TODO testing
		try
		{
//		TODO del Debug.trace("writing request");
			writeRequest(request, body);	//write the request along with the request body
//		TODO del Debug.trace("reading response");
			HTTPResponse response=readResponse(request);	//read the response TODO check for redirects
//		TODO del Debug.trace("response connection header:", response.getConnection());
			while(response.getStatusCode()==SC_UNAUTHORIZED)	//if the request requires authorization
			{
				readResponseBody(request, response);	//skip the response body
//TODO fix; del if not needed				disconnect();	//disconnect from the server so that the server won't time out while we look for credentials and throw a SocketException TODO improve
//			TODO del Debug.trace("unauthorized; looking for challenge");
				final AuthenticateChallenge challenge=response.getWWWAuthenticate();	//get the challenge
				if(challenge==null)	//if there is no challenge
				{
					break;	//we can't authenticate without a challenge
				}
				final URI rootURI=getRootURI(request.getURI());	//get the root URI of the host we were trying to connect to
				final AuthenticationScheme scheme=challenge.getScheme();	//get the scheme
				if(scheme!=AuthenticationScheme.BASIC && scheme!=AuthenticationScheme.DIGEST)	//if we don't recognize the scheme
				{
					break;	//we can't authenticate ourselves
				}
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
				if(passwordAuthentication==null)	//if we got no authentication
				{
					break;	//we can't authenticate without a password
				}
				//TODO make sure that QOP.AUTH is allowed in the challenge
				if(++nonceCount>3)	//increase our nonce count; only allow three attempts
				{
					break;	//TODO testing
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
				writeRequest(request, body);	//write the modified request along with the request body
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
			return response;	//return the response we received
		}
		catch(final IllegalArgumentException illegalArgumentException)
		{
			throw new IOException(illegalArgumentException);
		}
		catch(final NoSuchAlgorithmException noSuchAlgorithmException)
		{
			throw new IOException(noSuchAlgorithmException);	//TODO decide if this is the best thing to do here
		}
		catch(final SyntaxException syntaxException)
		{
			throw new IOException(syntaxException);
		}
	}
	
	/**Sends an XML document as the body of a fixed-length request and gets a response.
	This convenience method can retry requests with appropriate authorization if necessary.
	Once the request is successful, the body of the response will still be waiting to be read.
	If an the response results in a corresponding {@link HTTPException}, the response body will be ignored and will no longer be available in the input stream.
	This method delegates to {@link #sendRequest(HTTPRequest, byte[])}. 
	@param request The request to send to the server.
	@param document The XML document to place in the body of the request.
	@return The response to get from the server
	@throws NullPointerException if the given request and/or document is <code>null</code>.
	@exception IOException if there is an error writing the request or reading the response.
	*/
	public HTTPResponse sendRequest(final HTTPRequest request, final Document document) throws IOException
	{
		final ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();	//create a byte array output stream to hold our outgoing data
		try
		{
			new XMLSerializer(true).serialize(document, byteArrayOutputStream, UTF_8_CHARSET);	//serialize the document to the byte array with no byte order mark
			final byte[] bytes=byteArrayOutputStream.toByteArray();	//get the bytes we serialized
			return sendRequest(request, bytes);	//set the bytes of the XML document into the body of the message
		}
		finally
		{
			byteArrayOutputStream.close();	//always close the stream as good practice			
		}
	}
	
	/**Closes the connection.
	@exception IOException if there is a problem closing the connection.
	*/
/*TODO del
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

	/**Cleans up the object upon garbage collection.
	This version attempts to disconnect if the connection if open.
	*/
	protected void finalize() throws Throwable
	{
		try
		{
			disconnect();	//try to disconnect if we need to
		}
		finally
		{
			super.finalize();
		}
	}


	/**Cleans up the connection after reading a response body.
	The connection is closed if requested.
	If auto-exchange is enabled, the exhange is ended.
	@param response The response in an HTTP exchange for which this input stream reads the body.
	@throws NullPointerException if the given response is <code>null</code>.
	@throws IOException if there is an error cleaning up the connection.
	@see #disconnect()
	@see #endExchange()
	*/
	protected void afterReadBody(final HTTPResponse response) throws IOException
	{
		if(response.isConnectionClose())	//if the response asks us to close
		{
			disconnect();	//disconnect from the host
		}
  	if(true/*TODO fix: isAutoExchange()*/)	//if auto-exchange is turned on
  	{
  		endExchange();	//end the request/response exchange
  	}
	}

	/**Creates an output stream that cleans up the connection after reading a response body.
	The connection is closed if requested.
	If auto-exchange is enabled, the exhange is ended.
	@see HTTPClientTCPConnection#afterReadBody(HTTPResponse)
	@author Garret Wilson
	*/
	protected class ResponseBodyInputStreamDecorator extends InputStreamDecorator<InputStream>
	{

		/**The response in an HTTP exchange for which this input stream reads the body.*/
		private final HTTPResponse response;

			/**@return The response in an HTTP exchange for which this input stream reads the body.*/
			public HTTPResponse getResponse() {return response;}
	
		/**Decorates the given output stream.
		@param outputStream The output stream to decorate
		@param response The response in an HTTP exchange for which this input stream reads the body.
		@throws NullPointerException if the given output stream and/or response is <code>null</code>.
		*/
		public ResponseBodyInputStreamDecorator(final InputStream outputStream, final HTTPResponse response)
		{
			super(outputStream);	//construct the parent class
			this.response=checkInstance(response, "Response cannot be null.");
		}
	
	  /**Called after the stream is successfully closed.
		This version closes the connection if requested.
		If auto-exchange is enabled, the exhange is ended.
		@throws IOException if there is an error cleaning up the connection.
		@see HTTPClientTCPConnection#afterReadBody(HTTPResponse)
		*/
	  protected void afterClose() throws IOException
	  {
	  	afterReadBody(response);	//clean up the connection
	  }
	}

}
