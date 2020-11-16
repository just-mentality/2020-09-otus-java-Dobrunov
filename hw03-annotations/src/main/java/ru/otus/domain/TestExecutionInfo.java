package ru.otus.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TestExecutionInfo {
    private boolean isSuccessfull;

    @Getter
    private String errorMessage;

    public static TestExecutionInfo from(final String errorMessage) {
        return new TestExecutionInfo(false, errorMessage);
    }

    public static TestExecutionInfo from() {
        return new TestExecutionInfo(true, "");
    }

    public boolean isFailedTest() {
        return !isSuccessfull;
    }

    public boolean isSuccessfullTest() {
        return isSuccessfull;
    }
}
