package io.openk9.neo4j.relationship.graph.factory;

import io.openk9.neo4j.relationship.graph.client.IsoDurationWrapper;
import io.openk9.relationship.graph.api.client.IsoDuration;
import io.openk9.relationship.graph.api.factory.IsoDurationFactory;
import org.neo4j.driver.internal.InternalIsoDuration;
import org.osgi.service.component.annotations.Component;

@Component(
	immediate = true,
	service = IsoDurationFactory.class
)
public class IsoDurationFactoryImpl implements IsoDurationFactory {

	@Override
	public IsoDuration createIsoDuration(
		long months, long days, long seconds, int nanoseconds) {
		return new IsoDurationWrapper(
			new InternalIsoDuration(months, days, seconds, nanoseconds));
	}

}
