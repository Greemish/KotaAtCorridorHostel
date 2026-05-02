package com.kota.controller;

import com.kota.dto.request.LoginRequest;
import com.kota.dto.request.RegisterRequest;
import com.kota.dto.response.JwtResponse;
import com.kota.model.User;
import com.kota.security.JwtTokenProvider;
import com.kota.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(userDetails);
        User user = userService.findByEmail(request.getEmail());
        return ResponseEntity.ok(new JwtResponse(token, user.getId(), user.getEmail(), user.getRole().name()));
    }

    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.registerStudent(request);
        UserDetails userDetails = new com.kota.security.UserPrincipal(user);
        String token = jwtTokenProvider.generateToken(userDetails);
        return ResponseEntity.ok(new JwtResponse(token, user.getId(), user.getEmail(), user.getRole().name()));
    }
}
