package com.kota.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SystemSettingRequest {

    @NotBlank
    private String settingKey;

    private String settingValue;
}
