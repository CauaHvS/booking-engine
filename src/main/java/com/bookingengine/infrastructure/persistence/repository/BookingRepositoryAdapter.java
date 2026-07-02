package com.bookingengine.infrastructure.persistence.repository;

import com.bookingengine.domain.model.Booking;
import com.bookingengine.domain.model.BookingId;
import com.bookingengine.domain.port.out.BookingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BookingRepositoryAdapter implements BookingRepository {

    @Override
    public Booking save(Booking booking) {
        throw new UnsupportedOperationException("não implementado");
    }

    @Override
    public Optional<Booking> findById(BookingId id) {
        throw new UnsupportedOperationException("não implementado");
    }

    @Override
    public List<Booking> findByUserId(String userId) {
        throw new UnsupportedOperationException("não implementado");
    }
}
