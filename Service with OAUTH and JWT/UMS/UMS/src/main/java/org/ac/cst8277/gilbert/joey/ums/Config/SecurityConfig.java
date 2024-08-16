package org.ac.cst8277.gilbert.joey.ums.Config;

import org.ac.cst8277.gilbert.joey.ums.Bean.User;
import org.ac.cst8277.gilbert.joey.ums.Service.JwtService;
import org.ac.cst8277.gilbert.joey.ums.Repo.UserRepo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtService jwtService;
    private final UserRepo userRepository;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtService jwtService, UserRepo userRepository, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/index.html", "/users").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler((request, response, authentication) -> {
                            OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
                            String githubLogin = authToken.getPrincipal().getAttribute("login");

                            // Gather user details to generate token with
                            Optional<User> optionalUser = userRepository.findByEmail(githubLogin);
                            if (optionalUser.isPresent()) {
                                User user = optionalUser.get();

                                // Retrieve existing or create new token
                                String existingToken = (String) request.getSession().getAttribute("token");
                                String jwtToken = (existingToken != null && jwtService.validateTokenEndpoint(existingToken))
                                        ? existingToken
                                        : jwtService.createToken(githubLogin, user.getRoles());

                                // Set the token and redirect
                                request.getSession().setAttribute("token", jwtToken);
                                String redirectUri = (String) request.getSession().getAttribute("redirectUri");
                                response.sendRedirect(redirectUri != null ? redirectUri : "/demo?token=" + jwtToken);
                            } else {
                                // Handle missing user
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found.");
                            }
                        })
                )
                .csrf(csrf -> csrf.disable())
                // Add JWT filter for other requests
                .addFilterBefore(new JwtFilterConfig(jwtService, userDetailsService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
