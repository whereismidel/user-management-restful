package com.midel.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.util.Date;

@Getter
public abstract class CustomResponse {

    protected final int status;
    protected final Date timestamp;

    public CustomResponse(HttpStatus status) {
        this.status = status.value();
        this.timestamp = new Date();
    }

    @JsonIgnore
    public ResponseEntity<?> getResponseEntity() {
        return new ResponseEntity<>(
                this,
                HttpStatusCode.valueOf(this.status)
        );
    }

}
