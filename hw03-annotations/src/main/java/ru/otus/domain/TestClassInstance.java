package ru.otus.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.otus.annotations.After;
import ru.otus.annotations.Before;
import ru.otus.annotations.Test;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class TestClassInstance {

    private Class<?> clazz;
    private List<Method> beforeMethods;
    private List<Method> afterMethods;
    private List<TestMethod> testMethods;

    public TestClassInstance(final Class<?> clazz) {
        this.clazz = clazz;
        beforeMethods = new LinkedList<>();
        afterMethods = new LinkedList<>();
        testMethods = new LinkedList<>();
    }

    public void saveMethodIfSatisfy(final Method method) {
        if (method.isAnnotationPresent(Before.class)) {
            addBeforeMethod(method);
        } else if (method.isAnnotationPresent(After.class)) {
            addAfterMethod(method);
        } else if (method.isAnnotationPresent(Test.class)) {
            addTestMethod(new TestMethod(method));
        }
    }

    public void addBeforeMethod(final Method method) {
        beforeMethods.add(method);
    }

    public void addAfterMethod(final Method method) {
        afterMethods.add(method);
    }

    public void addTestMethod(final TestMethod testMethod) {
        testMethods.add(testMethod);
    }

    public boolean hasTestMethods() {
        return !testMethods.isEmpty();
    }
}
