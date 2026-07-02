package com.bookingengine.domain.port.in;

import java.time.LocalDateTime;
import java.util.UUID;

public record SlotResult(UUID id, UUID resourceId, LocalDateTime startTime, LocalDateTime endTime, String status) {}
