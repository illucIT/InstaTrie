package com.illucit.instatrie.index;

import java.io.Serializable;
import java.util.function.Function;
import java.util.stream.Stream;

public class PrefixSearchMapped<T extends Serializable, U extends Serializable> implements PrefixSearch<U> {

	private final PrefixSearch<T> delegate;
	private final Function<T, U> mappingFunction;

	public PrefixSearchMapped(PrefixSearch<T> delegate, Function<T, U> mappingFunction) {
		this.delegate = delegate;
		this.mappingFunction = mappingFunction;
	}

	@Override
	public Stream<U> searchStream(String query) {
		return delegate.searchStream(query).map(mappingFunction);
	}

	@Override
	public Stream<U> searchExactStream(String query) {
		return delegate.searchExactStream(query).map(mappingFunction);
	}

}
