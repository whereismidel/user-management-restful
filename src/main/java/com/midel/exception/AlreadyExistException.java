package com.midel.exception;

import java.util.List;

public class AlreadyExistException extends RuntimeException {

    public AlreadyExistException(String message) {
        super(message);
    }

    public AlreadyExistException() {
        super();
    }

    public AlreadyExistException(List<String> messages) {
        super(String.join("\n", messages));
    }
}
