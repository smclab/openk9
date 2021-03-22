package io.openk9.relationship.graph.api.client;

public interface Segment {
	Relationship relationship();

	Node start();

	Node end();
}
