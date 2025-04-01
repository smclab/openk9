/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.datasource.util;

import com.typesafe.config.impl.ConfigImplUtil;
import io.quarkus.runtime.util.ExceptionUtil;

import java.io.LineNumberReader;
import java.io.StringReader;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SchedulerUtil {

	/**
	 * Extracts a concise error description from a throwable by removing unnecessary stack trace details.
	 * The method retrieves the full stack trace, prioritizes the root cause, and filters out less relevant lines,
	 * such as stack trace elements (`at ...`) and collapsed stack trace sections (`... n more`).
	 * The resulting description is truncated to a maximum of 4000 characters.
	 *
	 * @param throwable the throwable from which to extract the error description
	 * @return a cleaned-up error message, prioritizing the root cause and removing redundant details,
	 *          truncated to a maximum of 4000 characters
	 */
	public static String getErrorDescription(Throwable throwable) {
		var invertedStackTrace =
			ExceptionUtil.rootCauseFirstStackTrace(throwable);

		var stringReader = new StringReader(invertedStackTrace);
		var lineNumberReader = new LineNumberReader(stringReader);

		var collapsed = lineNumberReader.lines()
			.filter(line -> !line.startsWith("\tat"))
			.filter(line -> !line.startsWith("\t..."))
			.collect(Collectors.joining("\n"));

		return collapsed.substring(0, Math.min(collapsed.length(), 4000));
	}

	/**
	 * Parses a duration string and converts it into a {@link java.time.Duration} object.
	 * The input string should contain a numeric value followed by a time unit, such as "5s" (5 seconds) or "2.5h" (2.5 hours).
	 *
	 * <p>Supported time units (case-sensitive):
	 * <ul>
	 *   <li>"ns", "nanos", "nanoseconds" - Nanoseconds</li>
	 *   <li>"us", "micros", "microseconds" - Microseconds</li>
	 *   <li>"ms", "millis", "milliseconds" - Milliseconds</li>
	 *   <li>"s", "seconds" - Seconds</li>
	 *   <li>"m", "minutes" - Minutes</li>
	 *   <li>"h", "hours" - Hours</li>
	 *   <li>"d", "days" - Days</li>
	 * </ul>
	 *
	 * <p>If the input string is empty, contains an invalid unit, or cannot be parsed as a number,
	 * the method returns a default {@link java.time.Duration} of 2 days.
	 *
	 * @param durationString the string representing the duration, containing a numeric value and a time unit
	 * @return the parsed {@link java.time.Duration} object, or a default of 2 days if the input is invalid
	 */
	public static Duration parseDuration(String durationString) {
		String s = ConfigImplUtil.unicodeTrim(durationString);
		String unitString = getUnits(s);
		String numberString = ConfigImplUtil.unicodeTrim(
			s.substring(0, s.length() - unitString.length()));
		TimeUnit units = null;
		var twoDaysDuration = Duration.ofDays(2);

		// this would be caught later anyway, but the error message
		// is more helpful if we check it here.
		if (numberString.isEmpty()){
			return twoDaysDuration;
		}

		if (unitString.length() > 2 && !unitString.endsWith("s")) {
			unitString = unitString + "s";
		}

		// note that this is deliberately case-sensitive
		if (unitString.isEmpty() || unitString.equals("ms") || unitString.equals("millis")
			|| unitString.equals("milliseconds")) {
			units = TimeUnit.MILLISECONDS;
		}
		else if (unitString.equals("us") || unitString.equals("micros") || unitString.equals("microseconds")) {
			units = TimeUnit.MICROSECONDS;
		}
		else if (unitString.equals("ns") || unitString.equals("nanos") || unitString.equals("nanoseconds")) {
			units = TimeUnit.NANOSECONDS;
		}
		else if (unitString.equals("d") || unitString.equals("days")) {
			units = TimeUnit.DAYS;
		}
		else if (unitString.equals("h") || unitString.equals("hours")) {
			units = TimeUnit.HOURS;
		}
		else if (unitString.equals("s") || unitString.equals("seconds")) {
			units = TimeUnit.SECONDS;
		}
		else if (unitString.equals("m") || unitString.equals("minutes")) {
			units = TimeUnit.MINUTES;
		}
		else {
			return twoDaysDuration;
		}

		try {
			// if the string is purely digits, parse as an integer to avoid
			// possible precision loss;
			// otherwise as a double.
			if (numberString.matches("[+-]?[0-9]+")) {
				return Duration.ofNanos(units.toNanos(Long.parseLong(numberString)));
			}
			else {
				long nanosInUnit = units.toNanos(1);
				numberString = numberString.replace(",", ".");
				return Duration.ofNanos((long) (Double.parseDouble(numberString) * nanosInUnit));
			}
		}
		catch (NumberFormatException e) {
			return twoDaysDuration;
		}
	}

	private static String getUnits(String s) {
		int i = s.length() - 1;
		while (i >= 0) {
			char c = s.charAt(i);
			if (!Character.isLetter(c)) {
				break;
			}
			i -= 1;
		}
		return s.substring(i + 1);
	}
}
