package com.midel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.midel.entity.User;
import com.midel.exception.AlreadyExistException;
import com.midel.exception.InvalidArgumentException;
import com.midel.exception.NotFoundException;
import com.midel.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void getAllUsers_validParameters() throws Exception {
        mockMvc.perform(get("/users")).andExpect(status().isOk());

        verify(userService, times(1)).getUsersWithPagination(0,20, null, null);
    }

    @Test
    void getUserList_invalidParameters() throws Exception {

        int page = -1;
        int size = -5;

        when(userService.getUsersWithPagination(page, size, null, null)).thenThrow(
                new InvalidArgumentException()
        );

        mockMvc.perform(
                get("/users")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
        ).andExpect(status().isBadRequest());

        verify(userService, times(1)).getUsersWithPagination(page,size, null, null);
    }

    @Test
    void getUserById_userFound() throws Exception {

        long id = 1;

        User user = new User(
                1L, "test@example.com", "Name", "Lastname",
                LocalDate.of(2000, 1, 1), "Address", "132456789"
        );

        when(userService.getUserById(id)).thenReturn(user);

        mockMvc.perform(get("/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.firstname").value("Name"))
                .andExpect(jsonPath("$.data.lastname").value("Lastname"))
                .andExpect(jsonPath("$.data.address").value("Address"))
                .andExpect(jsonPath("$.data.phoneNumber").value("132456789"));

        verify(userService, times(1)).getUserById(id);
    }

    @Test
    void getUserById_userNotFound() throws Exception {

        long id = 10000;

        when(userService.getUserById(id))
                .thenThrow(new NotFoundException());

        mockMvc.perform(get("/users/{id}", id))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(id);
    }

    @Test
    void createUser_validParameters() throws Exception {

        User user = new User(
                1L, "test@example.com", "Name", "Lastname",
                LocalDate.of(2000, 1, 1), null, null
        );

        when(userService.createUser(any(User.class))).thenReturn(user);

        mockMvc.perform(
                    post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(user))
                ).andExpect(status().isCreated())
                        .andExpect(jsonPath("$.data.id").value(user.getId()));

        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    void createUser_invalidParameters() throws Exception {

        User user = new User(
                1L, "test@example.com", "Name", "Lastname",
                LocalDate.of(2000, 1, 1), null, null
        );

        when(userService.createUser(any(User.class))).thenThrow(new AlreadyExistException());

        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(user))
                ).andExpect(status().isBadRequest());

        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    void deleteUser() throws Exception {
        long id = 1;

        mockMvc.perform(
                delete("/users/{id}", id)
        ).andExpect(status().isOk());

        verify(userService, times(1)).deleteUser(id);
    }

    @Test
    void updateUser_validParameters() throws Exception {

        long id = 1;

        User updateUser = new User(
                id, "test1@example.com", "Name", "Lastname",
                LocalDate.of(2000, 1, 1), null, null
        );

        when(userService.updateUser(eq(id), any(User.class)))
                .thenReturn(updateUser);

        mockMvc.perform(
                put("/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUser))
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.email").value("test1@example.com"));

        verify(userService, times(1)).updateUser(eq(id), any(User.class));
    }

    @Test
    void updateUser_invalidParameters() throws Exception {

        long id = 1;

        User updateUser = new User(
                id, "@test1@example@com", "Name", "Lastname",
                LocalDate.of(2024, 1, 1), null, null
        );

        when(userService.updateUser(eq(id), any(User.class)))
                .thenThrow(new InvalidArgumentException());

        mockMvc.perform(
                        put("/users/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateUser))
                ).andExpect(status().isBadRequest());

        verify(userService, times(1)).updateUser(eq(id), any(User.class));
    }

    @Test
    void updateUser_notFound() throws Exception {

        long id = -1;

        User updateUser = new User(
                id, "@test1@example@com", "Name", "Lastname",
                LocalDate.of(2024, 1, 1), null, null
        );

        when(userService.updateUser(eq(id), any(User.class)))
                .thenThrow(new NotFoundException());

        mockMvc.perform(
                put("/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUser))
        ).andExpect(status().isNotFound());

        verify(userService, times(1)).updateUser(eq(id), any(User.class));
    }

    @Test
    void partiallyUpdateUser_validParameters() throws Exception {

        long id = 1;

        User user = new User(
                id, "test@example.com", "Name", "Lastname",
                LocalDate.of(2000, 1, 1), null, null
        );

        User updateUser = new User(
                null, "test_update@example.com", null, null,
                null, null, null
        );

        when(userService.partiallyUpdateUser(eq(id), any(User.class)))
                .then(invocationOnMock -> {
                    user.setEmail(updateUser.getEmail());
                    return user;
                });

        mockMvc.perform(
                        patch("/users/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateUser))
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.email").value("test_update@example.com"));

        verify(userService, times(1)).partiallyUpdateUser(eq(id), any(User.class));
    }

    @Test
    void partiallyUpdateUser_invalidParameters() throws Exception {

        long id = 1;

        User updateUser = new User(
                null, "test_update@example.com", null, null,
                null, null, null
        );

        when(userService.partiallyUpdateUser(eq(id), any(User.class)))
                .thenThrow(new InvalidArgumentException());

        mockMvc.perform(
                patch("/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUser))
        ).andExpect(status().isBadRequest());

        verify(userService, times(1)).partiallyUpdateUser(eq(id), any(User.class));
    }

    @Test
    void partiallyUpdateUser_notFound() throws Exception {

        long id = -1;

        User updateUser = new User(
                null, "test_update@example.com", null, null,
                null, null, null
        );

        when(userService.partiallyUpdateUser(eq(id), any(User.class)))
                .thenThrow(new NotFoundException());

        mockMvc.perform(
                patch("/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUser))
        ).andExpect(status().isNotFound());

        verify(userService, times(1)).partiallyUpdateUser(eq(id), any(User.class));
    }
}