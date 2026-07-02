package com.bookingengine.domain.port.out;

import com.bookingengine.domain.model.Booking;
import com.bookingengine.domain.model.BookingId;

import java.util.List;
import java.util.Optional;

public interface BookingRepository {

    Booking save(Booking booking);

    Optional<Booking> findById(BookingId id);

    List<Booking> findByUserId(String userId);
}
