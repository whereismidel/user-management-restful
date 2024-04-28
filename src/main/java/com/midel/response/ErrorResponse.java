package com.midel.response;


import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;

@Getter
public class ErrorResponse extends CustomResponse {
    private final List<String> message;

    public ErrorResponse(HttpStatus status, List<String> message) {
        super(status);

        this.message = message;
    }

    public ErrorResponse(HttpStatus status, String message) {
        this(status, Collections.singletonList(message));
    }
}
