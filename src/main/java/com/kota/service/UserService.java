package com.kota.service;

import com.kota.dto.request.RegisterRequest;
import com.kota.dto.request.UpdateUserRequest;
import com.kota.dto.response.UserResponse;
import com.kota.exception.BusinessException;
import com.kota.exception.ResourceNotFoundException;
import com.kota.model.User;
import com.kota.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User registerStudent(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already in use: " + request.getEmail());
        }
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(User.Role.STUDENT);
        user.setActive(true);
        return userRepository.save(user);
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = findById(id);
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getRole() != null) user.setRole(User.Role.valueOf(request.getRole()));
        if (request.getActive() != null) user.setActive(request.getActive());
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id, String performedBy) {
        User user = findById(id);
        user.setActive(false);
        userRepository.save(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void changePassword(Long id, String newPassword) {
        User user = findById(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public UserResponse toResponse(User user) {
        UserResponse r = new UserResponse();
        r.setId(user.getId());
        r.setFirstName(user.getFirstName());
        r.setLastName(user.getLastName());
        r.setEmail(user.getEmail());
        r.setRole(user.getRole().name());
        r.setPhone(user.getPhone());
        r.setActive(user.isActive());
        r.setCreatedAt(user.getCreatedAt());
        return r;
    }
}
