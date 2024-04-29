package com.midel.exception;

import java.util.List;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException() {
        super();
    }

    public NotFoundException(List<String> messages) {
        super(String.join("\n", messages));
    }
}
