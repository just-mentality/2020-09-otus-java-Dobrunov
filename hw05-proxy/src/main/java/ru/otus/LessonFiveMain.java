package ru.otus;

import lombok.val;
import ru.otus.proxy.TestLoggingInvocationHandler;

public class LessonFiveMain {

    public static void main(String[] args) {
        val testLogging = TestLoggingInvocationHandler.getInstance();
        testLogging.calculation(11);
        testLogging.calculation(3, 5);   // shouldn't log cause no annotation Test on this method
        testLogging.calculation(1, 4, "9");
    }
}
