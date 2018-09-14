package com.illucit.instatrie.index;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class PrefixSearchListDecorator<T extends Serializable> implements List<T>, PrefixSearch<T> {

	private final PrefixSearch<T> prefixSearch;

	public PrefixSearchListDecorator(PrefixSearch<T> prefixSearch) {
		this.prefixSearch = prefixSearch;
	}

	@Override
	public Stream<T> searchStream(String query) {
		return prefixSearch.searchStream(query);
	}

	@Override
	public Stream<T> searchExactStream(String query) {
		return prefixSearch.searchExactStream(query);
	}

	@Override
	public int size() {
		return getAll().size();
	}

	@Override
	public boolean isEmpty() {
		return getAll().isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return getAll().contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			private final Iterator<? extends T> iteratorDelegate = getAll().iterator();

			@Override
			public boolean hasNext() {
				return iteratorDelegate.hasNext();
			}

			@Override
			public T next() {
				return iteratorDelegate.next();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void forEachRemaining(Consumer<? super T> action) {
				iteratorDelegate.forEachRemaining(action);
			}
		};
	}

	@Override
	public Object[] toArray() {
		return getAll().toArray();
	}

	@Override
	public <S> S[] toArray(S[] a) {
		return getAll().toArray(a);
	}

	@Override
	public boolean add(T e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return getAll().containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof PrefixSearchListDecorator) {
			return getAll().equals(((PrefixSearchListDecorator<?>) o).getAll());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getAll().hashCode();
	}

	@Override
	public T get(int index) {
		return getAll().get(index);
	}

	@Override
	public T set(int index, T element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(int index, T element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T remove(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int indexOf(Object o) {
		return getAll().indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return getAll().lastIndexOf(o);
	}

	@Override
	public ListIterator<T> listIterator() {
		return listIterator(0);
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		return new ListIterator<T>() {
			private final ListIterator<T> iteratorDelegate = getAll().listIterator(index);

			@Override
			public boolean hasNext() {
				return iteratorDelegate.hasNext();
			}

			@Override
			public T next() {
				return iteratorDelegate.next();
			}

			@Override
			public boolean hasPrevious() {
				return iteratorDelegate.hasPrevious();
			}

			@Override
			public T previous() {
				return iteratorDelegate.previous();
			}

			@Override
			public int nextIndex() {
				return iteratorDelegate.nextIndex();
			}

			@Override
			public int previousIndex() {
				return iteratorDelegate.previousIndex();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void set(T e) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void add(T e) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void forEachRemaining(Consumer<? super T> action) {
				iteratorDelegate.forEachRemaining(action);
			}
		};
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return getAll().subList(fromIndex, toIndex);
	}

	@Override
	public <U extends Serializable> PrefixSearchListDecorator<U> map(Function<T, U> mapFunction) {
		PrefixSearch<U> mapped = prefixSearch.map(mapFunction);
		if (mapped instanceof PrefixSearchListDecorator) {
			return (PrefixSearchListDecorator<U>) mapped;
		}
		return new PrefixSearchListDecorator<>(mapped);
	}

	@Override
	public PrefixSearchListDecorator<T> filter(Predicate<T> predicate) {
		PrefixSearch<T> filtered = prefixSearch.filter(predicate);
		if (filtered instanceof PrefixSearchListDecorator) {
			return (PrefixSearchListDecorator<T>) filtered;
		}
		return new PrefixSearchListDecorator<>(filtered);
	}
}
