package com.garretwilson.net.http;

import java.io.UnsupportedEncodingException;
import java.util.*;

import static java.util.Collections.*;

import static com.garretwilson.net.http.BasicAuthenticationConstants.*;
import static com.garretwilson.net.http.DigestAuthenticationConstants.*;
import static com.garretwilson.net.http.HTTPConstants.*;
import static com.garretwilson.security.MessageDigestUtilities.*;

import com.garretwilson.text.FormatUtilities;
import static com.garretwilson.text.CharacterEncoding.*;
import static com.garretwilson.text.FormatUtilities.*;
import static com.globalmentor.java.Characters.*;

import com.garretwilson.util.Base64;
import com.garretwilson.util.NameValuePair;

/**Class that knows how to format HTTP information.
@author Garret Wilson
*/
public class HTTPFormatter
{

	/**Formats the request line of an HTTP request.
	@param stringBuilder The string builder into which the information should be formatted.
	@param method The HTTP method.
	@param requestURI The absolute URI or absolute path of the request, or '*'.
	@param httpVersion The HTTP version.
	@return The string builder containing the formatted information.
	*/
	public static StringBuilder formatRequestLine(final StringBuilder stringBuilder, final String method, final String requestURI, final HTTPVersion httpVersion)
	{
		stringBuilder.append(method).append(SP).append(requestURI.toString()).append(SP);	//Method SP Request-URI SP
		formatVersion(stringBuilder, httpVersion);	//HTTP-Version
		return stringBuilder.append(CRLF);	//CRLF
	}

	/**Formats an HTTP version in the form <code>HTTP/<var>major</var>.<var>minor</var></code>.
	@param stringBuilder The string builder into which the information should be formatted.
	@param version The HTTP version.
	@return The string builder containing the formatted information.
	*/
	public static StringBuilder formatVersion(final StringBuilder stringBuilder, final HTTPVersion version)
	{
		return stringBuilder.append(VERSION_IDENTIFIER).append(VERSION_SEPARATOR).append(version.getMajor()).append(VERSION_DELIMITER).append(version.getMinor());	//HTTP/major.minor
	}

	/**Formats an HTTP header line in the form <code><var>name</var>: <var>value</var>CRLF</code>.
	@param stringBuilder The string builder into which the information should be formatted.
	@param header The HTTP header.
	@return The string builder containing the formatted information.
	*/
	public static StringBuilder formatHeaderLine(final StringBuilder stringBuilder, final NameValuePair<String, String> header)
	{
		return stringBuilder.append(header.getName()).append(HEADER_SEPARATOR).append(SP).append(header.getValue()).append(CRLF);	//name: SP value
	}

	/**Formats an HTTP list using the format:
	 <var>name</var>="<var>value</value>", <var>name</var>="<var>value</value>"
	@param stringBuilder The formatting destination.
	@param attributes The attributes to format. 
	@return The string builder used for formatting.
	*/
	public static StringBuilder formatAttributeList(final StringBuilder stringBuilder, final NameValuePair<?, ?>... attributes)
	{
		return formatAttributeList(stringBuilder, emptySet(), attributes);	//format the attributes quoting all values
	}
	/**Formats an HTTP list using the format:
	 <var>name</var>="<var>value</value>", <var>name</var>="<var>value</value>"
	@param stringBuilder The formatting destination.
	@param unquotedNames The set of names that should not be quoted.
	@param attributes The attributes to format. 
	@return The string builder used for formatting.
	*/
	public static StringBuilder formatAttributeList(final StringBuilder stringBuilder, final Set<?> unquotedNames, final NameValuePair<?, ?>... attributes)
	{
		return formatAttributes(stringBuilder, LIST_DELIMITER, EQUALS_SIGN_CHAR, QUOTE, unquotedNames, attributes);	//format the attributes using HTTP's list formatting characters
	}

	/**Formats the Authorization header with credentials.
	@param stringBuilder The string builder into which the information should be formatted.
	@param credentials The credentials to present to the server.
	@return The string builder containing the formatted information.
	*/
	public static StringBuilder formatAuthorizationHeader(final StringBuilder stringBuilder, final AuthenticateCredentials credentials)
	{
		final AuthenticationScheme scheme=credentials.getScheme();	//get the authentication scheme
		stringBuilder.append(scheme).append(SP);	//authentication scheme
		if(credentials instanceof BasicAuthenticateCredentials)	//if this is basic credentials
		{
			final BasicAuthenticateCredentials basicCredentials=(BasicAuthenticateCredentials)credentials;	//get the credentials as basic credentials
			final List<NameValuePair<String, String>> parameterList=new ArrayList<NameValuePair<String, String>>();	//create the list of parameters
			final StringBuilder credentialsBuilder=new StringBuilder();	//build basic credentials	//TODO put this in a shared location
			credentialsBuilder.append(basicCredentials.getUsername());	//username
			credentialsBuilder.append(BASIC_DELIMITER);	//:
			credentialsBuilder.append(basicCredentials.getPassword());	//password
			try
			{
				final String base64Credentials=Base64.encodeBytes(credentialsBuilder.toString().getBytes(UTF_8));	//base64-encode the credential string
				stringBuilder.append(base64Credentials);	//append the base64-encoded basic credentials
			}
			catch(final UnsupportedEncodingException unsupportedEncodingException)	//UTF-8 should always be accepted
			{
				throw new AssertionError(unsupportedEncodingException);
			}
		}
		else if(credentials instanceof DigestAuthenticateCredentials)	//if this is digest credentials
		{
			final DigestAuthenticateCredentials digestCredentials=(DigestAuthenticateCredentials)credentials;	//get the credentials as digest credentials
			final List<NameValuePair<String, String>> parameterList=new ArrayList<NameValuePair<String, String>>();	//create the list of parameters
			parameterList.add(new NameValuePair<String, String>(USERNAME_PARAMETER, digestCredentials.getUsername()));	//realm
			parameterList.add(new NameValuePair<String, String>(REALM_PARAMETER, digestCredentials.getRealm()));	//add the realm parameter
			parameterList.add(new NameValuePair<String, String>(NONCE_PARAMETER, digestCredentials.getNonce()));	//nonce
			parameterList.add(new NameValuePair<String, String>(DIGEST_URI_PARAMETER, digestCredentials.getURI().toString()));	//digest-uri	TODO remove toString() when changed from URI to String
			parameterList.add(new NameValuePair<String, String>(RESPONSE_PARAMETER, digestCredentials.getResponse()));	//response
			parameterList.add(new NameValuePair<String, String>(ALGORITHM_PARAMETER, digestCredentials.getMessageDigest().getAlgorithm()));	//algorithm
			final String cnonce=digestCredentials.getCNonce();	//get the cnonce value
			if(cnonce!=null)	//if we have a cnonce value
			{
				parameterList.add(new NameValuePair<String, String>(CNONCE_PARAMETER, cnonce));	//cnonce
			}
			final String opaque=digestCredentials.getOpaque();	//get the opaque value
			if(opaque!=null)	//if we have an opaque value
			{
				parameterList.add(new NameValuePair<String, String>(OPAQUE_PARAMETER, digestCredentials.getOpaque()));	//opaque
			}
			final QOP qop=digestCredentials.getQOP();	//get the quality of protection
			if(qop!=null)	//if a quality of protection was specified
			{
				parameterList.add(new NameValuePair<String, String>(QOP_PARAMETER, qop.toString()));	//message-qop
			}
			final String nonceCount=digestCredentials.getNonceCountString();	//get the nonce count in string format
			if(nonceCount!=null)	//if there is a nonce count
			{
				parameterList.add(new NameValuePair<String, String>(NONCE_COUNT_PARAMETER, nonceCount));	//nonce-count				
			}
//TODO implement auth-param
			final Set<String> unquotedWWWAuthenticateParameters=new HashSet<String>();	//create a set of parameters that should not be quoted, as per RFC 2617
			unquotedWWWAuthenticateParameters.add(ALGORITHM_PARAMETER);	//algorithm
			unquotedWWWAuthenticateParameters.add(NONCE_COUNT_PARAMETER);	//nonce-count
			unquotedWWWAuthenticateParameters.add(QOP_PARAMETER);	//qop
			formatAttributeList(stringBuilder, unquotedWWWAuthenticateParameters, parameterList.toArray(new NameValuePair[parameterList.size()]));	//parameters
		}
		else	//if this is an unsupported challenge
		{
			throw new IllegalArgumentException(scheme.toString());
		}
		return stringBuilder;	//return the string builder we used
	}

	/**Formats the WWW-Authenticate header value challenging the client to authenticate itself.
	@param stringBuilder The string builder into which the information should be formatted.
	@param challenge The authenticate challenge to issue to the client.
	@return The string builder containing the formatted information.
	*/
	public static StringBuilder formatWWWAuthenticateHeader(final StringBuilder stringBuilder, final AuthenticateChallenge challenge)
	{
		final AuthenticationScheme scheme=challenge.getScheme();	//get the authentication scheme
		stringBuilder.append(scheme).append(SP);	//authentication scheme
		if(challenge instanceof DigestAuthenticateChallenge)	//if this is a digest challenge
		{
			final DigestAuthenticateChallenge digestChallenge=(DigestAuthenticateChallenge)challenge;	//get the challenge as a digest challenge
			final List<NameValuePair<String, String>> parameterList=new ArrayList<NameValuePair<String, String>>();	//create the list of parameters
			parameterList.add(new NameValuePair<String, String>(REALM_PARAMETER, digestChallenge.getRealm()));	//realm
				//TODO implement domain
			parameterList.add(new NameValuePair<String, String>(NONCE_PARAMETER, digestChallenge.getNonceDigest()));	//nonce
			final String opaque=digestChallenge.getOpaque();	//get the opaque value
			if(opaque!=null)	//if we have an opaque value
			{
				parameterList.add(new NameValuePair<String, String>(OPAQUE_PARAMETER, opaque));	//opaque
			}
			final boolean stale=digestChallenge.isStale();	//see if staleness should be indicated
			if(stale)	//if an earlier request had a stale nonce
			{
				parameterList.add(new NameValuePair<String, String>(STALE_PARAMETER, Boolean.toString(stale)));	//stale				
			}
			parameterList.add(new NameValuePair<String, String>(ALGORITHM_PARAMETER, digestChallenge.getMessageDigest().getAlgorithm()));	//algorithm
			final QOP[] qopOptions=digestChallenge.getQOPOptions();	//get the quality of protection options
			if(qopOptions!=null)	//if quality of protection was specified
			{
				parameterList.add(new NameValuePair<String, String>(QOP_PARAMETER, formatList(new StringBuilder(), (Object[])qopOptions).toString()));	//qop
			}
			final Set<String> unquotedAuthorizationParameters=new HashSet<String>();	//create a set of parameters that should not be quoted, as per RFC 2617
			unquotedAuthorizationParameters.add(ALGORITHM_PARAMETER);	//algorithm
			unquotedAuthorizationParameters.add(STALE_PARAMETER);	//stale
			formatAttributeList(stringBuilder, unquotedAuthorizationParameters, parameterList.toArray(new NameValuePair[parameterList.size()]));	//parameters
		}
		else	//if this is an unsupported challenge TODO implement BASIC challenge
		{
			throw new IllegalArgumentException(scheme.toString());
		}
		return stringBuilder;	//return the string builder we used
	}

	/**Appends the string representations of the given objects separated by a the HTTP list delimiter character.
	@param stringBuilder The string builder into which the result should be placed.
	@param items The objects to be formatted.
	@return The string buffer containing the new information.
	@see FormatUtilities#formatList(StringBuilder, char, Object[])
	@see Object#toString
	*/
	public static StringBuilder formatList(final StringBuilder stringBuilder, final Object... items)
	{
		return FormatUtilities.formatList(stringBuilder, LIST_DELIMITER, items);	//format the items as a list
	}

	/**Appends the string representations of the given objects separated by the HTTP list delimiter character.
	@param stringBuilder The string builder into which the result should be placed.
	@param delimiter The separator character to be inserted between the object strings. 
	@param iterable The objects to be formatted.
	@return The string buffer containing the new information.
	@see Object#toString
	*/
	public static StringBuilder formatList(final StringBuilder stringBuilder, final Iterable<?> iterable)
	{
		return FormatUtilities.formatList(stringBuilder, LIST_DELIMITER, iterable);	//format the list with a string delimiter
	}

}
