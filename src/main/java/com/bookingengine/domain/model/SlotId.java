package com.bookingengine.domain.model;

import java.util.UUID;

public record SlotId(UUID value) {

    public static SlotId of(UUID value) {
        return new SlotId(value);
    }
}
