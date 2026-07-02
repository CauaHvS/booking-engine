package com.bookingengine.infrastructure.persistence.entity;

import com.bookingengine.domain.model.BookingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "bookings")
public class BookingJpaEntity {

    @Id
    private UUID id;

    @Column(name = "slot_id", nullable = false)
    private UUID slotId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getSlotId() { return slotId; }
    public void setSlotId(UUID slotId) { this.slotId = slotId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
}
