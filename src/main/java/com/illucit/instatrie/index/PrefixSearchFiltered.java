package com.illucit.instatrie.index;

import java.io.Serializable;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class PrefixSearchFiltered<T extends Serializable> implements PrefixSearch<T> {

	private final PrefixSearch<T> delegate;
	private final Predicate<T> predicate;

	public PrefixSearchFiltered(PrefixSearch<T> delegate, Predicate<T> predicate) {
		this.delegate = delegate;
		this.predicate = predicate;
	}

	@Override
	public Stream<T> searchStream(String query) {
		return delegate.searchStream(query).filter(predicate);
	}

	@Override
	public Stream<T> searchExactStream(String query) {
		return delegate.searchExactStream(query).filter(predicate);
	}
}
