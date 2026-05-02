package com.kota.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateQueuePositionRequest {

    @NotNull
    private Long orderId;

    @NotNull
    private Integer newPosition;
}
