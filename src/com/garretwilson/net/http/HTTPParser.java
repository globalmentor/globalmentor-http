package com.garretwilson.net.http;

import java.io.*;
import java.util.*;

import com.garretwilson.io.ParseIOException;
import com.garretwilson.io.ParseReader;
import static com.garretwilson.lang.CharSequenceUtilities.*;
import static com.garretwilson.net.http.HTTPConstants.*;
import static com.garretwilson.text.CharacterConstants.*;
import static com.garretwilson.text.CharacterEncodingConstants.*;
import com.garretwilson.util.*;
import static com.garretwilson.util.MapUtilities.*;

/**Parses HTTP content. 
@author Garret Wilson
*/
public class HTTPParser
{

	
//TODO parse the header lines and fold them if they start with LWS
	
	/**Parses a line of text from a message header, assuming each line ends
	 	in CRLF and the content is encoded in UTF-8.
	All spaces and horizontal tabs are folded into a single space.
	@param inputStream The source of the HTTP message.
	@return A line of text without the ending CRLF.
	@exception ParseIOException if the line is not properly formatted.
	@exception EOFException If the end of the data string was unexpected reached
		while searching for the end of the line.
	@exception IOException if there is an error reading the content.
	*/
	public static String parseLine(final InputStream inputStream) throws ParseIOException, EOFException, IOException	//TODO make sure our byte-level processing doesn't interfere with any UTF-8 encoding
	{
		final ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();	//create a dynamic byte array
//G***del		byte b;	//we'll keep track of each byte we read
		int value;	//we'll keep track of each value we read
		boolean foldingLWS=false;	//whether we are currently folding linear whitespace
		while((value=inputStream.read())>=0)	//read another value; while we haven't reached the end of the data stream
		{
			final byte b=(byte)value;	//cast the value to a byte
			if(b==CR)	//if this is the first half of a CRLF sequence
			{
				final int lfValue=inputStream.read();	//read the LF value
				if(lfValue==LF)	//if we found the LF
				{
					final byte[] bytes=byteArrayOutputStream.toByteArray();	//get the bytes we collected
					return new String(bytes, UTF_8);	//return a string from the UTF-8-encoded bytes
				}
				else if(lfValue<0)	//if we reached the end of the file
				{
					throw new EOFException("Unexpectedly reached end of stream while reading line looking for second half of CRLF.");						
				}
				else	//if we found an unknown value
				{
					throw new ParseIOException("Unexpected character "+(char)lfValue+" following CR.");
				}
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
/*G***fix
	public final List<NameValuePair<String, String>> parseList(final String string)
	{
		
	}
	@param groupBegins The valid group beginning characters.
	@param groupEnds The valid group ending characters, matching to beginning characters.

	public ReaderTokenizer(final Reader reader, final String delimiters, final String groupBegins, final String groupEnds)
	{
*/

	/**Parses a list of */
/*G***fix
	public final List<NameValuePair<String, String>> parseList(final String string)
	{
		
	}
*/

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
		if(addAllValues(parameterMap, parameterList))	//add the values to the map; if we have duplicate values
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
		reader.skipCharsEOF(WHITESPACE_CHARS);	//skip whitespace until we reach a character or the end of the file
		reader.skipCh
*/
		reader.skipCharsEOF(WHITESPACE_CHARS+LIST_DELIMITER);	//skip whitespace and the list delimiter, if there is one (or two or however many)
		while(!reader.isEOF())	//while we haven't reached the end of the file
		{
			final NameValuePair<String, String> parameter=parseParameter(reader);	//parse the next parameters
			parameterList.add(parameter);	//add this parameter
			reader.skipCharsEOF(WHITESPACE_CHARS+LIST_DELIMITER);	//skip whitespace and the list delimiter, if there is one (or two or however many)
		}
		return parameterList;	//return the list of parameters we parsed
	}

	
	/**Parses a attribute name/value pair from the given reader.
	Quotes are removed from quoted values. 
	@param reader The source of the data.
	@exception IOException if there is an error reading the data.
	*/
	public static NameValuePair<String, String> parseParameter(final ParseReader reader) throws IOException
	{
		reader.skipChars(WHITESPACE_CHARS);	//skip whitespace
		final String name=reader.readStringUntilChar(WHITESPACE_CHARS+EQUALS_SIGN_CHAR);	//name
		reader.readExpectedChar(EQUALS_SIGN_CHAR);	//=
		reader.skipChars(WHITESPACE_CHARS);	//skip whitespace
		final String value;
		if(reader.peekChar()==QUOTE)	//if this value is quoted
		{
			value=parseQuotedString(reader);	//parse the quoted string value
		}
		else	//if the value isn't quoted
		{
			value=reader.readStringUntilCharEOF(DELIMITER_CHARS);	//read until we hit a delimiter or the end of the file
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
/*G***fix
	public List<NameValuePair<String, String>> parseParameters(final ParseReader reader) throws IOException
	{
		reader.skipChars(WHITESPACE_CHARS);	//skip whitespace
		final String name=reader.readStringUntilChar(WHITESPACE_CHARS+EQUALS_SIGN_CHAR);	//name
		reader.readExpectedChar(EQUALS_SIGN_CHAR);	//=
		reader.skipChars(WHITESPACE_CHARS);	//skip whitespace
		if(reader.peekChar()==QUOTE_CHAR)	//if this value is quoted
		{
			
		}
		final String value=reader.readStringUntilChar(WHITESPACE_CHARS+EQUALS_SIGN_CHAR);	//name
		
		
		
		
	}
*/

}
