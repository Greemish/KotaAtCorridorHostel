package com.kota.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String phone;
    private boolean active;
    private LocalDateTime createdAt;
}
