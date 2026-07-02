package com.bookingengine.infrastructure.web.dto;

import com.bookingengine.domain.port.in.SlotResult;

import java.time.LocalDateTime;
import java.util.UUID;

public record SlotResponse(UUID id, UUID resourceId, LocalDateTime startTime, LocalDateTime endTime, String status) {

    public static SlotResponse from(SlotResult result) {
        return new SlotResponse(result.id(), result.resourceId(), result.startTime(), result.endTime(), result.status());
    }
}
