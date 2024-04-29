package com.midel.exception;

import java.util.List;

public class AlreadyExistException extends ResponseException {

    public AlreadyExistException(String message) {
        super(message);
    }

    public AlreadyExistException() {
        super();
    }

    public AlreadyExistException(List<String> messages) {
        super(messages);
    }
}
