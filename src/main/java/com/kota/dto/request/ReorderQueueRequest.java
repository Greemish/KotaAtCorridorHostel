package com.kota.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReorderQueueRequest {

    @NotEmpty
    private List<Long> orderedOrderIds;

    private String reason;
}
