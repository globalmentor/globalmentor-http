package com.garretwilson.net.http;

import static com.garretwilson.net.http.DigestAuthenticationConstants.*;

/**Represents quality of protection.
@author Garret Wilson
*/
public enum QOP
{

	/**Authentication quality of protection.*/	
	AUTH(AUTH_QOP),

	/**Authentication with integerity protection quality of protection.*/	
	AUTH_INT(AUTH_INT_QOP);

	/**The string value of the enum.*/
	private final String string;
	
	/**String constructor.
	@param string The string value of the enum.
	*/
	private QOP(final String string) {this.string=string;}
	
	/**@return The string value of the enum.*/
	public String toString() {return string;}

	/**Determines the enumeration value matching the given string.
	@param string The string value.
	@return The enumeration value corresponding to the string.
	@exception IllegalArgumentException if the enum has no constant with the specified name.
	@exception NullPointerException if the string is <code>null</code>.
	@see #toString()
	*/
	public static QOP valueOfString(final String string)	//TODO find out how to do this with valueOf()
	{
		for(final QOP value:QOP.values())	//look at each value
		{
			if(string.equals(value.toString()))	//if this value has the right string
			{
				return value;	//return this value
			}
		}
		throw new IllegalArgumentException(string);	//show that we didn't recognize the string
	}
}
