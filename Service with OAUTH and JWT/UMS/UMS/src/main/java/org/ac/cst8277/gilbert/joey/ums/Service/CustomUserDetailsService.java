package org.ac.cst8277.gilbert.joey.ums.Service;
import org.ac.cst8277.gilbert.joey.ums.Bean.Role;

import org.ac.cst8277.gilbert.joey.ums.Bean.User;
import org.ac.cst8277.gilbert.joey.ums.Repo.UserRepo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepo userRepository;

    public CustomUserDetailsService(UserRepo userRepository) {
        this.userRepository = userRepository;
    }
    // Overrode so i could just use email with LIKE sql to associate login to ums user
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                // No password only email required (login)
                .username(user.getEmail())
                .authorities(user.getRoles().stream().map(Role::getRolename).toArray(String[]::new))
                .build();
    }
}
