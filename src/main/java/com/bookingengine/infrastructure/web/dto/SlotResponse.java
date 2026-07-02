package com.bookingengine.infrastructure.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SlotResponse(UUID id, UUID resourceId, LocalDateTime startTime, LocalDateTime endTime, String status) {}
