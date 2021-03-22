package io.openk9.neo4j.relationship.graph.client;

import io.openk9.relationship.graph.api.client.IsoDuration;
import lombok.RequiredArgsConstructor;

import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.util.List;

@RequiredArgsConstructor
public class IsoDurationWrapper implements IsoDuration {

	@Override
	public long months() {
		return this.delegate.months();
	}

	@Override
	public long days() {
		return this.delegate.days();
	}

	@Override
	public long seconds() {
		return this.delegate.seconds();
	}

	@Override
	public int nanoseconds() {
		return this.delegate.nanoseconds();
	}

	@Override
	public long get(TemporalUnit unit) {
		return this.delegate.get(unit);
	}

	@Override
	public List<TemporalUnit> getUnits() {
		return this.delegate.getUnits();
	}

	@Override
	public Temporal addTo(Temporal temporal) {
		return this.delegate.addTo(temporal);
	}

	@Override
	public Temporal subtractFrom(Temporal temporal) {
		return this.delegate.subtractFrom(temporal);
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	public org.neo4j.driver.types.IsoDuration getDelegate() {
		return delegate;
	}

	final org.neo4j.driver.types.IsoDuration delegate;

}
