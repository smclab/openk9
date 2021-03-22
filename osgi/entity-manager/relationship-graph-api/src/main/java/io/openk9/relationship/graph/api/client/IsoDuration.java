package io.openk9.relationship.graph.api.client;

import java.time.temporal.TemporalAmount;

public interface IsoDuration extends TemporalAmount {

	long months();

	long days();

	long seconds();

	int nanoseconds();

}
