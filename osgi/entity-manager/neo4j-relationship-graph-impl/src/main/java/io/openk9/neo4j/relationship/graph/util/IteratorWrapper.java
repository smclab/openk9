package io.openk9.neo4j.relationship.graph.util;

import lombok.RequiredArgsConstructor;

import java.util.Iterator;
import java.util.function.Function;

@RequiredArgsConstructor(staticName = "of")
public class IteratorWrapper<A, B> implements Iterator<B> {

	@Override
	public boolean hasNext() {
		return _iterator.hasNext();
	}

	@Override
	public B next() {
		return _mapper.apply(_iterator.next());
	}

	private final Iterator<A> _iterator;
	private final Function<A, B> _mapper;

}
