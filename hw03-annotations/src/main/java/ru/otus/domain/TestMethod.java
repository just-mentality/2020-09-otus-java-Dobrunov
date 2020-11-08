package ru.otus.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class TestMethod {

    private Method method;
    private Collection<TestExecutionInfo> execs;

    public TestMethod(final Method method) {
        this.method = method;
        this.execs = new ArrayList<>();
    }

    public void setTestResult(final TestExecutionInfo testExec) {
        execs.add(testExec);
    }
}
