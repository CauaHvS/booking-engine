package com.bookingengine.domain.port.in;

import java.util.List;

public interface QueryBookingsUseCase {

    List<BookingResult> findByUser(String userId);
}
