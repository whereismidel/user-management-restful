package com.midel.exception;

import java.util.List;

public class InvalidArgumentException extends ResponseException {

    public InvalidArgumentException(String message) {
        super(message);
    }

    public InvalidArgumentException() {
        super();
    }

    public InvalidArgumentException(List<String> messages) {
        super(messages);
    }
}
