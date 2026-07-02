package com.bookingengine.infrastructure.web.dto;

import com.bookingengine.domain.port.in.BookingResult;

import java.util.UUID;

public record BookingResponse(UUID id, UUID slotId, String userId, String status) {

    public static BookingResponse from(BookingResult result) {
        return new BookingResponse(result.id(), result.slotId(), result.userId(), result.status());
    }
}
