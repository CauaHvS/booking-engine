package com.bookingengine.infrastructure.persistence.mapper;

import com.bookingengine.domain.model.Booking;
import com.bookingengine.domain.model.BookingId;
import com.bookingengine.domain.model.SlotId;
import com.bookingengine.infrastructure.persistence.entity.BookingJpaEntity;

public class BookingMapper {

    private BookingMapper() {}

    public static Booking toDomain(BookingJpaEntity entity) {
        return new Booking(
                BookingId.of(entity.getId()),
                SlotId.of(entity.getSlotId()),
                entity.getUserId(),
                entity.getStatus()
        );
    }

    public static BookingJpaEntity toEntity(Booking booking) {
        var entity = new BookingJpaEntity();
        entity.setId(booking.getId().value());
        entity.setSlotId(booking.getSlotId().value());
        entity.setUserId(booking.getUserId());
        entity.setStatus(booking.getStatus());
        return entity;
    }
}
