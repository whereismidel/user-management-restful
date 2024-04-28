package com.midel.service;

import com.midel.entity.User;
import com.midel.repository.UserRepository;
import com.midel.response.ErrorResponse;
import com.midel.response.PaginationResponse;
import com.midel.response.UserResponse;
import com.midel.utils.UserUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Value("${server.address}")
    private String address;

    @Value("${server.port}")
    private int port;

    @Value("${allowed-age}")
    private int allowedAge;

    @Override
    public ResponseEntity<?> createUser(User user) {

        List<String> messages = validateUser(user);

        if (!messages.isEmpty()) {
            return new ErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    messages
            ).getResponseEntity();
        }

        return save(user, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<?> getUserById(Long id) {
        Optional<User> user = userRepository.findById(id);

        if (user.isPresent()) {
            return new UserResponse(
                    HttpStatus.OK,
                    user.get(),
                    String.format("http://%s:%d/users/%d", address, port, id)
            ).getResponseEntity();
        } else {
            return new ErrorResponse(
                    HttpStatus.NOT_FOUND,
                    "User with id = " + id + " not found."
            ).getResponseEntity();
        }
    }

    @Override
    public ResponseEntity<?> getUsersWithPagination(
            int page, int size,
            LocalDate from, LocalDate to
    ) {

        if (from != null && to != null && to.isBefore(from)) {
            return new ErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    "The 'to' value must be after the 'from' value"
            ).getResponseEntity();
        }

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath("/users")
                .queryParam("page", page)
                .queryParam("size", size);

        Pageable pageable = PageRequest.of(page, size);

        Specification<User> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (from != null && to != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("birthDate"), from));
                predicates.add(criteriaBuilder.lessThan(root.get("birthDate"), to));

                uriBuilder.replaceQueryParam("from", from);
                uriBuilder.replaceQueryParam("to", to);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<User> dataPage = userRepository.findAll(specification, pageable);

        uriBuilder.replaceQueryParam("page", Math.min(page+1, dataPage.getTotalPages()));
        String nextPage = String.format("http://%s:%d%s", address, port, uriBuilder.toUriString());

        uriBuilder.replaceQueryParam("page", Math.max(page-1, 0));
        String prevPage = String.format("http://%s:%d%s", address, port, uriBuilder.toUriString());

        long total = dataPage.getTotalElements();

        Stream<User> data = dataPage.get();

        return new PaginationResponse(
                HttpStatus.OK, page, size, total,
                data,
                dataPage.hasNext()? nextPage : null,
                dataPage.hasPrevious()?prevPage : null
        ).getResponseEntity();
    }

    @Override
    public ResponseEntity<?> updateUser(Long id, User user) {

        Optional<User> optionalUser = userRepository.findById(id);

        if (optionalUser.isEmpty()) {
            return new ErrorResponse(
                    HttpStatus.NOT_FOUND,
                    "User with id = " + id + " does not exist"
            ).getResponseEntity();
        }

        List<String> messages = validateUser(user);
        if (!messages.isEmpty()) {
            return new ErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    messages
            ).getResponseEntity();
        }

        User existingUser = optionalUser.get();
        existingUser.setEmail(user.getEmail());
        existingUser.setFirstname(user.getFirstname());
        existingUser.setLastname(user.getLastname());
        existingUser.setBirthdate(user.getBirthdate());
        existingUser.setAddress(user.getAddress());
        existingUser.setPhoneNumber(user.getPhoneNumber());

        return save(existingUser, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> partiallyUpdateUser(Long id, User user) {
        Optional<User> optionalUser = userRepository.findById(id);

        if (optionalUser.isEmpty()) {
            return new ErrorResponse(
                    HttpStatus.NOT_FOUND,
                    "User with id = " + id + " does not exist"
            ).getResponseEntity();
        }

        User existingUser = optionalUser.get();
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getFirstname() != null) {
            existingUser.setFirstname(user.getFirstname());
        }
        if (user.getLastname() != null) {
            existingUser.setLastname(user.getLastname());
        }
        if (user.getBirthdate() != null) {
            existingUser.setBirthdate(user.getBirthdate());
        }
        if (user.getAddress() != null) {
            existingUser.setAddress(user.getAddress());
        }
        if (user.getPhoneNumber() != null) {
            existingUser.setPhoneNumber(user.getPhoneNumber());
        }

        List<String> messages = validateUser(existingUser);
        if (!messages.isEmpty()) {
            return new ErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    messages
            ).getResponseEntity();
        }

        return save(existingUser, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> deleteUser(Long id) {
        userRepository.deleteById(id);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private ResponseEntity<?> save(User user, HttpStatus successStatus) {
        try {
            user = userRepository.save(user);

            return new UserResponse(
                    successStatus,
                    user,
                    String.format("http://%s:%d/users/%d", address, port, user.getId())
            ).getResponseEntity();

        } catch (DataIntegrityViolationException e) {
            return new ErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    "A user with this email already exists."
            ).getResponseEntity();
        } catch (Exception e) {
            return new ErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            ).getResponseEntity();
        }
    }

    private List<String> validateUser(User user) {
        List<String> messages = new ArrayList<>();

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            messages.add("'email' is a required field.");
        }
        else if (!UserUtils.isEmailValid(user.getEmail())){
            messages.add("Invalid email.");
        }

        if (user.getFirstname() == null ||  user.getFirstname().isBlank()) {
            messages.add("'firstname' is a required field.");
        }

        if (user.getLastname() == null ||  user.getLastname().isBlank()) {
            messages.add("'lastname' is a required field.");
        }

        if (user.getBirthdate() == null) {
            messages.add("'birthdate' is a required field.");
        }
        else if (!UserUtils.isBirthDateValid(user.getBirthdate(), allowedAge)) {
            messages.add("Invalid birthdate. Value must be earlier than current date and the user must be at least " + allowedAge + " years old.");
        }

        return messages;
    }
}
