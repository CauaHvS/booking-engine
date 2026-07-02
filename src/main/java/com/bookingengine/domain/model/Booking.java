package com.bookingengine.domain.model;

public class Booking {

    private final BookingId id;
    private final SlotId slotId;
    private final String userId;
    private BookingStatus status;

    public Booking(BookingId id, SlotId slotId, String userId, BookingStatus status) {
        this.id = id;
        this.slotId = slotId;
        this.userId = userId;
        this.status = status;
    }

    public BookingId getId() { return id; }
    public SlotId getSlotId() { return slotId; }
    public String getUserId() { return userId; }
    public BookingStatus getStatus() { return status; }
}
