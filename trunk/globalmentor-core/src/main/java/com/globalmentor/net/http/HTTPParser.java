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
import java.security.NoSuchAlgorithmException;
import java.util.*;
import static java.util.Collections.*;
import java.util.regex.Matcher;

import com.globalmentor.io.InputStreams;
import com.globalmentor.io.ParseIOException;
import com.globalmentor.io.ParseReader;
import com.globalmentor.model.NameValuePair;

import static com.globalmentor.security.MessageDigests.*;
import static com.globalmentor.text.CharacterEncoding.*;

import com.globalmentor.text.CharacterEncoding;
import com.globalmentor.text.SyntaxException;
import com.globalmentor.util.*;

import static com.globalmentor.collections.Maps.*;
import static com.globalmentor.java.Booleans.*;
import static com.globalmentor.java.Bytes.*;
import static com.globalmentor.java.CharSequences.*;
import static com.globalmentor.java.Characters.*;
import static com.globalmentor.java.StringBuilders.*;
import static com.globalmentor.net.http.DigestAuthentication.*;
import static com.globalmentor.net.http.HTTP.*;

/**Parses HTTP content. 
@author Garret Wilson
*/
public class HTTPParser
{
	
	/**Parses the HTTP status line.
	@param inputStream The source of the HTTP message.
	@return An array of parsed headers.
	*/
	public static HTTPStatus parseStatusLine(final InputStream inputStream) throws ParseIOException, EOFException, IOException
	{
		HTTPVersion version=null;
		int statusCode=-1;
		boolean haveStatusCode=false;	//show that we're still building the status code
		final StringBuilder versionBuilder=new StringBuilder();	//build the version
		final StringBuilder statusCodeBuilder=new StringBuilder();	//build the status code
		final ByteArrayOutputStream reasonByteArrayOutputStream=new ByteArrayOutputStream();	//create a dynamic byte array to build the reason phrase
		
		int value;	//we'll keep track of each value we read
		while((value=inputStream.read())>=0)	//read another value; while we haven't reached the end of the data stream
		{
			if(version==null)	//if we're parsing the version
			{
				if(value==CR)	//ignore beginning CRLF sequences to compensate for buggy HTTP 1.0 implementations, as per the HTTP 1.1 specifications
				{
					parseLF(inputStream);	//make sure there is a following LF, but ignore it
				}
				else if(value==LF)	//if we get a bare LF
				{
					throw new ParseIOException("Unexpected LF.");					
				}
				else if(value==SP)	//if we've reached the delimiter
				{
					try
					{
						version=parseVersion(versionBuilder);	//parse the version
					}
					catch(final SyntaxException syntaxException)	//if the version wasn't syntactically correct
					{
						throw new ParseIOException(syntaxException.getMessage());	//make ParseIOException construction better
					}
				}
				else	//if we're still collecting version characters
				{
					versionBuilder.append((char)value);	//there is a byte-to-char equivalence for the version
				}
			}
			else if(!haveStatusCode)	//if we're building the status code
			{
				if(value==SP)	//if we've reached the delimiter
				{
					statusCode=Integer.parseInt(statusCodeBuilder.toString());	//parse the status code
					haveStatusCode=true;	//we're finished building the status code
				}
				else	//if we're still collecting status code characters
				{
					statusCodeBuilder.append((char)value);	//there is a byte-to-char equivalence for the status code
				}
			}
			else	//if we're building the reason phrase
			{
				if(value==CR)	//if we've reached the end of the reason phrase
				{
					parseLF(inputStream);	//make sure there is a following LF, but ignore it
					final String reasonPhrase=new String(reasonByteArrayOutputStream.toByteArray(), HTTP_CHARSET);	//convert the reason phrase to a string
					return new HTTPStatus(version, statusCode, reasonPhrase);	//return the status we parsed
				}
				else	//if we're still collecting reason phrase characters
				{
					reasonByteArrayOutputStream.write((byte)value);	//save the reason phrase byte
				}
			}
		}
		throw new EOFException("Unexpectedly reached end of stream while reading status line.");		
	}
	
	/**Reads a byte expected to be an LF.
	@param inputStream The source of the second half of a CRLF sequence.
	@exception EOFException If there is no more data in the input stream.
	@exception ParseIOException if the next character read is not an LF.
	@exception IOException if there is an error reading the content.
	*/
	protected static void parseLF(final InputStream inputStream) throws EOFException, ParseIOException, IOException	//TODO make sure our byte-level processing doesn't interfere with any UTF-8 encoding
	{
		parseExpectedByte(inputStream, LF);	//parse an expected LF TODO probably remove this method and call parseExpectedByte() directly
	}

	/**Reads a byte sequence expected to be CRLF.
	@param inputStream The source of the second half of a CRLF sequence.
	@exception EOFException If there is no more data in the input stream.
	@exception ParseIOException if the next character read is not an LF.
	@exception IOException if there is an error reading the content.
	*/
	protected static void parseCRLF(final InputStream inputStream) throws EOFException, ParseIOException, IOException	//TODO make sure our byte-level processing doesn't interfere with any UTF-8 encoding
	{
		parseExpectedByte(inputStream, CR);	//parse an expected CR
		parseExpectedByte(inputStream, LF);	//parse an expected LF
	}

	/**Reads an expected byte from an input stream.
	@param inputStream The source of the the byte.
	@param byteValue The byte to expect.
	@exception EOFException If there is no more data in the input stream.
	@exception ParseIOException if the next character read is not an the expected character.
	@exception IOException if there is an error reading the content.
	*/
	protected static void parseExpectedByte(final InputStream inputStream, final int byteValue) throws EOFException, ParseIOException, IOException	//TODO make sure our byte-level processing doesn't interfere with any UTF-8 encoding
	{
		final int value=inputStream.read();	//read the byte
		if(value<0)	//if we reached the end of the file
		{
			throw new EOFException("Unexpectedly reached end of stream while reading line looking for character "+byteValue+" ('"+(char)byteValue+"').");
		}
		else if(value!=byteValue)	//if we found an unknown value
		{
			throw new ParseIOException("Unexpected character "+value+" ('"+(char)value+"') looking for character "+byteValue+" ('"+(char)byteValue+"').");
		}		
	}

	/**Parses the next chunk in a chunked transfer coding sequence.
	@param inputStream The source of the second half of a CRLF sequence.
	@return The next chunk read, or <code>null</code> if the ending, empty chunk was reached.
	@exception EOFException If there is no more data in the input stream.
	@exception ParseIOException if the next character read is not an LF.
	@exception IOException if there is an error reading the content.
	*/
	public static byte[] parseChunk(final InputStream inputStream) throws ParseIOException, EOFException, IOException
	{
		final String chunkSizeLine=parseHeaderLine(inputStream);	//parse a header line TODO should this method be renamed?
		final int extensionDelimiterIndex=chunkSizeLine.indexOf(';');	//see if there is an extension TODO use a constant
		final String chunkSizeString=extensionDelimiterIndex>=0 ? chunkSizeLine.substring(0, extensionDelimiterIndex) : chunkSizeLine;	//get the chunk size string
		final int chunkSize;
		try
		{
			chunkSize=Integer.valueOf(chunkSizeString, 16);	//get the size of the chunk
		}
		catch(final NumberFormatException numberFormatException)	//if the chunk size isn't correctly formatted
		{
			throw new ParseIOException(numberFormatException.toString());
		}
		if(chunkSize>0)	//if a positive chunk size is given
		{
			final byte[] chunk=InputStreams.getBytes(inputStream, chunkSize);	//read this chunk
			parseCRLF(inputStream);	//parse a CRLF, but ignore it
			return chunk;	//return the chunk
		}
		else if(chunkSize==0)	//if the chunk size is zero, there's nothing to read---not even a CRLF
		{
			return null;	//there are no chunks left
		}
		throw new IOException("Illegal chunk size: "+chunkSize);	//a negative chunk size is not allowed
	}	

	/**Parses an HTTP version from the given character sequence.
	@param versionCharSequence The version characters to parse.
	@return An HTTP version object.
	@throws SyntaxException if the HTTP version is not formatted correctly.
	 */
	public static HTTPVersion parseVersion(final CharSequence versionCharSequence) throws SyntaxException
	{
		final int versionBeginIndex=VERSION_IDENTIFIER.length()+1;	//the version begins after the version identifier and separator
			//if the sequence begins with "HTTP/"
		if(startsWith(versionCharSequence, VERSION_IDENTIFIER) && versionCharSequence.length()>versionBeginIndex && versionCharSequence.charAt(versionBeginIndex-1)==VERSION_SEPARATOR)
		{
			final int delimiterIndex=indexOf(versionCharSequence, VERSION_DELIMITER, versionBeginIndex);	//look for the version delimiter
			if(delimiterIndex>versionBeginIndex)	//if we found the delimiter
			{
				try
				{
					final int major=Integer.valueOf(versionCharSequence.subSequence(versionBeginIndex, delimiterIndex).toString());	//parse the major version number
					final int minor=Integer.valueOf(versionCharSequence.subSequence(delimiterIndex+1, versionCharSequence.length()).toString());	//parse the minor version number
					return new HTTPVersion(major, minor);	//return the version number we parsed
				}
				catch(final NumberFormatException numberFormatException)	//if one of the version numbers weren't correctly formatted
				{
					throw new SyntaxException(numberFormatException, versionCharSequence.subSequence(versionBeginIndex, versionCharSequence.length()).toString());
				}
			}
			else	//if we didn't find the delimiter
			{
				throw new SyntaxException("HTTP version missing delimiter '"+VERSION_DELIMITER+"'.", versionCharSequence.toString());
			}
		}
		else	//if the sequence doesn't begin with "HTTP/"
		{
			throw new SyntaxException("HTTP version does not begin with "+VERSION_IDENTIFIER+VERSION_SEPARATOR, versionCharSequence.toString());
		}
	}

	/**Parses HTTP message headers, correctly folding LWS into a single space.
	@param inputStream The source of the HTTP message.
	@return The parsed headers.
	*/
	protected static Iterable<NameValuePair<String, String>> parseHeaders(final InputStream inputStream) throws ParseIOException, EOFException, IOException
	{
		final List<NameValuePair<String, String>> headerList=new ArrayList<NameValuePair<String, String>>();	//create a new list to hold the headers
		StringBuilder lineBuilder=new StringBuilder(parseHeaderLine(inputStream));	//parse the first header line
		while(lineBuilder.length()>0)	//while we haven't hit the empty line (originally containing only CRLF)
		{
			final StringBuilder nextLineBuilder=new StringBuilder(parseHeaderLine(inputStream));	//get the next line
			if(startsWithChar(nextLineBuilder, LWS_CHARS))	//if the new line begins with linear whitespace
			{
				trimBeginning(nextLineBuilder, LWS_CHARS);	//remove all beginning linear whitespace from the new line
				if(!endsWith(lineBuilder, SP))	//if the last line didn't end with a space
				{
					lineBuilder.append(SP);	//add a space to our line builder, as we're collapsing LWS into a single SP
				}
				lineBuilder.append(nextLineBuilder);	//append the next line to the previous one
			}
			else	//if the next line doesn't start with whitespace, it's a true new header (or the empty line); parse the last line 
			{
				final int delimiterIndex=charIndexOf(lineBuilder, DELIMITER_CHARS);	//get the index of the first delimiter
				if(delimiterIndex>=0 && lineBuilder.charAt(delimiterIndex)==HEADER_SEPARATOR)	//if we found the separator
				{
					final String name=lineBuilder.substring(0, delimiterIndex);	//find the name
					lineBuilder.delete(0, delimiterIndex+1);	//remove everything up to and including the delimiter
					trim(lineBuilder, LWS_CHARS);	//trim beginning and ending whitespace
					final String value=lineBuilder.toString();	//the value is whatever is remaining between the whitespace, if any
					headerList.add(new NameValuePair<String, String>(name, value));	//create a new name-value pair and add it to the list
					lineBuilder=nextLineBuilder;	//we'll start from the begining processing the next line next time
				}
				else	//if we didn't find the header separator
				{
					throw new ParseIOException("Header does not contain delimiter '"+HEADER_SEPARATOR+"'.");					
				}
			}
		}
		return headerList;	//return the list of headers
	}

	/**Parses a line of text from a message header, assuming each line ends in CRLF and the content is encoded in the {@value HTTP#HTTP_URI_SCHEME} charset.
	All spaces and horizontal tabs are folded into a single space.
	@param inputStream The source of the HTTP message.
	@return A line of text without the ending CRLF.
	@exception ParseIOException if the line is not properly formatted.
	@exception EOFException If the end of the data string was unexpected reached
		while searching for the end of the line.
	@exception IOException if there is an error reading the content.
	*/
	protected static String parseHeaderLine(final InputStream inputStream) throws ParseIOException, EOFException, IOException	//TODO make sure our byte-level processing doesn't interfere with any UTF-8 encoding
	{
		final ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();	//create a dynamic byte array
		int value;	//we'll keep track of each value we read
		boolean foldingLWS=false;	//whether we are currently folding linear whitespace
		while((value=inputStream.read())>=0)	//read another value; while we haven't reached the end of the data stream
		{
			final byte b=(byte)value;	//cast the value to a byte
			if(b==CR)	//if this is the first half of a CRLF sequence
			{
				parseLF(inputStream);	//make sure there is a following LF, but ignore it
				final byte[] bytes=byteArrayOutputStream.toByteArray();	//get the bytes we collected
				return new String(bytes, HTTP_CHARSET);	//return a string from the UTF-8-encoded bytes
			}
			else if(b==LF)	//if we get a bare LF
			{
				throw new ParseIOException("Unexpected LF.");					
			}
			if(contains(LWS_CHARS, (char)b))	//if this byte is linear white space
			{
				if(!foldingLWS)	//if we haven't started folding linear whitespace, yet
				{
					foldingLWS=true;	//we'll start folding linear whitespace now
					byteArrayOutputStream.write(SP);	//folder all linearwhitespace into a single space; future runs of whitespace will be ignored
				}
			}
			else	//if this is not linear whitespace
			{
				byteArrayOutputStream.write(b);	//write the byte normally
				foldingLWS=false;	//we're not folding linear whitespace, whether we were before or not
			}			
		}
		throw new EOFException("Unexpectedly reached end of stream while reading line looking for CRLF.");
	}

	/**Parses a list of attribute name/value pairs.*/
/*TODO fix
	public final List<NameValuePair<String, String>> parseList(final String string)
	{
		
	}
	@param groupBegins The valid group beginning characters.
	@param groupEnds The valid group ending characters, matching to beginning characters.

	public ReaderTokenizer(final Reader reader, final String delimiters, final String groupBegins, final String groupEnds)
	{
*/

	/**Parses a list of */
/*TODO fix
	public final List<NameValuePair<String, String>> parseList(final String string)
	{
		
	}
*/

	/**Parses a list of strings, reading until the end of the reader is reached.
	@param reader The source of the data.
	@exception IOException if there is an error reading the data.
	@return A list of list element string values.
	*/
	public static String[] parseList(final ParseReader reader) throws IOException
	{
		final List<String> elementList=new ArrayList<String>();	//create a new list to hold our list elements
/*TODO maybe fix for EOL detection later 
		reader.skipCharsEOF(LWS_CHARS);	//skip whitespace until we reach a character or the end of the file
		reader.skipCh
*/
		reader.skipCharsEOF(LWS_CHARS+LIST_DELIMITER);	//skip whitespace and the list delimiter, if there is one (or two or however many)
		while(!reader.isEOF())	//while we haven't reached the end of the file
		{
			final String element=parseListElement(reader);	//parse the next element
			elementList.add(element);	//add this element
			reader.skipCharsEOF(LWS_CHARS+LIST_DELIMITER);	//skip whitespace and the list delimiter, if there is one (or two or however many)
		}
		return elementList.toArray(new String[elementList.size()]);	//return the list of elements we parsed
	}

	/**Parses a list of weighted strings (string with an optional qvalue parameter), reading until the end of the reader is reached.
	If no weight is specified for a list item, it defaults to 1.0.
	Any data after a qvalue in each list element is ignored.
	The returned values are returned in order sorted by weight from lowest to highest.
	@param reader The source of the data.
	@exception IOException if there is an error reading the data.
	*/
	@SuppressWarnings("unchecked")	//we cast to an array of generic objects we have created, which Java cannot check at runtime
	public static WeightedValue<String>[] parseWeightedList(final ParseReader reader) throws IOException
	{
		final List<WeightedValue<String>> elementList=new ArrayList<WeightedValue<String>>();	//create a new list to hold our list elements
		reader.skipCharsEOF(LWS_CHARS+LIST_DELIMITER);	//skip whitespace and the list delimiter, if there is one (or two or however many)
		while(!reader.isEOF())	//while we haven't reached the end of the file
		{
			final String element=parseListElement(reader);	//parse the next element
			final Matcher weightMatcher=LIST_ELEMENT_WEIGHT_PATTERN.matcher(element);	//match on the element
			if(weightMatcher.matches())	//if the element matches
			{
				final String value=weightMatcher.group(1);	//the first group is the value
				final String qvalueString=weightMatcher.group(2);	//the second group is the qvalue, if any
				elementList.add(new WeightedValue<String>(value, qvalueString!=null ? Float.valueOf(qvalueString) : 1.0f));	//add this element, defaulting to 1.0 if no qvalue is specified (we don't need to check for a number format exception, as the regular expression ensures the correct format)
				reader.skipCharsEOF(LWS_CHARS+LIST_DELIMITER);	//skip whitespace and the list delimiter, if there is one (or two or however many)
			}
			else	//if the element doesn't match (this should never happen, because the matcher accepts zero or more occurrences of any character
			{
				throw new AssertionError("Weighted list element "+element+" was expected to match pattern "+LIST_ELEMENT_WEIGHT_PATTERN+".");
			}
		}
		sort(elementList);	//sort the elements by natural order (weight)
		return (WeightedValue<String>[])elementList.toArray(new WeightedValue[elementList.size()]);	//return the list of elements we parsed
	}

	/**Parses a list element from the given reader.
	@param reader The source of the data.
	@exception IOException if there is an error reading the data.
	@return A string list element.
	*/
	public static String parseListElement(final ParseReader reader) throws IOException	//TODO fix to handle embedded quotes
	{
		reader.skipChars(LWS_CHARS);	//skip whitespace
		return reader.readStringUntilCharEOF(LWS_CHARS+LIST_DELIMITER);	//read until we hit a delimiter or the end of the file
	}

	/**Parses a list of attribute name/value pair from the given reader, reading until the end of the reader is reached.
	Quotes are removed from quoted values.
	The parameters are mapped by name and returned.
	@param reader The source of the data.
	@exception IOException if there is an error reading the data.
	@exception IllegalArgumentException if more than one parameter with the same
		name was exists.
	*/
	public static Map<String, String> parseParameterMap(final ParseReader reader) throws IOException, IllegalArgumentException
	{
		final List<NameValuePair<String, String>> parameterList=parseParameters(reader);	//parse the parameters
		final Map<String, String> parameterMap=new HashMap<String, String>();	//create a map for the parameters
		addAll(parameterMap, parameterList);	//add the values to the map
		if(parameterMap.size()<parameterList.size())	//if we had duplicate values
		{
			throw new IllegalArgumentException("Encountered duplicate parameter names.");
		}
		return parameterMap;	//return the parameter map
	}

	/**Parses a list of attribute name/value pair from the given reader, reading until the end of the reader is reached.
	Quotes are removed from quoted values. 
	@param reader The source of the data.
	@exception IOException if there is an error reading the data.
	*/
	public static List<NameValuePair<String, String>> parseParameters(final ParseReader reader) throws IOException
	{
		final List<NameValuePair<String, String>> parameterList=new ArrayList<NameValuePair<String, String>>();	//create a new list to hold our parameters
/*TODO maybe fix for EOL detection later 
		reader.skipCharsEOF(LWS_CHARS);	//skip whitespace until we reach a character or the end of the file
		reader.skipCh
*/
		reader.skipCharsEOF(LWS_CHARS+LIST_DELIMITER);	//skip whitespace and the list delimiter, if there is one (or two or however many)
		while(!reader.isEOF())	//while we haven't reached the end of the file
		{
			final NameValuePair<String, String> parameter=parseParameter(reader);	//parse the next parameter
			parameterList.add(parameter);	//add this parameter
			reader.skipCharsEOF(LWS_CHARS+LIST_DELIMITER);	//skip whitespace and the list delimiter, if there is one (or two or however many)
		}
		return parameterList;	//return the list of parameters we parsed
	}
	
	/**Parses a attribute name/value pair from the given reader.
	Quotes are removed from quoted values. 
	@param reader The source of the data.
	@exception IOException if there is an error reading the data.
	@return A name/value pair representing a parameter.
	*/
	public static NameValuePair<String, String> parseParameter(final ParseReader reader) throws IOException
	{
		reader.skipChars(LWS_CHARS);	//skip whitespace
		final String name=reader.readStringUntilChar(LWS_CHARS+EQUALS_SIGN_CHAR);	//name
		reader.readExpectedChar(EQUALS_SIGN_CHAR);	//=
		reader.skipChars(LWS_CHARS);	//skip whitespace
		final String value;
		if(reader.peekChar()==QUOTE)	//if this value is quoted
		{
			value=parseQuotedString(reader);	//parse the quoted string value
		}
		else	//if the value isn't quoted
		{
			value=reader.readStringUntilCharEOF(DELIMITER_CHARS);	//read until we hit a delimiter or the end of the file TODO make sure this is the correct constant
		}
		return new NameValuePair<String, String>(name, value);	//return the name and value we parsed
	}

	/**Parses a quoted string from the reader and returns the value within the quotes.
	Escaped quotes are correctly parsed.
	@param reader The source of the data.
	@exception IOException if there is an error reading the data.
	*/
	public static String parseQuotedString(final ParseReader reader) throws IOException
	{
		final StringBuilder stringBuilder=new StringBuilder();
		reader.readExpectedChar(QUOTE);	//"
		char nextChar;	//this will be a quote when we finish 
		do
		{
			stringBuilder.append(reader.readStringUntilChar(""+QUOTE+ESCAPE_CHAR));	//find the end of the quoted value or an escape character
			nextChar=reader.peekChar();	//see what the next character is
			if(nextChar==ESCAPE_CHAR)	//if we've run ino an escape character
			{
				if(reader.peek()==QUOTE)	//if this is a quoted pair (\")
				{
					reader.skip(1);	//skip the escape character and continue on
				}
			}
		}
		while(nextChar!=QUOTE);	//keep reading until we reach the ending quote
		reader.readExpectedChar(QUOTE);	//"		
		return stringBuilder.toString();	//return the value we parsed
	}
	
	/**Parses a list of attribute name/value pair from the given reader.
	Quoted values are 
	@param reader The source of the data.
	@exception IOException if there is an error reading the data.
	*/
/*TODO fix
	public List<NameValuePair<String, String>> parseParameters(final ParseReader reader) throws IOException
	{
		reader.skipChars(LWS_CHARS);	//skip whitespace
		final String name=reader.readStringUntilChar(LWS_CHARS+EQUALS_SIGN_CHAR);	//name
		reader.readExpectedChar(EQUALS_SIGN_CHAR);	//=
		reader.skipChars(LWS_CHARS);	//skip whitespace
		if(reader.peekChar()==QUOTE_CHAR)	//if this value is quoted
		{
			
		}
		final String value=reader.readStringUntilChar(LWS_CHARS+EQUALS_SIGN_CHAR);	//name
		
		
		
		
	}
*/

	/**Parses an HTTP header and returns the authenticate credentials.
	This method does not allow the wildcard '*' request-URI for the digest URI parameter.
	@param header The header value.
	@return The credentials from the authorization header.
	@exception SyntaxException if the given header was not syntactically correct.
	@exception IllegalArgumentException if the authorization information is not supported. 
	@see HTTP#AUTHORIZATION_HEADER
	*/
	public static AuthenticateCredentials parseAuthorizationHeader(final CharSequence header) throws SyntaxException, IllegalArgumentException
	{
//TODO del Log.trace("parsing authorization header", header);
		try
		{
			final int schemeDelimiterIndex=indexOf(header, SP);	//find the space between the scheme and the rest of the credentials
			if(schemeDelimiterIndex>=0)	//if we found the scheme delimiter
			{
				final String scheme=header.subSequence(0, schemeDelimiterIndex).toString();	//get the scheme
				final String parameters=header.subSequence(schemeDelimiterIndex+1, header.length()).toString();	//get the rest of the credentials
				switch(AuthenticationScheme.valueOf(scheme.toUpperCase()))	//see which type of authentication scheme this is
				{
					case DIGEST:
						{
							final Map<String, String> parameterMap=parseParameterMap(new ParseReader(parameters));	//parse the parameters into a map
//						TODO del 		Log.trace("parameter map", parameterMap);
							final String username=parameterMap.get(USERNAME_PARAMETER);	//get the username
							if(username==null)	//if no username is present
							{
								throw new SyntaxException(AUTHORIZATION_HEADER+" missing parameter "+USERNAME_PARAMETER, header.toString());
							}
							final String realm=parameterMap.get(REALM_PARAMETER);	//get the realm
							if(realm==null)	//if no realm is present
							{
								throw new SyntaxException(AUTHORIZATION_HEADER+" missing parameter "+REALM_PARAMETER, header.toString());
							}
							final String nonce=parameterMap.get(NONCE_PARAMETER);	//get the nonce
							if(nonce==null)	//if no nonce is present
							{
								throw new SyntaxException(AUTHORIZATION_HEADER+" missing parameter "+NONCE_PARAMETER, header.toString());
							}
							final String digestURIString=parameterMap.get(DIGEST_URI_PARAMETER);	//get the digest URI as a string
							if(digestURIString==null)	//if no digest URI is present
							{
								throw new SyntaxException(AUTHORIZATION_HEADER+" missing parameter "+DIGEST_URI_PARAMETER, header.toString());
							}
//TODO del when works							final URI digestURI=URI.create(digestURIString);	//create a URI from the digest URI string; this will reject the wildcard request URI ('*')
							final String response=parameterMap.get(RESPONSE_PARAMETER);	//get the response
							if(response==null)	//if no response is present
							{
								throw new SyntaxException(AUTHORIZATION_HEADER+" missing parameter "+RESPONSE_PARAMETER, header.toString());
							}
							final String algorithm=parameterMap.get(ALGORITHM_PARAMETER);	//get the algorithm
							final String cnonce=parameterMap.get(CNONCE_PARAMETER);	//get the cnonce
							final String opaque=parameterMap.get(OPAQUE_PARAMETER);	//get the opaque parameter
							final String messageQOPString=parameterMap.get(QOP_PARAMETER);	//get the quality of protection
							final QOP messageQOP=messageQOPString!=null ? QOP.valueOfString(messageQOPString) : null;	//convert the quality of protection from a string to an enum
							final String nonceCountString=parameterMap.get(NONCE_COUNT_PARAMETER);	//get the quality of protection
							if(nonceCountString.length()!=NONCE_COUNT_LENGTH)	//if the nonce count is not of the correct length
							{
								throw new SyntaxException(AUTHORIZATION_HEADER+' '+NONCE_COUNT_PARAMETER+" does not have length "+NONCE_COUNT_LENGTH+".", header.toString());
							}
							final long nonceCount=Long.parseLong(nonceCountString, 16);	//parse the hex nonce count string to a long
							return new DigestAuthenticateCredentials(username, realm, nonce, digestURIString, response, cnonce, opaque, messageQOP, nonceCount, algorithm!=null ? algorithm : MD5_ALGORITHM);
						}
					default:	//if we don't support this authentication scheme
						return null;	//show that we don't support this authentication scheme TODO fix for BASIC and other schemes
				}
			}
			else	//if no scheme delimiter was found
			{
				throw new SyntaxException(AUTHORIZATION_HEADER+" missing scheme delimiter.", header.toString());
			}
		}
		catch(final NoSuchAlgorithmException noSuchAlgorithmException)	//if the algorithm was not supported
		{
			throw new IllegalArgumentException(noSuchAlgorithmException);
		}
		catch(IOException ioException)
		{
			throw new SyntaxException(ioException, header.toString());
		}
	}

	/**Parses an HTTP header and returns the WWW-Authenticate challenge.
	@param header The header value.
	@return The challenge from the authenticate header.
	@exception SyntaxException if the given header did not contain valid information.
	@see HTTP#WWW_AUTHENTICATE_HEADER
	*/
	public static AuthenticateChallenge parseWWWAuthenticateHeader(final CharSequence header) throws SyntaxException, IllegalArgumentException
	{
		try
		{
			final int schemeDelimiterIndex=indexOf(header, SP);	//find the space between the scheme and the rest of the credentials
			if(schemeDelimiterIndex>=0)	//if we found the scheme delimiter
			{
				final String scheme=header.subSequence(0, schemeDelimiterIndex).toString();	//get the scheme
				final String parameters=header.subSequence(schemeDelimiterIndex+1, header.length()).toString();	//get the rest of the credentials
				switch(AuthenticationScheme.valueOf(scheme.toUpperCase()))	//see which type of authentication scheme this is
				{
					case BASIC:
						{
							final Map<String, String> parameterMap=parseParameterMap(new ParseReader(parameters));	//parse the parameters into a map
//TODO del Log.trace("parameter map", parameterMap);
							final String realm=parameterMap.get(REALM_PARAMETER);	//get the realm
							if(realm==null)	//if no realm is present
							{
								throw new SyntaxException(WWW_AUTHENTICATE_HEADER+" missing parameter "+REALM_PARAMETER, header.toString());
							}
							final BasicAuthenticateChallenge basicChallenge=new BasicAuthenticateChallenge(realm);	//create the challenge
							return basicChallenge;	//return the basic authentication challenge
						}
					case DIGEST:
					{
						final Map<String, String> parameterMap=parseParameterMap(new ParseReader(parameters));	//parse the parameters into a map
//TODO del Log.trace("parameter map", parameterMap);
						final String realm=parameterMap.get(REALM_PARAMETER);	//get the realm
						if(realm==null)	//if no realm is present
						{
							throw new SyntaxException(WWW_AUTHENTICATE_HEADER+" missing parameter "+REALM_PARAMETER, header.toString());
						}
//TODO implement domain
						final String nonce=parameterMap.get(NONCE_PARAMETER);	//get the nonce
						if(nonce==null)	//if no nonce is present
						{
							throw new SyntaxException(WWW_AUTHENTICATE_HEADER+" missing parameter "+NONCE_PARAMETER, header.toString());
						}
						final String opaque=parameterMap.get(OPAQUE_PARAMETER);	//get the opaque parameter
						final String staleString=parameterMap.get(STALE_PARAMETER);	//get the stale parameter
						final Boolean stale=staleString!=null ? parseBoolean(staleString) : null;	//get the staleness parameter
						final String algorithm=parameterMap.get(ALGORITHM_PARAMETER);	//get the algorithm
						final String qopOptionsString=parameterMap.get(QOP_PARAMETER);	//get the quality of protection
//TODO implement auth-param
						final DigestAuthenticateChallenge digestChallenge=new DigestAuthenticateChallenge(realm, nonce, opaque, algorithm);	//create the challenge
						if(stale!=null)	//if a stale parameter was given
						{
							digestChallenge.setStale(stale);	//set the staleness
						}
						if(qopOptionsString!=null)	//if quality of protection was indicated
						{
							final String[] qopOptionsStrings=parseList(new ParseReader(qopOptionsString));	//parse the quality of protection identifiers
							final QOP[] qopOptions=new QOP[qopOptionsStrings.length];	//create an array of quality of protection enums
							for(int i=qopOptions.length-1; i>=0; --i)	//look at each quality of protection string
							{
								qopOptions[i]=QOP.valueOfString(qopOptionsStrings[i]);	//convert this quality of protection string to an enum
							}
							digestChallenge.setQOPOptions(qopOptions);	//set the quality of protection options								
						}
						return digestChallenge;	//return the digest authentication challenge
					}
					default:	//if we don't support this authentication scheme
						return null;	//show that we don't support this authentication scheme TODO fix for BASIC and other schemes
				}
			}
			else	//if no scheme delimiter was found
			{
				throw new SyntaxException(AUTHORIZATION_HEADER+" missing scheme delimiter.", header.toString());
			}
		}
		catch(final NoSuchAlgorithmException noSuchAlgorithmException)	//if the algorithm was not supported
		{
			throw new IllegalArgumentException(noSuchAlgorithmException);
		}
		catch(IOException ioException)
		{
			throw new SyntaxException(ioException, header.toString());
		}
	}

}
