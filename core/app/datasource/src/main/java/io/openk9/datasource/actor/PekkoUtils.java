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

package io.openk9.datasource.actor;

import com.typesafe.config.Config;

import java.time.Duration;
import java.util.function.Function;

public class PekkoUtils {

	public static Duration getDuration(
		Config config, String configPath, Duration defaultValue) {

		return getProperty(config, configPath, config::getDuration, defaultValue);
	}

	public static int getInteger(
		Config config, String configPath, int defaultValue) {

		return getProperty(config, configPath, config::getInt, defaultValue);
	}

	public static long getLong(Config config, String configPath, long defaultValue) {
		return getProperty(config, configPath, config::getLong, defaultValue);
	}

	public static String getString(
		Config config, String configPath, String defaultValue) {

		return getProperty(config, configPath, config::getString, defaultValue);
	}

	public static <T> T getProperty(
		Config config, String configPath, Function<String, T> getter, T defaultValue) {

		if (config.hasPathOrNull(configPath)) {
			if (config.getIsNull(configPath)) {
				return defaultValue;
			} else {
				return getter.apply(configPath);
			}
		} else {
			return defaultValue;
		}
	}
}
