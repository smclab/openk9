package io.openk9.neo4j.relationship.graph.client;

import io.openk9.relationship.graph.api.client.Point;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PointWrapper implements Point {

	@Override
	public int srid() {
		return this.delegate.srid();
	}

	@Override
	public double x() {
		return this.delegate.x();
	}

	@Override
	public double y() {
		return this.delegate.y();
	}

	@Override
	public double z() {
		return this.delegate.z();
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	public org.neo4j.driver.types.Point getDelegate() {
		return delegate;
	}

	final org.neo4j.driver.types.Point delegate;

}
