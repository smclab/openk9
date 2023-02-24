package io.openk9.datasource.pipeline.util;

import akka.actor.typed.javadsl.ActorContext;
import com.typesafe.config.Config;

import java.time.Duration;
import java.util.function.Supplier;

public class Util {

	private Util() {}

	public static Duration getDurationFromActorContext(
		ActorContext<?> actorContext, String path, Supplier<Duration> defaultValue) {
		Config config = actorContext.getSystem().settings().config();

		if (config.hasPathOrNull(path)) {
			return config.getDuration(path);
		}

		return defaultValue.get();

	}

}
