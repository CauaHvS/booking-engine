package com.bookingengine.domain.port.in;

import java.util.UUID;

public record BookingResult(UUID id, UUID slotId, String userId, String status) {}
