package com.acme.room_booking_system.config;

import com.acme.room_booking_system.security.AppAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    @Autowired
    private AppAuthenticationEntryPoint appAuthenticationEntryPoint;

    @Value("${spring.security.user.name}")
    private String username;

    @Value("${spring.security.user.password}")
    private String password;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                //disable CSRF as we're using stateless authentication and not session-based or form-based authentication
                .csrf(csrf -> csrf.disable())
                //allow swagger and h2 access without authentication
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/h2-console/**").permitAll()
                        .anyRequest().authenticated()
                )
                //disable frame options to allow H2 console access
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.disable())
                )
                //enable http basic authentication
                .httpBasic(withDefaults())
                //exception handling for 401 unauthorized responses
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(appAuthenticationEntryPoint)
                );

        return http.build();
    }

    //configure in-memory user for authentication with secure password hashing (BCrypt)
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
                .username(username)
                .password(passwordEncoder().encode(password))
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
