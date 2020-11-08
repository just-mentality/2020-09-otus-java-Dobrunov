package ru.otus.executor;

import java.util.Optional;

public abstract class ITestExecutor<T> {

    protected int totalTestsCount = 0;
    protected int successfullTestsCount = 0;
    protected int failedTestsCount = 0;

    protected abstract void execute(final T classes);

    /**
     * Try to load class by it's class name.
     *
     * @param testClassName
     * @return
     */
    protected Optional<Class<?>> loadClass(final String testClassName) {
        final Class<?> clazz;
        try {
            clazz = Class.forName(testClassName);
        } catch (ClassNotFoundException ex) {
            return Optional.empty();
        }
        return Optional.of(clazz);
    }

}
