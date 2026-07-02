package com.bookingengine.infrastructure.web.dto;

import java.util.UUID;

public record BookingResponse(UUID id, UUID slotId, String userId, String status) {}
