package com.gettimhired.controller;

import com.gettimhired.model.dto.UserDTO;
import com.gettimhired.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserAPI {
    Logger log = LoggerFactory.getLogger(UserAPI.class);

    private final UserService userService;

    public UserAPI(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}/id")
    @PreAuthorize("hasRole('SYSTEM')")
    public ResponseEntity<UserDTO> findUserById(@PathVariable String id) {
        log.info("GET /api/users/{id}/id findUserById id={}", id);

        try {
            Optional<UserDTO> userDtoOpt = userService.findUserById(id);
            return userDtoOpt
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{email}/email")
    @PreAuthorize("hasRole('SYSTEM')")
    public ResponseEntity<UserDTO> findUserByEmail(@PathVariable String email) {
        log.info("GET /api/users/{email}/email findUserByEmail email={}", email);

        try {
            Optional<UserDTO> userDtoOpt = userService.findUserByEmail(email);
            return userDtoOpt
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    //anyrequest
    public ResponseEntity createUser(@RequestParam String email, @RequestParam String password) {
        log.info("POST /api/users createUser email={}", email);

        try {
            userService.createUser(email, password);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{email}/password")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> generateApiPassword(@PathVariable String email) {
        log.info("POST /api/users/{email}/password generateApiPassword email={}", email);
        try {
            var userDtoOpt = userService.findUserByEmail(email);
            return userDtoOpt
                    .map(userDTO -> ResponseEntity.ok(userService.generatePassword(userDTO)))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
