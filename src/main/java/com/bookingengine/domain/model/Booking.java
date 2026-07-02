package com.bookingengine.domain.model;

import com.bookingengine.domain.exception.BookingAlreadyCancelledException;

public class Booking {

    private final BookingId id;
    private final SlotId slotId;
    private final String userId;
    private BookingStatus status;

    public Booking(BookingId id, SlotId slotId, String userId, BookingStatus status) {
        if (id == null) {
            throw new IllegalArgumentException("id da reserva não pode ser nulo");
        }
        if (slotId == null) {
            throw new IllegalArgumentException("slotId da reserva não pode ser nulo");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId da reserva não pode ser vazio");
        }
        if (status == null) {
            throw new IllegalArgumentException("status da reserva não pode ser nulo");
        }
        this.id = id;
        this.slotId = slotId;
        this.userId = userId;
        this.status = status;
    }

    public void cancel() {
        if (status == BookingStatus.CANCELLED) {
            throw new BookingAlreadyCancelledException(
                    "reserva %s já está cancelada".formatted(id.value()));
        }
        this.status = BookingStatus.CANCELLED;
    }

    public boolean isConfirmed() {
        return status == BookingStatus.CONFIRMED;
    }

    public BookingId getId() { return id; }
    public SlotId getSlotId() { return slotId; }
    public String getUserId() { return userId; }
    public BookingStatus getStatus() { return status; }
}
