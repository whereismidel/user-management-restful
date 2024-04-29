package com.midel.exception;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public class ResponseException extends RuntimeException {

    protected List<String> messages;

    public ResponseException(String message) {
        super(message);
        this.messages = Collections.singletonList(message);
    }

    public ResponseException() {
        super();
        this.messages = Collections.emptyList();
    }

    public ResponseException(List<String> messages) {
        super(messages.toString());
        this.messages = messages;
    }
}
