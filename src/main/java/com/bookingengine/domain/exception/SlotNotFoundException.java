package com.bookingengine.domain.exception;

public class SlotNotFoundException extends RuntimeException {

    public SlotNotFoundException(String message) {
        super(message);
    }
}
