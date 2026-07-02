package com.bookingengine.domain.port.in;

public interface CancelBookingUseCase {

    void cancel(CancelBookingCommand command);
}
