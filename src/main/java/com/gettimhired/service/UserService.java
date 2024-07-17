package com.gettimhired.service;

import com.gettimhired.model.dto.UserDTO;
import com.gettimhired.model.mongo.User;
import com.gettimhired.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
}
