package com.kota.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {

    private String firstName;

    private String lastName;

    private String phone;

    private String role;

    private Boolean active;
}
