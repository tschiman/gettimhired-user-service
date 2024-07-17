package com.gettimhired.service;

import com.gettimhired.TestHelper;
import com.gettimhired.model.dto.UserDTO;
import com.gettimhired.model.mongo.User;
import com.gettimhired.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserService userService;

    private UserRepository userRepository;
    private RestTemplate restTemplate;
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    public void init() {
        userRepository = Mockito.mock(UserRepository.class);
        restTemplate = Mockito.mock(RestTemplate.class);
        userService = new UserService(userRepository, passwordEncoder, restTemplate);
    }

    @Test
    public void testFindUserByUsername() {
        when(userRepository.findById("BARK")).thenReturn(Optional.of(new User(TestHelper.ID, "BARK_PASSWORD", "email", "password", Collections.emptyList())));

        var userOpt = userService.findUserById("BARK");

        verify(userRepository, times(1)).findById("BARK");
        assertTrue(userOpt.isPresent());
    }

    @Test
    public void testCreateUser() {
        User user = new User(TestHelper.ID, "password", "email", "password2", Collections.emptyList());
        when(userRepository.save(any(User.class))).thenReturn(user);

        assertDoesNotThrow(() -> userService.createUser("email", "password"));

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testGeneratePassword() {
        var user = new User(TestHelper.ID, "password", "email", "password2", Collections.emptyList());
        var userDto = new UserDTO(TestHelper.ID, "password", "email", "password2", Collections.emptyList());
        when(userRepository.save(any(User.class))).thenReturn(user);

       var result = userService.generatePassword(userDto);

       assertNotNull(result);
       verify(userRepository, times(1)).save(any(User.class));
    }
}