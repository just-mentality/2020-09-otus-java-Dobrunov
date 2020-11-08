package ru.otus.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TestExecutionInfo {
    private boolean isSuccessfull;
    private String errorMessage;
}
