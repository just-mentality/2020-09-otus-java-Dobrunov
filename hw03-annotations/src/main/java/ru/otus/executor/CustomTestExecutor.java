package ru.otus.executor;

import ru.otus.domain.TestClassInstance;
import ru.otus.domain.TestExecutionInfo;
import ru.otus.domain.TestMethod;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class CustomTestExecutor extends ITestExecutor<Collection<String>> {

    protected Collection<String> errors = new LinkedList<>();

    protected static final String DELIMETER = "==============================================================";
    protected static final String INNER_DELIMETER = "------------------------------------------------------------";
    protected static final String TEST_INFO_DELIMETER = "**********************************************************************************";

    @Override
    public void execute(final Collection<String> classes) {
        List<TestClassInstance> testClassInstances = new ArrayList<>();

        for (String className : classes) {
            if (!className.endsWith("Test")) {
                errors.add(String.format("Test class name must ends with postfix <%s>, but <%s> doesn't!", "Test",
                        className));
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
            errors.add(String.format("Class %s is not found ! Something went wrong...", testClassName));
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
        final String errrorMessage;
        final var throwable = exception.getCause();
        if (throwable != null) {
            errrorMessage = throwable.getMessage();
        } else {
            errrorMessage = exception.getMessage();
        }
        return errrorMessage;
    }

    private void increaseErrorsCounter(final Exception exception) {
        final String exceptionMessage = getErrorFromException(exception);
        errors.add(exceptionMessage);
        System.out.println(exceptionMessage);
        failedTestsCount++;
    }

    private void increaseErrorsCounter(final String message) {
        errors.add(message);
        System.out.println(message);
        failedTestsCount++;
    }

    private void runTests(final Collection<TestClassInstance> testClassInstances) {
        System.out.println("Testing is started.");

        for (final var testClassInstance : testClassInstances) {
            final Object testObj;
            try {
                testObj = testClassInstance.getClazz().getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                continue;
            }
            final List<TestMethod> testMethods = testClassInstance.getTestMethods();

            for (TestMethod method : testMethods) {
                System.out.println(TEST_INFO_DELIMETER);
                System.out.println(" --> " + testObj.getClass());
                totalTestsCount += 1;
                // try to run before methods
                try {
                    executeMethodsOnObject(testObj, testClassInstance.getBeforeMethods());
                } catch (Exception e) {
                    final String message = getErrorFromException(e);
                    increaseErrorsCounter(message);
                    method.setTestResult(new TestExecutionInfo(false, message));
                    continue;
                    // other before each will not called!
                }
                // try to run main tests
                try {
                    Method runningMethod = method.getMethod();
                    runningMethod.setAccessible(true); // like junit5
                    System.out.print(String.format("Running method [%s] -> ", runningMethod.getName()));
                    runningMethod.invoke(testObj);
                    method.setTestResult(new TestExecutionInfo(true, null));
                    successfullTestsCount += 1;
                } catch (Exception e) {
                    final String errorOccurred = getErrorFromException(e);
                    method.setTestResult(new TestExecutionInfo(false, errorOccurred));
                    increaseErrorsCounter(errorOccurred);
                    continue;
                }
                // try to run after methods
                try {
                    executeMethodsOnObject(testObj, testClassInstance.getAfterMethods());
                } catch (Exception e) {
                    increaseErrorsCounter(e);
                    continue;
                    // other after each will not called!
                }
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

    private void executeMethodsOnObject(final Object testObj, final List<Method> methods) throws Exception {
        for (Method method : methods) {
            System.out.print(String.format("Running method [%s] -> ", method.getName()));
            method.setAccessible(true); // like junit5
            method.invoke(testObj);
        }
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
                                    test.getMethod().getName(), (exec.isSuccessfull() ? "successfull" : "with errors -> " + exec.getErrorMessage())))
                    )
            );
        });
        System.out.println();
        System.out.println(String.format("Total tests: %s. Successfull: %s. Failed: %s.",
                totalTestsCount, successfullTestsCount, failedTestsCount));
        System.out.println(DELIMETER);
    }
}