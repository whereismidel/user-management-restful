package com.midel.service;

import com.midel.entity.User;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

public interface UserService {

    User createUser(User user);

    User getUserById(Long id);

    ResponseEntity<?> getUsersWithPagination(int page, int size, LocalDate from, LocalDate to);

    User updateUser(Long id, User user);

    User partiallyUpdateUser(Long id, User user);

    void deleteUser(Long id);
}
