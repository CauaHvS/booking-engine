package com.bookingengine.domain.port.in;

public interface CreateBookingUseCase {

    BookingResult create(CreateBookingCommand command);
}
