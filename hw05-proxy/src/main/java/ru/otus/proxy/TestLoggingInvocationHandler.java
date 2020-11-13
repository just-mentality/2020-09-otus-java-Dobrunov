package ru.otus.proxy;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import ru.otus.test.TestLogging;
import ru.otus.test.TestLoggingImpl;
import ru.otus.annotation.Log;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class TestLoggingInvocationHandler implements InvocationHandler {

    private final TestLogging testLogging;
    private List<Method> annotatedMethods;

    private static final String DELIMITER = " | ";

    private TestLoggingInvocationHandler(final TestLogging testLogging) {
        this.testLogging = testLogging;
        initProxyMethods();
    }

    public static TestLogging getInstance() {
        var handler = new TestLoggingInvocationHandler(new TestLoggingImpl());
        return (TestLogging) Proxy.newProxyInstance(TestLoggingInvocationHandler.class.getClassLoader(),
                new Class<?>[]{TestLogging.class}, handler);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (annotatedMethods.stream().anyMatch(m -> testMethodsOnEquality(m, method))) {
            log.info("Run method: {} {} {} with parameters -> {}",
                    method.getReturnType(), method.getName(), method.getParameterTypes(), getParamsAsString(args));
        }
        return method.invoke(testLogging, args);
    }

    private String getParamsAsString(final Object[] args) {
        val stringBuilder = new StringBuilder(DELIMITER);

        IntStream.range(0, args.length).forEach(i -> {
            stringBuilder.append(args[i]).append(DELIMITER);
        });
        return stringBuilder.toString();
    }

    private boolean testMethodsOnEquality(final Method first, final Method second) {
        // we should not rely on method's class and method's argument list with name of args - only types
        return first.getName().equals(second.getName())
                && Arrays.equals(first.getParameterTypes(), second.getParameterTypes())
                && first.getReturnType().equals(second.getReturnType());
    }

    private void initProxyMethods() {
        val methods = TestLoggingImpl.class.getMethods();
        annotatedMethods = Arrays.stream(methods)
                .filter(method -> Arrays.stream(method.getDeclaredAnnotations())
                        .anyMatch(TestLoggingInvocationHandler::testAnnotation))
                .collect(Collectors.toList());

        log.debug("Debug info: {}", annotatedMethods);
    }

    private static boolean testAnnotation(final Annotation annotation) {
        return annotation instanceof Log;
    }
}