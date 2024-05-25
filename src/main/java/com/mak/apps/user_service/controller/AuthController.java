package com.mak.apps.user_service.controller;

import com.mak.apps.user_service.dto.AuthRequest;
import com.mak.apps.user_service.model.Role;
import com.mak.apps.user_service.model.User;
import com.mak.apps.user_service.repository.RoleRepository;
import com.mak.apps.user_service.repository.UserRepository;
import com.mak.apps.user_service.service.CustomUserDetailsService;
import com.mak.apps.user_service.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@RestController
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @PostMapping("/authenticate")
    public String createAuthenticationToken(@RequestBody AuthRequest authRequest) throws Exception {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());
        return jwtUtil.generateToken(userDetails,authRequest.getAppId());
    }

    @PostMapping("/register")
    public User registerUser(@RequestBody User user) {
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));

        Set<Role> roles = new HashSet<>();
        for (Role role : user.getRoles()) {
            Role dbRole = roleRepository.findByName(role.getName());
            if (dbRole == null) {
                dbRole = roleRepository.save(role);
            }
            roles.add(dbRole);
        }
        user.setRoles(roles);

        return userRepository.save(user);
    }
}

