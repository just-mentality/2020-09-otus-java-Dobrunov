package ru.otus.tests;

import ru.otus.annotations.After;
import ru.otus.annotations.Before;
import ru.otus.annotations.Test;
import ru.otus.exceptions.AfterEachException;
import ru.otus.exceptions.BeforeEachException;

public class TestedClassTwoTest {

    @Before
    void beforeEachExampleThree() {
        throw new BeforeEachException("raised beforeEachExampleThree!");
    }

    @After
    void afterEachExampleThree() {
        throw new AfterEachException("raised afterEachExampleThree!");
    }

    @Test
    void someTest() {
        System.out.println("someTest never called!");
    }

}
