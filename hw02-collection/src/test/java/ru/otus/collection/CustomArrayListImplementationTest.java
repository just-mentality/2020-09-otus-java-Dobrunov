package ru.otus.collection;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomArrayListImplementationTest {

    @Test
    void testAddingSeveralElementsToImplementedCollection() {
        CustomArrayListImplementation<Integer> collection = new CustomArrayListImplementation<>();
        final List<Integer> expected = IntStream.range(1, 500)
                .peek(collection::add)
                .boxed().collect(Collectors.toList());

        assertEquals(499, collection.size());
        assertThat(collection).containsExactlyElementsOf(expected);

    }

    @Test
    void checkThatCollectionsAddAllIsWorkingOnImplementedCollection() {
        CustomArrayListImplementation<Integer> collection = new CustomArrayListImplementation<>();
        final Integer[] expected = IntStream.range(1, 500)
                .boxed()
                .toArray(Integer[]::new);

        Collections.addAll(collection, expected);
        assertThat(collection).containsExactly(expected);
    }

    @Test
    void checkThatCollectionsCopyIsWorkingOnImplementedCollection() {
        CustomArrayListImplementation<Integer> collection = new CustomArrayListImplementation<>();
        final List<Integer> expected = IntStream.range(1, 500)
                .peek(collection::add)
                .boxed().collect(Collectors.toList());

        Collections.copy(collection, expected);
        assertThat(collection).containsExactlyElementsOf(expected);
    }

    @Test
    void checkThatCollectionsSortIsWorkingOnImplementedCollection() {
        CustomArrayListImplementation<Integer> collection = new CustomArrayListImplementation<>();
        List<Integer> expectedData = IntStream.range(1, 500)
                .peek(i -> collection.add(500 - i))
                .boxed()
                .collect(Collectors.toList());

        Collections.sort(collection, Comparator.naturalOrder());
        assertThat(collection).containsExactlyElementsOf(expectedData);
    }

    @Test
    void testAddingSeveralElementsAndRemove() {
        CustomArrayListImplementation<Integer> collection = new CustomArrayListImplementation<>();
        final List<Integer> expected = IntStream.range(1, 6)
                .peek(collection::add)
                .boxed().collect(Collectors.toList());

        Collections.copy(collection, expected);
        collection.remove(3);
        assertThat(collection).containsExactlyElementsOf(List.of(1, 2, 3, 5));
        assertEquals(4, collection.size());

    }
}
