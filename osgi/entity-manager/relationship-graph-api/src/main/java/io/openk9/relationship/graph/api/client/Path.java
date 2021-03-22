package io.openk9.relationship.graph.api.client;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

public interface Path extends Iterable<Segment> {
	Node start();

	Node end();

	int length();

	boolean contains(Node node);

	boolean contains(Relationship relationship);

	Iterable<Node> nodes();

	Iterable<Relationship> relationships();

	Iterator<Segment> iterator();

	void forEach(Consumer<? super Segment> action);

	Spliterator<Segment> spliterator();
}
