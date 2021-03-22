package io.openk9.neo4j.relationship.graph.client;

import io.openk9.neo4j.relationship.graph.util.IteratorWrapper;
import io.openk9.neo4j.relationship.graph.util.SpliteratorWrapper;
import io.openk9.relationship.graph.api.client.Node;
import io.openk9.relationship.graph.api.client.Path;
import io.openk9.relationship.graph.api.client.Relationship;
import io.openk9.relationship.graph.api.client.Segment;
import lombok.RequiredArgsConstructor;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class PathWrapper implements Path {

	@Override
	public Node start() {
		return new NodeWrapper(this.delegate.start());
	}

	@Override
	public Node end() {
		return new NodeWrapper(this.delegate.end());
	}

	@Override
	public int length() {
		return this.delegate.length();
	}

	@Override
	public boolean contains(Node node) {
		return this.delegate.contains(((NodeWrapper)node).getDelegate());
	}

	@Override
	public boolean contains(Relationship relationship) {
		return this.delegate.contains(
			((RelationshipWrapper)relationship).getDelegate());
	}

	@Override
	public Iterable<Node> nodes() {
		return () -> IteratorWrapper.of(
			this.delegate.nodes().iterator(),
			NodeWrapper::new);
	}

	@Override
	public Iterable<Relationship> relationships() {
		return () ->
			IteratorWrapper.of(
				this.delegate.relationships().iterator(), RelationshipWrapper::new);

	}

	@Override
	public Iterator<Segment> iterator() {
		return IteratorWrapper.of(this.delegate.iterator(), SegmentWrapper::of);
	}

	@Override
	public void forEach(
		Consumer<? super Segment> action) {
		this.delegate.forEach(segment -> action.accept(SegmentWrapper.of(segment)));
	}

	@Override
	public Spliterator<Segment> spliterator() {
		return SpliteratorWrapper.of(delegate.spliterator(), SegmentWrapper::of);
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	public org.neo4j.driver.types.Path getDelegate() {
		return delegate;
	}

	final org.neo4j.driver.types.Path delegate;

}
