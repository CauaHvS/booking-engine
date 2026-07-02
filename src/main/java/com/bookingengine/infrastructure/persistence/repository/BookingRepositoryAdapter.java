package com.bookingengine.infrastructure.persistence.repository;

import com.bookingengine.domain.model.Booking;
import com.bookingengine.domain.model.BookingId;
import com.bookingengine.domain.port.out.BookingRepository;
import com.bookingengine.infrastructure.persistence.mapper.BookingMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BookingRepositoryAdapter implements BookingRepository {

    private final BookingJpaRepository jpaRepository;

    public BookingRepositoryAdapter(BookingJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Booking save(Booking booking) {
        var saved = jpaRepository.save(BookingMapper.toEntity(booking));
        return BookingMapper.toDomain(saved);
    }

    @Override
    public Optional<Booking> findById(BookingId id) {
        return jpaRepository.findById(id.value()).map(BookingMapper::toDomain);
    }

    @Override
    public List<Booking> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId)
                .stream()
                .map(BookingMapper::toDomain)
                .toList();
    }
}
