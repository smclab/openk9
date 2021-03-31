package io.openk9.relationship.graph.api.client;

import org.neo4j.cypherdsl.core.Statement;
import org.reactivestreams.Publisher;

public interface GraphClient {

	Publisher<Record> write(Statement statement);

	Publisher<Record> read(Statement statement);

}
