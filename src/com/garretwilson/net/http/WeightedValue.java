package com.garretwilson.net.http;

import static com.garretwilson.lang.FloatUtilities.*;

import com.garretwilson.lang.ObjectUtilities;

/**A value with an associated "qvalue" specifying its weight.
@param <V> The type of weighted value represented.
@author Garret Wilson
*/
public class WeightedValue<V> implements Comparable<WeightedValue<V>>
{

	/**The weighted value.*/
	private final V value;

		/**@return The weighted value.*/
		public V getValue() {return value;}

	/**The weight of the value.*/
	private final float qvalue;
		
		/**@return The weight of the value.*/
		public float getQValue() {return qvalue;}

	/**Value and weight constructor.
	@param value The weighted value.
	@param qvalue The weight of the value.
	@exception IllegalArgumentException if the qvalue is not within the range (0.0, 1.0), inclusive.
	*/
	public WeightedValue(final V value, final float qvalue)
	{
		this.value=value;	//save the value
		this.qvalue=checkRange(qvalue, 0.0f, 1.0f);	//save the qvalue, checking its range
	}

	/**@return A hash code for the object.*/
	public int hashCode()
	{
		return ObjectUtilities.hashCode(getValue(), getQValue());
	}

	/**@return A string representation of the object in the form <code><var>value</var>;q=<var>qvalue</var></code>.*/
	public String toString()
	{
		return ObjectUtilities.toString(getValue())+";q="+getQValue();	//return a string representation, using "null" if the value is null
	}

	/**Compares this object with the specified object for order.
	Returns a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
	@param object The object to be compared.
	@return A negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
	*/
	public int compareTo(final WeightedValue<V> object)	//TODO improve to sort by value if weighted values are equal, which will often be the case
	{
		final float qvalue1=getQValue();
		final float qvalue2=object.getQValue();
		return qvalue1<qvalue2 ? -1 : qvalue1>qvalue2 ? 1 : 0;	//compare the values (subtracting and then casting would incorrectly return zero in many instances)
	}

}
