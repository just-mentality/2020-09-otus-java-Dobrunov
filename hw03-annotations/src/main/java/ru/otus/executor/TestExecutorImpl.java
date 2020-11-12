package ru.otus.executor;

import ru.otus.domain.TestClassInstance;
import ru.otus.domain.TestExecutionInfo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class TestExecutorImpl extends TestExecutor<Collection<String>> {

    protected Collection<String> errors = new LinkedList<>();

    protected static final String DELIMETER = "==============================================================";
    protected static final String INNER_DELIMETER = "------------------------------------------------------------";
    protected static final String TEST_INFO_DELIMETER = "**********************************************************************************";

    @Override
    public void execute(final Collection<String> classes) {
        List<TestClassInstance> testClassInstances = new ArrayList<>();

        for (String className : classes) {
            if (!className.endsWith("Test")) {
                saveError(String.format("Test class name must ends with postfix <%s>, but <%s> doesn't!", "Test", className));
                continue;
            }
            var testInstance = groupClassMethods(className);
            testInstance.ifPresent(e -> {
                if (e.hasTestMethods()) {
                    testClassInstances.add(e);
                }
            });
        }

        if (testClassInstances.isEmpty()) {
            System.out.println("No tests found! Something went wrong...");
        } else {
            showTestsSummary(testClassInstances);
            runTests(testClassInstances);
            printTestResults(testClassInstances);
        }
        printTestsErrors();
    }

    /**
     * Method which analyzes annotations on testClassName methods and differenciate them into lists.
     *
     * @param testClassName
     * @return
     */
    private Optional<TestClassInstance> groupClassMethods(final String testClassName) {
        final Optional<Class<?>> clazz = loadClass(testClassName);

        if (!clazz.isPresent()) {
            saveError(String.format("Class %s is not found ! Something went wrong...", testClassName));
            return Optional.empty();
        }

        final Method[] classMethods = clazz.get().getDeclaredMethods();
        TestClassInstance testClassInstance = new TestClassInstance(clazz.get());
        Arrays.stream(classMethods).forEach(testClassInstance::saveMethodIfSatisfy);
        return Optional.of(testClassInstance);
    }

    private void printTestsErrors() {
        if (errors.isEmpty()) {
            System.out.println("No errors! Congrats!");
            return;
        }
        System.out.println(DELIMETER);
        System.out.println("TESTS ERRORS -->");
        errors.forEach(System.out::println);
        System.out.println(DELIMETER);
    }

    private String getErrorFromException(final Exception exception) {
        final var throwable = exception.getCause();
        if (throwable != null) {
            return throwable.getMessage();
        }
        return exception.getMessage();
    }

    private <T> void saveError(final T error) {
        if (error instanceof String) {
            errors.add((String) error);
        } else {
            errors.add(getErrorFromException((Exception) error));
        }
    }

    private <T> void saveAndPrintError(final T error) {
        if (error == null) {
            return;
        } else if (error instanceof String) {
            errors.add((String) error);
            System.out.println(error);
        } else {
            saveAndPrintError(getErrorFromException((Exception) error));
        }
    }

    private <T> void increaseErrors(final T error, final boolean shouldIncreaseErrorCounter) {
        saveAndPrintError(error);

        if (shouldIncreaseErrorCounter) {
            failedTestsCount++;
        }
    }

    private Object initiateObject(final TestClassInstance testClassInstance) throws Exception {
        final Object testObjectInstance;
        try {
            testObjectInstance = testClassInstance.getClazz().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new Exception(e);
        }
        return testObjectInstance;
    }

    private void runTests(final Collection<TestClassInstance> testClassInstances) {
        int testsCount = 0;
        boolean shouldIncreaseErrorCounter;
        System.out.println("Testing is started.");

        for (final var testClassInstance : testClassInstances) {
            try {
                final var testMethods = testClassInstance.getTestMethods();
                testsCount = testMethods.size();
                totalTestsCount += testsCount;
                final var initiatedTestObject = initiateObject(testClassInstance);

                for (var method : testMethods) {
                    shouldIncreaseErrorCounter = true;
                    System.out.println(TEST_INFO_DELIMETER);
                    System.out.println(" --> " + initiatedTestObject);

                    final var beforeResult = executeMethodsOnObject(initiatedTestObject, testClassInstance.getBeforeMethods(), shouldIncreaseErrorCounter);
                    shouldIncreaseErrorCounter = beforeResult.isSuccessfullTest();
                    method.setTestResult(beforeResult);

                    if (beforeResult.isSuccessfullTest()) {
                        final var testResult = executeMethodsOnObject(initiatedTestObject, List.of(method.getMethod()), shouldIncreaseErrorCounter);
                        if (testResult.isSuccessfullTest()) {
                            successfullTestsCount += 1;
                        } else {
                            shouldIncreaseErrorCounter = false;
                        }
                        method.setTestResult(testResult);
                    }
                    executeMethodsOnObject(initiatedTestObject, testClassInstance.getAfterMethods(), shouldIncreaseErrorCounter);
                }
            } catch (Exception e) {
                failedTestsCount += testsCount;
                saveAndPrintError(e);
            }
        }
        System.out.println(TEST_INFO_DELIMETER);
        System.out.println("Testing has been finished!");
    }

    private void printBeautyInnerTestInfo(final String info) {
        System.out.println(INNER_DELIMETER);
        System.out.println(info);
        System.out.println(INNER_DELIMETER);
    }

    private TestExecutionInfo executeMethodsOnObject(final Object testObj,
                                                     final List<Method> methods,
                                                     final boolean shouldIncreaseErrorCounter) {
        for (Method method : methods) {
            System.out.print(String.format("Running method [%s] -> ", method.getName()));
            method.setAccessible(true); // like junit5 (acess to not public methods)
            try {
                method.invoke(testObj);
            } catch (Exception e) {
                final String errorMessage = getErrorFromException(e);
                increaseErrors(errorMessage, shouldIncreaseErrorCounter);
                return TestExecutionInfo.from(errorMessage);
            }
        }
        return TestExecutionInfo.from();
    }

    private void showTestsSummary(List<TestClassInstance> testClassInstances) {
        System.out.println(DELIMETER);
        System.out.println("        Tests summary        \n");

        testClassInstances.forEach(t -> {
            System.out.println("Class: " + t.getClazz().getName());
            t.getTestMethods().forEach(test -> System.out.println("  -> " + test.getMethod()));
            System.out.println();
        });

        System.out.println(DELIMETER);
    }

    private void printTestResults(List<TestClassInstance> testClassInstances) {
        System.out.println(DELIMETER);
        System.out.println("Tests results: ");

        testClassInstances.forEach(t -> {
            System.out.println("Class: " + t.getClazz().getName());
            t.getTestMethods().forEach(
                    test -> test.getExecs().forEach(
                            exec -> System.out.println(String.format("  -> Test %s was %s",
                                    test.getMethod().getName(), (exec.isSuccessfullTest() ? "successfull" : "with errors -> " + exec.getErrorMessage())))
                    )
            );
        });
        System.out.println();
        System.out.println(String.format("Total tests: %s. Successfull: %s. Failed: %s.",
                totalTestsCount, successfullTestsCount, failedTestsCount));
        System.out.println(DELIMETER);
    }
}