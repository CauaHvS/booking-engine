package com.bookingengine.infrastructure.persistence.repository;

import com.bookingengine.infrastructure.persistence.entity.BookingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookingJpaRepository extends JpaRepository<BookingJpaEntity, UUID> {

    List<BookingJpaEntity> findByUserId(String userId);
}
