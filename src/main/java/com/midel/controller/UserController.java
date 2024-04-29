package com.midel.controller;

import com.midel.entity.User;
import com.midel.exception.AlreadyExistException;
import com.midel.exception.InvalidArgumentException;
import com.midel.exception.NotFoundException;
import com.midel.response.ErrorResponse;
import com.midel.response.UserResponse;
import com.midel.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Value("${server.address}")
    private String address;

    @Value("${server.port}")
    private int port;

    @GetMapping("")
    public ResponseEntity<?> getUserList(
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "20", required = false) int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        try {
            return userService.getUsersWithPagination(page, size, from, to);
        } catch (InvalidArgumentException e) {
            return new ErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage()).getResponseEntity();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        User user;

        try {
            user = userService.getUserById(id);
        } catch (NotFoundException e) {
            return new ErrorResponse(HttpStatus.NOT_FOUND, e.getMessage()).getResponseEntity();
        }

        return new UserResponse(
                HttpStatus.OK,
                user,
                String.format("http://%s:%d/users/%d", address, port, id)
        ).getResponseEntity();
    }

    @PostMapping("")
    public ResponseEntity<?> createUser(@RequestBody User user) {

        User createdUser;
        try {
            createdUser = userService.createUser(user);
        } catch (InvalidArgumentException | AlreadyExistException e) {
            return new ErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage()).getResponseEntity();
        }

        return new UserResponse(
                HttpStatus.CREATED,
                createdUser,
                String.format("http://%s:%d/users/%d", address, port, user.getId())
        ).getResponseEntity();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {

        userService.deleteUser(id);

        return ResponseEntity.ok(null);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user) {

        try {
            user = userService.updateUser(id, user);
        } catch (NotFoundException nfe) {
            return new ErrorResponse(HttpStatus.NOT_FOUND, nfe.getMessage()).getResponseEntity();
        } catch (InvalidArgumentException iae) {
            return new ErrorResponse(HttpStatus.BAD_REQUEST, iae.getMessage()).getResponseEntity();
        }

        return new UserResponse(
                HttpStatus.OK,
                user,
                String.format("http://%s:%d/users/%d", address, port, user.getId())
        ).getResponseEntity();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> partiallyUpdateUser(@PathVariable Long id, @RequestBody User user) {

        try {
            user = userService.partiallyUpdateUser(id, user);
        } catch (NotFoundException nfe) {
            return new ErrorResponse(HttpStatus.NOT_FOUND, nfe.getMessage()).getResponseEntity();
        } catch (InvalidArgumentException iae) {
            return new ErrorResponse(HttpStatus.BAD_REQUEST, iae.getMessage()).getResponseEntity();
        }

        return new UserResponse(
                HttpStatus.OK,
                user,
                String.format("http://%s:%d/users/%d", address, port, user.getId())
        ).getResponseEntity();
    }
}
