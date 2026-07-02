package com.bookingengine.domain.model;

import java.util.UUID;

public record BookingId(UUID value) {

    public static BookingId of(UUID value) {
        return new BookingId(value);
    }
}
