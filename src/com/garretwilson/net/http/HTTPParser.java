package com.garretwilson.net.http;

import java.io.*;
import java.util.*;

import com.garretwilson.io.ParseReader;
import static com.garretwilson.net.http.HTTPConstants.*;
import static com.garretwilson.text.CharacterConstants.*;
import com.garretwilson.util.Debug;
import com.garretwilson.util.NameValuePair;

/**Parses HTTP content. 
@author Garret Wilson
*/
public class HTTPParser
{

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
		if(reader.peekChar()==QUOTE_CHAR)	//if this value is quoted
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
		reader.readExpectedChar(QUOTE_CHAR);	//"
		char nextChar;	//this will be a quote when we finish 
		do
		{
			stringBuilder.append(reader.readStringUntilChar(""+QUOTE_CHAR+ESCAPE_CHAR));	//find the end of the quoted value or an escape character
			nextChar=reader.peekChar();	//see what the next character is
			if(nextChar==ESCAPE_CHAR)	//if we've run ino an escape character
			{
				if(reader.peek()==QUOTE_CHAR)	//if this is a quoted pair (\")
				{
					reader.skip(1);	//skip the escape character and continue on
				}
			}
		}
		while(nextChar!=QUOTE_CHAR);	//keep reading until we reach the ending quote
		reader.readExpectedChar(QUOTE_CHAR);	//"		
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
