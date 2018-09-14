package com.illucit.instatrie.index;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface PrefixSearch<T extends Serializable> {

	/**
	 * Search the index for a query string and return a stream of indexed
	 * models, which match the query prefix criteria. The query string is
	 * splitted with the given search word splitter (the default search word
	 * splitter is extracting the query words by finding connected alphanumeric
	 * sequences). All query words must be found (completely or as prefixes) in
	 * the index for an entry to be considered found. The stream still requires
	 * a terminal operation in order to be processed.
	 *
	 * @param query query string
	 * @return stream of models, in the same order or the iterator of the indexed
	 * collection
	 */
	Stream<T> searchStream(String query);

	/**
	 * Search the index for a query string and return a list of indexed models,
	 * which match the query criteria. The query string is splitted with the
	 * given search word splitter (the default search word splitter is
	 * extracting the query words by finding connected alphanumeric sequences).
	 * All query words must be found completely in the index for an entry to be
	 * considered found.
	 *
	 * @param query query string
	 * @return stream of models, in the same order or the iterator of the indexed
	 * collection
	 */
	Stream<T> searchExactStream(String query);

	/**
	 * Search the index for a query string and return a list of indexed models,
	 * which match the query prefix criteria. The query string is splitted with
	 * the given search word splitter (the default search word splitter is
	 * extracting the query words by finding connected alphanumeric sequences).
	 * All query words must be found (completely or as prefixes) in the index
	 * for an entry to be considered found.
	 *
	 * @param query query string
	 * @return list of models, in the same order or the iterator of the indexed
	 * collection
	 */
	default List<T> search(String query) {
		return unmodifiableList(searchStream(query).collect(toList()));
	}

	/**
	 * Search the index for a query string and return a list of indexed models,
	 * which match the query prefix criteria. The query string is splitted with
	 * the given search word splitter (the default search word splitter is
	 * extracting the query words by finding connected alphanumeric sequences).
	 * All query words must be found (completely or as prefixes) in the index
	 * for an entry to be considered found. If there are more entries found than
	 * the parameter limit, all remaining are omitted from the result list.
	 *
	 * @param query   query string
	 * @param maxSize as soon as there are so many entries found, all others are
	 *                omitted
	 * @return list of models, in the same order or the iterator of the indexed
	 * collection
	 */
	default List<T> search(String query, long maxSize) {
		return unmodifiableList(searchStream(query).limit(maxSize).collect(toList()));
	}

	/**
	 * Search the index for a query string and return a list of indexed models,
	 * which match the query criteria. The query string is splitted with the
	 * given search word splitter (the default search word splitter is
	 * extracting the query words by finding connected alphanumeric sequences).
	 * All query words must be found completely in the index for an entry to be
	 * considered found.
	 *
	 * @param query query string
	 * @return list of models, in the same order or the iterator of the indexed
	 * collection
	 */
	default List<T> searchExact(String query) {
		return unmodifiableList(searchExactStream(query).collect(toList()));
	}

	/**
	 * Get all model entries in the index as list (in the same order they were
	 * in the collection iterator on index building).
	 *
	 * @return list of models
	 */
	default List<T> getAll() {
		return search(null);
	}

	/**
	 * Retrieve a new, dependent Prefix search, which will apply the given predicate as filter for all result streams/lists.
	 *
	 * @param predicate predicate to filter for
	 * @return new prefix search
	 */
	default PrefixSearch<T> filter(Predicate<T> predicate) {
		return new PrefixSearchFiltered<>(this, predicate);
	}

	/**
	 * Retrieve a new, dependent Prefix search, which will apply the given mapping function to all result streams/lists.
	 * @param mapFunction mapping frunction from the current type parameter to a new type
	 * @param <U> type parameter of new Prefix search
	 * @return new prefix search
	 */
	default <U extends Serializable> PrefixSearch<U> map(Function<T, U> mapFunction) {
		return new PrefixSearchMapped<>(this, mapFunction);
	}

	/**
	 * Get an instance of {@link PrefixIndex} which also implements the
	 * {@link List} interface. The returned instance is completely entangled
	 * with the current object, so that both objects share a state and each trie
	 * operation on one of the objects also affects the other object in the same
	 * way.
	 *
	 * @return decorated prefix index
	 */
	default PrefixSearchListDecorator<T> decorateAsList() {
		if (this instanceof PrefixSearchListDecorator) {
			return (PrefixSearchListDecorator<T>) this;
		}
		return new PrefixSearchListDecorator<>(this);
	}

}
