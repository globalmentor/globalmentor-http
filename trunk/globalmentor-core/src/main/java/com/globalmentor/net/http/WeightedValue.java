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

import static com.globalmentor.java.Doubles.*;

import com.globalmentor.java.Objects;

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
	private final double qvalue;
		
		/**@return The weight of the value.*/
		public double getQValue() {return qvalue;}

	/**Value and weight constructor.
	@param value The weighted value.
	@param qvalue The weight of the value.
	@exception IllegalArgumentException if the qvalue is not within the range (0.0, 1.0), inclusive.
	*/
	public WeightedValue(final V value, final double qvalue)
	{
		this.value=value;	//save the value
		this.qvalue=checkRange(qvalue, 0.0f, 1.0f);	//save the qvalue, checking its range
	}

	/**@return A hash code for the object.*/
	public int hashCode()
	{
		return Objects.getHashCode(getValue(), getQValue());
	}

	/**@return A string representation of the object in the form <code><var>value</var>;q=<var>qvalue</var></code>.*/
	public String toString()
	{
		return Objects.toString(getValue())+";q="+getQValue();	//return a string representation, using "null" if the value is null
	}

	/**Compares this object with the specified object for order.
	Returns a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
	@param object The object to be compared.
	@return A negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
	*/
	public int compareTo(final WeightedValue<V> object)	//TODO maybe improve to sort by value if weighted values are equal, which will often be the case
	{
		return Double.compare(getQValue(), object.getQValue());	//compare the values
	}

}
