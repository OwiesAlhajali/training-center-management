package com.trainingcenter.management.security;

import com.trainingcenter.management.entity.User;
import com.trainingcenter.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // Map UserType to Spring Security Authority (ROLE_ADMIN, ROLE_STUDENT, ROLE_TEACHER)
        String role = "ROLE_" + user.getUserType().toString();

        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getEmail())
                .password(user.getPassword())
                .authorities(new SimpleGrantedAuthority(role))
                .build();
    }
}


