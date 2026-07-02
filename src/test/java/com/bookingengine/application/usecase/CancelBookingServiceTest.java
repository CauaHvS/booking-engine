package com.bookingengine.application.usecase;

import com.bookingengine.domain.exception.BookingAlreadyCancelledException;
import com.bookingengine.domain.exception.BookingNotFoundException;
import com.bookingengine.domain.model.Booking;
import com.bookingengine.domain.model.BookingId;
import com.bookingengine.domain.model.BookingStatus;
import com.bookingengine.domain.model.ResourceId;
import com.bookingengine.domain.model.Slot;
import com.bookingengine.domain.model.SlotId;
import com.bookingengine.domain.model.SlotStatus;
import com.bookingengine.domain.port.in.CancelBookingCommand;
import com.bookingengine.domain.port.out.BookingRepository;
import com.bookingengine.domain.port.out.SlotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelBookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private SlotRepository slotRepository;

    @InjectMocks
    private CancelBookingService service;

    private static final LocalDateTime START = LocalDateTime.of(2026, 8, 1, 9, 0);
    private static final LocalDateTime END   = LocalDateTime.of(2026, 8, 1, 10, 0);

    @Test
    void deveCancelarReservaELiberarSlot() {
        var bookingId = BookingId.of(UUID.randomUUID());
        var slotId    = SlotId.of(UUID.randomUUID());
        var booking   = new Booking(bookingId, slotId, "user-42", BookingStatus.CONFIRMED);
        var slot      = new Slot(slotId, ResourceId.of(UUID.randomUUID()), START, END, SlotStatus.RESERVED, 1L);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(slotRepository.findByIdWithLock(slotId)).thenReturn(slot);
        when(bookingRepository.save(any())).thenReturn(booking);
        when(slotRepository.save(any())).thenReturn(slot);

        service.cancel(new CancelBookingCommand(bookingId, null));

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertEquals(SlotStatus.AVAILABLE, slot.getStatus());
        verify(bookingRepository).save(booking);
        verify(slotRepository).save(slot);
    }

    @Test
    void deveLancarExcecaoQuandoReservaNaoExistir() {
        var bookingId = BookingId.of(UUID.randomUUID());
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class,
                () -> service.cancel(new CancelBookingCommand(bookingId, null)));

        verify(slotRepository, never()).findByIdWithLock(any());
    }

    @Test
    void deveLancarExcecaoQuandoReservaJaCancelada() {
        var bookingId = BookingId.of(UUID.randomUUID());
        var slotId    = SlotId.of(UUID.randomUUID());
        var booking   = new Booking(bookingId, slotId, "user-42", BookingStatus.CANCELLED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(BookingAlreadyCancelledException.class,
                () -> service.cancel(new CancelBookingCommand(bookingId, null)));

        verify(slotRepository, never()).findByIdWithLock(any());
    }
}
