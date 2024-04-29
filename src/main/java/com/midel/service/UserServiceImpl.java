package com.midel.service;

import com.midel.entity.User;
import com.midel.exception.AlreadyExistException;
import com.midel.exception.InvalidArgumentException;
import com.midel.exception.NotFoundException;
import com.midel.repository.UserRepository;
import com.midel.response.PaginationResponse;
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
    public User createUser(User user) {
        return save(user);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id = " + id + " not found."));
    }

    @Override
    public ResponseEntity<?> getUsersWithPagination(
            int page, int size,
            LocalDate from, LocalDate to
    ) {

        if (from != null && to != null && to.isBefore(from)) {
            throw new InvalidArgumentException("The 'to' value must be after the 'from' value");
        }

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath("/users")
                .queryParam("page", page)
                .queryParam("size", size);

        Pageable pageable = PageRequest.of(page, size);

        Specification<User> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (from != null && to != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("birthdate"), from));
                predicates.add(criteriaBuilder.lessThan(root.get("birthdate"), to));

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

        List<User> data = dataPage.get().toList();

        return new PaginationResponse(
                HttpStatus.OK, page, size, total,
                data,
                dataPage.hasNext()? nextPage : null,
                dataPage.hasPrevious()?prevPage : null
        ).getResponseEntity();
    }

    @Override
    public User updateUser(Long id, User user) {

        Optional<User> optionalUser = userRepository.findById(id);

        if (optionalUser.isEmpty()) {
            throw new NotFoundException("User with id = " + id + " does not exist");
        }

        User existingUser = optionalUser.get();
        existingUser.setEmail(user.getEmail());
        existingUser.setFirstname(user.getFirstname());
        existingUser.setLastname(user.getLastname());
        existingUser.setBirthdate(user.getBirthdate());
        existingUser.setAddress(user.getAddress());
        existingUser.setPhoneNumber(user.getPhoneNumber());

        return save(existingUser);
    }

    @Override
    public User partiallyUpdateUser(Long id, User user) {
        Optional<User> optionalUser = userRepository.findById(id);

        if (optionalUser.isEmpty()) {
            throw new NotFoundException("User with id = " + id + " does not exist");
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

        return save(existingUser);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    private User save(User user) {

        List<String> messages = validateUser(user);

        if (!messages.isEmpty()) {
            throw new InvalidArgumentException(messages);
        }

        try {
            return userRepository.save(user);

        } catch (DataIntegrityViolationException e) {
            throw new AlreadyExistException("A user with this email already exists.");
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
