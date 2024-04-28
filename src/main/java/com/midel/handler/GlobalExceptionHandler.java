package com.midel.handler;

import com.midel.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> defaultEntry(Exception e) {
        return new ErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
                )
                .getResponseEntity();
    }
}
