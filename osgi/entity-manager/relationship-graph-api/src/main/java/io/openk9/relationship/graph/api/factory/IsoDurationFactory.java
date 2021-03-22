package io.openk9.relationship.graph.api.factory;

import io.openk9.relationship.graph.api.client.IsoDuration;

public interface IsoDurationFactory {

	IsoDuration createIsoDuration(
		long months, long days, long seconds, int nanoseconds);

}
