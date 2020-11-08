package ru.otus.collection;

import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class CustomArrayListImplementation<T> implements List<T> {


    private static final Object[] EMPTY_ARRAY = {};
    private static final int DEFAULT_SIZE = 16;
    private static final int INCREASE_STEP = 64;
    private static final int NOT_FOUND_IDX = -1;


    private Object[] elements;
    private int size;

    public CustomArrayListImplementation() {
        this.elements = EMPTY_ARRAY;
        this.size = 0;
    }

    public CustomArrayListImplementation(final int size) {
        this.elements = new Object[size];
        this.size = size;
    }

    private Object[] growArray(final int atLeastNewSize) {
        final int previousSize = elements.length;
        if (previousSize == 0 || elements == EMPTY_ARRAY) {
            elements = new Object[Math.max(atLeastNewSize, DEFAULT_SIZE)];
            size = 0;
        } else {
            elements = Arrays.copyOf(elements, atLeastNewSize + INCREASE_STEP);
        }
        return elements;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new CustomSimpleIterator();
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOfRange(elements, 0, size);
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(T t) {
        if (size == elements.length) {
            elements = growArray(size + 1);
        }
        elements[size] = t;
        size += 1;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        final int indexOfElement = indexOf(o);
        final int newSize = size - 1;
        if (indexOfElement < size - 1) {
            System.arraycopy(elements, indexOfElement + 1, elements, indexOfElement, newSize - indexOfElement);
        }
        elements[size = newSize] = null;
        return true;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        if (c.isEmpty()) {
            // nothing to do
            return false;
        }
        final int additionalSize = c.size();

        if (additionalSize > elements.length - size) {
            elements = growArray(size + additionalSize);
        }

        System.arraycopy(c.toArray(), 0, elements, size, additionalSize);
        size += additionalSize;
        return true;
    }

    private void checkIndex(final int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        checkIndex(index);

        if (c.isEmpty()) {
            // nothing to do
            return false;
        }
        final int additionalSize = c.size();

        if (additionalSize > elements.length - size) {
            elements = growArray(size + additionalSize);
        }

        final int movedNumberOfElements = size - index;
        if (movedNumberOfElements > 0) {
            System.arraycopy(elements, index, elements, index + additionalSize, movedNumberOfElements);
        }
        System.arraycopy(c.toArray(), 0, elements, index, additionalSize);
        size += additionalSize;

        return true;
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
        for (int i = 0; i < size; i++)
            elements[i] = null;
    }

    @Override
    public T get(int index) {
        Objects.checkIndex(index, size);
        return (T) elements[index];
    }

    @Override
    public T set(int index, T element) {
        Objects.checkIndex(index, size);
        T oldValue = (T) elements[index];
        elements[index] = element;
        return oldValue;
    }

    @Override
    public void add(int index, T element) {
        checkIndex(index);

        // has no place to insert
        if (size == elements.length)
            elements = growArray(size);

        System.arraycopy(elements, index, elements, index + 1, size - index);
        elements[index] = element;
        size += 1;
    }

    @Override
    public T remove(int index) {
        checkIndex(index);
        final T oldValue = (T) elements[index];

        final int newSize = size - 1;
        if (index < size - 1) {
            System.arraycopy(elements, index + 1, elements, index, newSize - index);
        }
        elements[size = newSize] = null;
        return oldValue;
    }

    private OptionalInt indexOfInternal(final int startPosition, final int endPosition,
                                        final Predicate<Object> function) {
        return IntStream.range(startPosition, endPosition).filter(i -> function.test(elements[i])).findFirst();
    }

    @Override
    public int indexOf(Object o) {
        if (o == null) {
            // if object is null, then equals will throw NPE
            return indexOfInternal(0, size, Objects::isNull).orElse(NOT_FOUND_IDX);
        } else {
            return indexOfInternal(0, size, o::equals).orElse(NOT_FOUND_IDX);
        }
    }

    @Override
    public int lastIndexOf(Object o) {
        if (o == null) {
            // if object is null, then equals will throw NPE
            return indexOfInternal(size - 1, -1, Objects::isNull).orElse(NOT_FOUND_IDX);
        } else {
            return indexOfInternal(size - 1, -1, o::equals).orElse(NOT_FOUND_IDX);
        }
    }

    @Override
    public ListIterator<T> listIterator() {
        return new CustomListIterator(0);
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return new CustomListIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return null;
    }

    private class CustomListIterator extends CustomSimpleIterator implements ListIterator<T> {

        CustomListIterator(int index) {
            super();
            currentPosition = index;
        }

        @Override
        public boolean hasPrevious() {
            return currentPosition != 0;
        }

        @Override
        public T previous() {
            int i = currentPosition - 1;
            if (i < 0) {
                throw new NoSuchElementException();
            }
            if (i >= elements.length)
                throw new ConcurrentModificationException();

            currentPosition = i;
            lastReturnedIdx = i;
            return (T) elements[i];
        }

        @Override
        public int nextIndex() {
            return currentPosition;
        }

        @Override
        public int previousIndex() {
            return currentPosition - 1;
        }

        @Override
        public void set(T t) {
            CustomArrayListImplementation.this.set(lastReturnedIdx, t);
        }

        @Override
        public void add(T t) {
            try {
                int i = currentPosition;
                CustomArrayListImplementation.this.add(i, t);
                currentPosition = i + 1;
                lastReturnedIdx = -1;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
    }

    private class CustomSimpleIterator implements Iterator<T> {

        int currentPosition;
        int lastReturnedIdx = -1;

        CustomSimpleIterator() {
        }

        @Override
        public boolean hasNext() {
            return currentPosition != size;
        }

        @Override
        public T next() {
            final T result = (T) elements[currentPosition];
            lastReturnedIdx = currentPosition;
            currentPosition += 1;
            return result;
        }

        @Override
        public void remove() {
            try {
                CustomArrayListImplementation.this.remove(lastReturnedIdx);
                currentPosition = lastReturnedIdx;
                lastReturnedIdx = -1;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public void forEachRemaining(Consumer<? super T> action) {
            Objects.requireNonNull(action);

            int i = currentPosition;
            if (i < size) {
                if (i >= elements.length)
                    throw new ConcurrentModificationException();

                for (; i < size; i++)
                    action.accept((T) elements[i]);

                currentPosition = i;
                lastReturnedIdx = i - 1;
            }
        }
    }
}
