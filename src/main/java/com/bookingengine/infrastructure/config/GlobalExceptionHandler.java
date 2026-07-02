package com.bookingengine.infrastructure.config;

import com.bookingengine.domain.exception.BookingAlreadyCancelledException;
import com.bookingengine.domain.exception.BookingNotFoundException;
import com.bookingengine.domain.exception.ResourceNotFoundException;
import com.bookingengine.domain.exception.SlotNotAvailableException;
import com.bookingengine.domain.exception.SlotNotFoundException;
import com.bookingengine.infrastructure.web.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SlotNotAvailableException.class)
    public ResponseEntity<ErrorResponse> handleSlotNotAvailable(SlotNotAvailableException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("SLOT_NOT_AVAILABLE", ex.getMessage()));
    }

    @ExceptionHandler(BookingAlreadyCancelledException.class)
    public ResponseEntity<ErrorResponse> handleBookingAlreadyCancelled(BookingAlreadyCancelledException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("BOOKING_ALREADY_CANCELLED", ex.getMessage()));
    }

    @ExceptionHandler(SlotNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSlotNotFound(SlotNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("SLOT_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBookingNotFound(BookingNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("BOOKING_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("RESOURCE_NOT_FOUND", ex.getMessage()));
    }
}
