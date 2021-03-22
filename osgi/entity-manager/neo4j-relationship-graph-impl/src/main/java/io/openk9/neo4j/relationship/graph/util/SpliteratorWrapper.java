package io.openk9.neo4j.relationship.graph.util;

import lombok.RequiredArgsConstructor;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor(staticName = "of")
public class SpliteratorWrapper<A, B> implements Spliterator<B> {

	@Override
	public boolean tryAdvance(Consumer<? super B> action) {
		return _spliterator.tryAdvance(a -> action.accept(_mapper.apply(a)));
	}

	@Override
	public Spliterator<B> trySplit() {
		return new SpliteratorWrapper<>(_spliterator.trySplit(), _mapper);
	}

	@Override
	public long estimateSize() {
		return _spliterator.estimateSize();
	}

	@Override
	public int characteristics() {
		return _spliterator.characteristics();
	}

	private final Spliterator<A> _spliterator;
	private final Function<A, B> _mapper;

}
