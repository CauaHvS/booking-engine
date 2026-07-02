package com.bookingengine.domain.port.in;

import com.bookingengine.domain.model.SlotId;

public record CreateBookingCommand(SlotId slotId, String userId) {}
