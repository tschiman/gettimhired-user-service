package com.gettimhired.service;

import com.gettimhired.model.dto.UserDTO;
import com.gettimhired.model.mongo.User;
import com.gettimhired.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;

    @Value("${resumeuserservice.mainapp.host}")
    private String host;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.restTemplate = restTemplate;
    }

    public Optional<UserDTO> findUserById(String id) {
        return userRepository.findById(id).map(u -> new UserDTO(
                u.id(),
                u.password(),
                u.email(),
                u.emailPassword(),
                u.roles()
        ));
    }

    public void createUser(String email, String password) {
        var user = new User(
                UUID.randomUUID().toString(),
                null,
                email,
                passwordEncoder.encode(password),
                List.of("ROLE_USER")
        );
        userRepository.save(user);
    }

    public String generatePassword(UserDTO userDTO) {
        var password = UUID.randomUUID().toString();
        var userToSave = new User(
                userDTO.id(),
                passwordEncoder.encode(password),
                userDTO.email(),
                userDTO.emailPassword(),
                userDTO.roles()
        );

        userRepository.save(userToSave);

        return password;
    }

    public Optional<UserDTO> findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(u -> new UserDTO(
                        u.id(),
                        u.password(),
                        u.email(),
                        u.emailPassword(),
                        u.roles()
                ));
    }

    public void migrateUsers() {
        ResponseEntity<UserDTO[]> userDtos = restTemplate.getForEntity(host + "/api/users/migrate", UserDTO[].class);
        for (UserDTO userDTO : Objects.requireNonNull(userDtos.getBody())) {
            User userToSave = new User(
                    userDTO.id(),
                    userDTO.password(),
                    userDTO.email(),
                    userDTO.emailPassword(),
                    userDTO.roles() == null || userDTO.roles().isEmpty() ? List.of("ROLE_USER") : userDTO.roles()
            );
            userRepository.save(userToSave);
        }
    }
}
