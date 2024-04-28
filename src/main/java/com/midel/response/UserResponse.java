package com.midel.response;

import com.midel.entity.User;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UserResponse extends CustomResponse{
    private final Object data;
    private final String location;

    public UserResponse(HttpStatus status, User user, String location) {
        super(status);

        this.data = user;
        this.location = location;
    }
}
