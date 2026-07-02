package com.bookingengine.infrastructure.web.dto;

import java.util.UUID;

public record CreateBookingRequest(UUID slotId, String userId) {}
