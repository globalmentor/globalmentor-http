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

import java.text.*;
import java.util.Locale;
import java.util.TimeZone;

import static com.globalmentor.time.TimeZones.*;

/**
 * Class for formatting dates and times according to one of the HTTP styles indicated in "3.3.1 Full Date" of <a href="http://www.ietf.org/rfc/rfc2068.txt">RFC
 * 2068</a>, "Hypertext Transfer Protocol -- HTTP/1.1".
 * @author Garret Wilson
 */
public class HTTPDateFormat extends SimpleDateFormat {

	private static final long serialVersionUID = 6637634745774656959L;

	/** The specific style of HTTP date format. */
	public enum Style {
		/** Style for RFC 822, updated by RFC 1123: Sun, 06 Nov 1994 08:49:37 GMT */
		RFC1123,
		/** Style for RFC 850, obsoleted by RFC 1036: Sunday, 06-Nov-94 08:49:37 GMT */
		RFC850,
		/** Style for ANSI C's asctime() format: Sun Nov 6 08:49:37 1994 */
		ASCTIME
	};

	/** Pattern for RFC 822, updated by RFC 1123. */
	private static final String RFC1123_PATTERN = "EEE, dd MMM yyyy HH:mm:ss 'GMT'";
	/** Pattern for year and month: YYYY-MM (eg 1997-07) */
	private static final String RFC850_PATTERN = "EEEE, dd-MMM-yy HH:mm:ss 'GMT'";
	/** Pattern for ANSI C's asctime() format. */
	private static final String ASCTIME_PATTERN = "EEE MMM d HH:mm:ss yyyy";

	/** The style to use for formatting. */
	private final Style style;

	/** @return The style to use for formatting. */
	protected Style getStyle() {
		return style;
	}

	/**
	 * Constructs a W3C date and time formatter using the default RFC 1123 style, the preferred style of RFC 2068.
	 */
	public HTTPDateFormat() {
		this(Style.RFC1123); //construct a date format based upon RFC 1123, as preferred by RFC 2068
	}

	/**
	 * Constructs a W3C date and time formatter using the given style.
	 * @param style One of the styles defined by this class
	 */
	public HTTPDateFormat(final Style style) {
		super(getPattern(style), Locale.ENGLISH); //the HTTP date format always uses English
		this.style = style; //save the style
		setTimeZone(TimeZone.getTimeZone(GMT_ID)); //the HTTP date format always uses GMT
	}

	/** The formatting patterns used, in order. */
	private static final String[] patterns = {RFC1123_PATTERN, RFC850_PATTERN, ASCTIME_PATTERN};

	/**
	 * Determines a pattern to use for the given style. This pattern may be incomplete and the output may require more processing.
	 * @param style One of the styles defined by this class
	 * @return A pattern to use as the basis for formatting.
	 */
	protected static String getPattern(final Style style) {
		final int styleOrdinal = style.ordinal(); //get the zero-based pattern index
		return patterns[styleOrdinal]; //get this pattern
	}
}
