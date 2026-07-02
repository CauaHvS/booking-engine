package com.bookingengine.domain.model;

import com.bookingengine.domain.exception.BookingAlreadyCancelledException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BookingTest {

    private Booking bookingConfirmada() {
        return new Booking(
                BookingId.of(UUID.randomUUID()),
                SlotId.of(UUID.randomUUID()),
                "user-42",
                BookingStatus.CONFIRMED
        );
    }

    // --- criação válida ---

    @Test
    void deveCriarReservaConfirmada() {
        var booking = bookingConfirmada();

        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertTrue(booking.isConfirmed());
    }

    // --- invariantes de construção ---

    @Test
    void deveLancarExcecaoQuandoIdForNulo() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> new Booking(null, SlotId.of(UUID.randomUUID()), "user-1", BookingStatus.CONFIRMED));

        assertEquals("id da reserva não pode ser nulo", ex.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoSlotIdForNulo() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> new Booking(BookingId.of(UUID.randomUUID()), null, "user-1", BookingStatus.CONFIRMED));

        assertEquals("slotId da reserva não pode ser nulo", ex.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoUserIdForNulo() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> new Booking(BookingId.of(UUID.randomUUID()), SlotId.of(UUID.randomUUID()),
                        null, BookingStatus.CONFIRMED));

        assertEquals("userId da reserva não pode ser vazio", ex.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoUserIdForVazio() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> new Booking(BookingId.of(UUID.randomUUID()), SlotId.of(UUID.randomUUID()),
                        "   ", BookingStatus.CONFIRMED));

        assertEquals("userId da reserva não pode ser vazio", ex.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoStatusForNulo() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> new Booking(BookingId.of(UUID.randomUUID()), SlotId.of(UUID.randomUUID()),
                        "user-1", null));

        assertEquals("status da reserva não pode ser nulo", ex.getMessage());
    }

    // --- comportamento: cancel() ---

    @Test
    void deveCancelarReservaConfirmada() {
        var booking = bookingConfirmada();

        booking.cancel();

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertFalse(booking.isConfirmed());
    }

    @Test
    void deveLancarExcecaoAoCancelarReservaJaCancelada() {
        var booking = bookingConfirmada();
        booking.cancel();

        var ex = assertThrows(BookingAlreadyCancelledException.class, booking::cancel);

        assertTrue(ex.getMessage().contains("já está cancelada"));
    }
}
