package com.midel.service;

import com.midel.entity.User;
import com.midel.exception.AlreadyExistException;
import com.midel.exception.InvalidArgumentException;
import com.midel.exception.NotFoundException;
import com.midel.repository.UserRepository;
import com.midel.response.PaginationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;


    private final int allowedAge = 18;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "allowedAge", allowedAge);
    }

    @Configuration
    public static class MockConfig{
        @Bean
        public Properties myProps(){
            Properties properties = new Properties();
            properties.setProperty("allowed-age", "18");
            return properties;
        }
    }

    @Test
    void createUser_validUser() {
        // Arrange
        User userToCreate = new User(
                1L, "test@example.com", "Name", "Lastname",
                LocalDate.of(2000, 1, 1), "Address", "132456789"
        );

        when(userRepository.save(any(User.class))).thenReturn(userToCreate);

        User created = userService.createUser(userToCreate);

        // Assert
        assertEquals(userToCreate, created);
        verify(userRepository, times(1)).save(userToCreate);
    }

    @Test
    void createUser_emailAlreadyExist() {
        // Arrange
        User userToCreate = new User(
                null, "test@example.com", "Name", "Lastname",
                LocalDate.of(2000, 1, 1), "Address", "132456789"
        );

        when(userRepository.save(userToCreate)).thenThrow(
                new DataIntegrityViolationException("")
        );

        // Act
        AlreadyExistException exception = assertThrows(
                AlreadyExistException.class,
                () -> userService.createUser(userToCreate)
        );

        // Assert
        assertEquals("A user with this email already exists.", exception.getMessage());

    }

    @Test
    void createUser_invalidEmail() {
        // Arrange
        User userToCreate = new User(
                null, "test@@@example.com", "Name", "Lastname",
                LocalDate.of(2000, 1, 1), "Address", "132456789"
        );

        // Act
        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> userService.createUser(userToCreate)
        );

        // Assert
        assertThat(exception.getMessages()).contains("Invalid email.");
    }

    @Test
    void createUser_invalidBirthdate() {
        // Arrange
        User userToCreate = new User(
                null, "test@example.com", "Name", "Lastname",
                LocalDate.of(2020, 1, 1), "Address", "132456789"
        );

        // Act
        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> userService.createUser(userToCreate)
        );

        // Assert
        assertThat(exception.getMessages()).contains("Invalid birthdate. Value must be earlier than current date and the user must be at least " + allowedAge + " years old.");
    }

    @Test
    void saveUser_requiredFieldIsNull() {
        // Arrange
        User userToCreate = new User(
                null, null, null, null, null,
                "Address", "132456789"
        );

        // Act
        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> userService.createUser(userToCreate)
        );

        // Assert
        assertThat(exception.getMessage()).contains("'email' is a required field.");
        assertThat(exception.getMessage()).contains("'firstname' is a required field.");
        assertThat(exception.getMessage()).contains("'lastname' is a required field.");
        assertThat(exception.getMessage()).contains("'birthdate' is a required field.");
    }

    @Test
    void saveUser_requiredFieldIsBlank() {
        // Arrange
        User userToCreate = new User(
                null, "", "", "",
                LocalDate.of(2000, 1, 1), "Address", "132456789"
        );

        // Act
        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> userService.createUser(userToCreate)
        );

        // Assert
        assertThat(exception.getMessage()).contains("'email' is a required field.");
        assertThat(exception.getMessage()).contains("'firstname' is a required field.");
        assertThat(exception.getMessage()).contains("'lastname' is a required field.");
    }

    @Test
    void getUserById_userFound() {
        // Arrange
        long id = 1;

        User userInDb = new User(
                id, "test@example.com", "Name", "Lastname",
                LocalDate.of(2000, 1, 1), "Address", "132456789"
        );

        when(userRepository.findById(eq(id))).thenReturn(Optional.of(userInDb));

        // Act
        User foundUser = userService.getUserById(id);

        // Assert
        assertEquals(foundUser, userInDb);
        verify(userRepository, times(1)).findById(eq(id));
    }

    @Test
    void getUserById_userNotFound() {
        // Arrange
        long id = 1;

        when(userRepository.findById(eq(id))).thenReturn(Optional.empty());

        // Act
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.getUserById(id)
        );

        // Assert
        assertEquals("User with id = " + id + " not found.", exception.getMessage());
        verify(userRepository, times(1)).findById(eq(id));
    }

    @Test
    void deleteUser() {
        // Arrange
        doNothing().when(userRepository).deleteById(any(Long.class));

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository, times(1)).deleteById(any(Long.class));
    }

    @Test
    void updateUser_validUser() {
        // Arrange
        long id = 1;

        User userInDB = new User(
                id, "test@example.com", "Name", "Lastname",
                LocalDate.of(2000, 1, 1), "Address", "132456789"
        );

        User toUpdate = new User(
                null, "test-update@example.com", "Name-update", "Lastname-update",
                LocalDate.of(2000, 1, 5), "Address-update", "1324567890"
        );

        User resultUser = new User(
                id, "test-update@example.com", "Name-update", "Lastname-update",
                LocalDate.of(2000, 1, 5), "Address-update", "1324567890"
        );

        when(userRepository.findById(eq(id))).thenReturn(Optional.of(userInDB));
        when(userRepository.save(any(User.class))).thenReturn(resultUser);

        // Act
        User updated = userService.updateUser(id, toUpdate);

        // Assert
        assertEquals(resultUser, updated);

        verify(userRepository, times(1)).findById(any(Long.class));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_userNotFound() {
        // Arrange
        long id = -1;

        User toUpdate = new User(
                null, "test-update@example.com", "Name-update", "Lastname-update",
                LocalDate.of(2000, 1, 5), "Address-update", "1324567890"
        );

        when(userRepository.findById(eq(id))).thenReturn(Optional.empty());

        // Act
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.updateUser(id, toUpdate)
        );

        // Assert
        assertEquals("User with id = " + id + " does not exist", exception.getMessage());

        verify(userRepository, times(1)).findById(any(Long.class));
    }

    @Test
    void partiallyUpdateUser_validUserWithAllField() {
        // Arrange
        long id = 1;

        User userInDB = new User(
                id, "test@example.com", "Name", "Lastname",
                LocalDate.of(2000, 1, 1), "Address", "132456789"
        );

        User toUpdate = new User(
                null, "test-update@example.com", "Name-update", "Lastname-update",
                LocalDate.of(2000, 1, 5), "Address-update", "1324567890"
        );

        User resultUser = new User(
                id, "test-update@example.com", "Name-update", "Lastname-update",
                LocalDate.of(2000, 1, 5), "Address-update", "1324567890"
        );

        // Act
        when(userRepository.findById(eq(id))).thenReturn(Optional.of(userInDB));
        when(userRepository.save(any(User.class))).thenReturn(resultUser);

        User updated = userService.partiallyUpdateUser(id, toUpdate);

        // Assert
        assertEquals(resultUser, updated);

        verify(userRepository, times(1)).findById(any(Long.class));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void partiallyUpdateUser_validUserWithoutUpdateFields() {
        // Arrange
        long id = 1;

        User userInDB = new User(
                id, "test@example.com", "Name", "Lastname",
                LocalDate.of(2000, 1, 1), "Address", "132456789"
        );

        User toUpdate = new User(
                null, null, null, null,
                null, null, null
        );

        User resultUser = new User(
                id, "test@example.com", "Name", "Lastname",
                LocalDate.of(2000, 1, 1), "Address", "132456789"
        );

        when(userRepository.findById(eq(id))).thenReturn(Optional.of(userInDB));
        when(userRepository.save(any(User.class))).thenReturn(resultUser);

        // Act
        User updated = userService.partiallyUpdateUser(id, toUpdate);

        // Assert
        assertEquals(resultUser, updated);

        verify(userRepository, times(1)).findById(any(Long.class));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void partiallyUser_userNotFound() {
        // Arrange
        long id = -1;

        User toUpdate = new User(
                null, "test-update@example.com", "Name-update", "Lastname-update",
                LocalDate.of(2000, 1, 5), "Address-update", "1324567890"
        );

        when(userRepository.findById(eq(id))).thenReturn(Optional.empty());

        // Act
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.partiallyUpdateUser(id, toUpdate)
        );

        // Assert
        assertEquals("User with id = " + id + " does not exist", exception.getMessage());

        verify(userRepository, times(1)).findById(any(Long.class));
    }

    @SuppressWarnings({"unchecked", "SuspiciousMethodCalls"})
    @Test
    void getUsersWithPagination_withDefaultParameters_withoutFilters_valid() {
        // Arrange
        int page = 0;
        int size = 20;
        List<User> userList = getUserList();

        Pageable pageable = PageRequest.of(page, size);

        Page<User> userPage = new PageImpl<>(userList, pageable, userList.size());

        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(userPage);

        // Act
        ResponseEntity<?> response = userService.getUsersWithPagination(page, size, null, null);
        PaginationResponse paginationResponse = (PaginationResponse) response.getBody();

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());

        assertNotNull(paginationResponse);
        assertNotNull(paginationResponse.getPagination());
        assertNotNull(paginationResponse.getData());
        assertNotNull(paginationResponse.getLinks());

        assertEquals(page, paginationResponse.getPagination().get("page"));
        assertEquals(size, paginationResponse.getPagination().get("size"));
        assertEquals(Math.min(userList.size(), size), paginationResponse.getData().size());
        paginationResponse.getData().forEach(u -> assertTrue(userList.contains(u)));

        verify(userRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getUsersWithPagination_invalidDateRange_throwsException() {
        // Arrange
        LocalDate from = LocalDate.of(2024, 4, 1);
        LocalDate to = LocalDate.of(2024, 3, 1);

        // Act & Assert
        assertThrows(InvalidArgumentException.class,
                () -> userService.getUsersWithPagination(0, 10, from, to));
    }

    @Test
    void getUsersWithPagination_validDateRange_returnsResponseEntityWithCorrectLinks() {
        // Arrange
        LocalDate from = LocalDate.of(2024, 3, 1);
        LocalDate to = LocalDate.of(2024, 4, 1);

        Page<User> mockedPage = mock(Page.class);

        when(mockedPage.hasNext()).thenReturn(true);
        when(mockedPage.hasPrevious()).thenReturn(true);
        when(mockedPage.getTotalElements()).thenReturn(100L);
        when(mockedPage.get()).thenReturn(getUserList().stream());
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(mockedPage);

        // Act
        ResponseEntity<?> responseEntity = userService.getUsersWithPagination(0, 10, from, to);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
    }

    @Test
    void getUsersWithPagination_noPreviousPage_returnsResponseEntityWithNextPageOnly() {
        // Arrange
        LocalDate from = LocalDate.of(2024, 3, 1);
        LocalDate to = LocalDate.of(2024, 4, 1);
        Page<User> mockedPage = mock(Page.class);
        when(mockedPage.hasNext()).thenReturn(true);
        when(mockedPage.hasPrevious()).thenReturn(false); // Simulate no previous page
        when(mockedPage.getTotalElements()).thenReturn(100L);
        when(mockedPage.get()).thenReturn(getUserList().stream());
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(mockedPage);

        // Act
        ResponseEntity<?> responseEntity = userService.getUsersWithPagination(0, 10, from, to);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
    }

    @Test
    void getUsersWithPagination_noNextPage_returnsResponseEntityWithPreviousPageOnly() {
        // Arrange
        LocalDate from = LocalDate.of(2024, 3, 1);
        LocalDate to = LocalDate.of(2024, 4, 1);
        Page<User> mockedPage = mock(Page.class);
        when(mockedPage.hasNext()).thenReturn(false); // Simulate no next page
        when(mockedPage.hasPrevious()).thenReturn(true);
        when(mockedPage.getTotalElements()).thenReturn(100L);
        when(mockedPage.get()).thenReturn(getUserList().stream());
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(mockedPage);

        // Act
        ResponseEntity<?> responseEntity = userService.getUsersWithPagination(0, 10, from, to);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
    }

    private List<User> getUserList() {
        return IntStream.range(1, 10).mapToObj(i -> new User(
                (long) i, i + "test@example.com", i + "Name", i + "Lastname",
                LocalDate.of(2000, i, i), i + "Address", i + "32456789"
        )).toList();
    }
}