package com.bookingengine.infrastructure.web.controller;

import com.bookingengine.domain.port.in.CancelBookingUseCase;
import com.bookingengine.domain.port.in.CreateBookingUseCase;
import com.bookingengine.domain.port.in.QueryBookingsUseCase;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final CreateBookingUseCase createBookingUseCase;
    private final CancelBookingUseCase cancelBookingUseCase;
    private final QueryBookingsUseCase queryBookingsUseCase;

    public BookingController(CreateBookingUseCase createBookingUseCase,
                             CancelBookingUseCase cancelBookingUseCase,
                             QueryBookingsUseCase queryBookingsUseCase) {
        this.createBookingUseCase = createBookingUseCase;
        this.cancelBookingUseCase = cancelBookingUseCase;
        this.queryBookingsUseCase = queryBookingsUseCase;
    }
}
