package com.bookingengine.domain.port.in;

import com.bookingengine.domain.model.BookingId;

public record CancelBookingCommand(BookingId bookingId, String userId) {}
