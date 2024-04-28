package com.midel.service;

import com.midel.entity.User;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

public interface UserService {

    ResponseEntity<?> createUser(User user);

    ResponseEntity<?> getUserById(Long id);

    ResponseEntity<?> getUsersWithPagination(int offset, int limit, LocalDate from, LocalDate to);

    ResponseEntity<?> updateUser(Long id, User user);

    ResponseEntity<?> partiallyUpdateUser(Long id, User user);

    ResponseEntity<?> deleteUser(Long id);
}
