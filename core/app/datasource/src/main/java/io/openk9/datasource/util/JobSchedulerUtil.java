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

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class JobSchedulerUtil {

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
