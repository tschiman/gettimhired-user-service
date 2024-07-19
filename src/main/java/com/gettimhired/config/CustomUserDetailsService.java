package com.gettimhired.config;

import com.gettimhired.model.dto.CustomUserDetails;
import com.gettimhired.service.UserService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserService userService;

    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        var userOpt = userService.findUserById(id);

        if (userOpt.isPresent()) {
            return new CustomUserDetails(
                    userOpt.get().id(),
                    userOpt.get().password(),
                    userOpt.get().roles().stream().map(SimpleGrantedAuthority::new).toList()
            );
        } else {
            throw new UsernameNotFoundException("User not found");
        }
    }
}
