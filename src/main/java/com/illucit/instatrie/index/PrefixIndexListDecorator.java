package com.illucit.instatrie.index;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Decorator for prefix index to enable the List interface. Delegate
 * implementation is done with interface default methods.
 * 
 * @author Christian Simon
 *
 * @param <T>
 *            list entry type
 */
public interface PrefixIndexListDecorator<T extends Serializable> extends List<T>, PrefixIndex<T> {

	@Override
	public default int size() {
		return getAll().size();
	}

	@Override
	public default boolean isEmpty() {
		return getAll().isEmpty();
	}

	@Override
	public default boolean contains(Object o) {
		return getAll().contains(o);
	}

	@Override
	public default Iterator<T> iterator() {
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
	public default Object[] toArray() {
		return getAll().toArray();
	}

	@Override
	public default <S> S[] toArray(S[] a) {
		return getAll().toArray(a);
	}

	@Override
	public default boolean add(T e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public default boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public default boolean containsAll(Collection<?> c) {
		return getAll().containsAll(c);
	}

	@Override
	public default boolean addAll(Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public default boolean addAll(int index, Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public default boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public default boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public default void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public default T get(int index) {
		return getAll().get(index);
	}

	@Override
	public default T set(int index, T element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public default void add(int index, T element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public default T remove(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public default int indexOf(Object o) {
		return getAll().indexOf(o);
	}

	@Override
	public default int lastIndexOf(Object o) {
		return getAll().lastIndexOf(o);
	}

	@Override
	public default ListIterator<T> listIterator() {
		return listIterator(0);
	}

	@Override
	public default ListIterator<T> listIterator(int index) {
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
	public default List<T> subList(int fromIndex, int toIndex) {
		return getAll().subList(fromIndex, toIndex);
	}
	
	@Override
	public PrefixIndexListDecorator<T> getFilteredView(Predicate<T> filterFunction);

}
