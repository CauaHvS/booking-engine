package com.bookingengine.domain.model;

import java.util.UUID;

public record ResourceId(UUID value) {

    public static ResourceId of(UUID value) {
        return new ResourceId(value);
    }
}
