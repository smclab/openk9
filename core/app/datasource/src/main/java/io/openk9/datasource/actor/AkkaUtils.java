package io.openk9.datasource.actor;

import com.typesafe.config.Config;

import java.time.Duration;
import java.util.function.Function;

public class AkkaUtils {

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
