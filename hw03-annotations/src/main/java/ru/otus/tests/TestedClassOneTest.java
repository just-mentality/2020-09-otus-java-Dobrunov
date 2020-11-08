package ru.otus.tests;

import ru.otus.annotations.After;
import ru.otus.annotations.Before;
import ru.otus.annotations.Test;
import ru.otus.exceptions.AfterEachException;
import ru.otus.exceptions.BeforeEachException;
import ru.otus.exceptions.TestException;

public class TestedClassOneTest {

    @Before
    public void beforeEachExampleOne() {
        System.out.println("beforeEachExampleOne executed!");
    }

    @Before
    void beforeEachExampleTwo() {
        System.out.println("beforeEachExampleTwo executed!");
    }

    @Test
    public void testOneFailed() throws TestException {
        throw new TestException("raised testOneFailed!");
    }

    @Test
    void testTwoFailed() throws TestException {
        throw new TestException("raised testTwoFailed!");
    }

    @Test
    public void testThreeSuccess() {
        System.out.println("testThreeSuccess executed!");
    }

    @Test
    void testFourSuccess() {
        System.out.println("testFourSuccess executed!");
    }

    @After
    public void afterEachExampleOne() {
        System.out.println("afterEachExampleOne executed!");
    }

    @After
    void afterEachExampleTwo() {
        System.out.println("afterEachExampleTwo executed!");
    }

}
