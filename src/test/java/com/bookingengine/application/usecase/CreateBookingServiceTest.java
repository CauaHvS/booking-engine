package com.bookingengine.application.usecase;

import com.bookingengine.domain.exception.SlotNotAvailableException;
import com.bookingengine.domain.exception.SlotNotFoundException;
import com.bookingengine.domain.model.Booking;
import com.bookingengine.domain.model.ResourceId;
import com.bookingengine.domain.model.Slot;
import com.bookingengine.domain.model.SlotId;
import com.bookingengine.domain.model.SlotStatus;
import com.bookingengine.domain.port.in.CreateBookingCommand;
import com.bookingengine.domain.port.out.BookingRepository;
import com.bookingengine.domain.port.out.SlotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateBookingServiceTest {

    @Mock
    private SlotRepository slotRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private CreateBookingService service;

    private static final SlotId SLOT_ID = SlotId.of(UUID.randomUUID());
    private static final String USER_ID = "user-42";
    private static final LocalDateTime START = LocalDateTime.of(2026, 8, 1, 9, 0);
    private static final LocalDateTime END   = LocalDateTime.of(2026, 8, 1, 10, 0);

    private Slot slotDisponivel() {
        return new Slot(SLOT_ID, ResourceId.of(UUID.randomUUID()), START, END, SlotStatus.AVAILABLE, 0L);
    }

    // --- happy path ---

    @Test
    void deveCriarReservaParaSlotDisponivel() {
        var slot = slotDisponivel();
        when(slotRepository.findByIdWithLock(SLOT_ID)).thenReturn(slot);
        when(slotRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.create(new CreateBookingCommand(SLOT_ID, USER_ID));

        assertEquals(SLOT_ID.value(), result.slotId());
        assertEquals(USER_ID, result.userId());
        assertEquals("CONFIRMED", result.status());
        assertNotNull(result.id());
    }

    @Test
    void deveReservarSlotAoCriarReserva() {
        var slot = slotDisponivel();
        when(slotRepository.findByIdWithLock(SLOT_ID)).thenReturn(slot);
        when(slotRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.create(new CreateBookingCommand(SLOT_ID, USER_ID));

        assertEquals(SlotStatus.RESERVED, slot.getStatus());
        verify(slotRepository).save(slot);
        verify(bookingRepository).save(any(Booking.class));
    }

    // --- slot não encontrado ---

    @Test
    void deveLancarExcecaoQuandoSlotNaoEncontrado() {
        when(slotRepository.findByIdWithLock(SLOT_ID))
                .thenThrow(new SlotNotFoundException("slot não encontrado: " + SLOT_ID.value()));

        assertThrows(SlotNotFoundException.class,
                () -> service.create(new CreateBookingCommand(SLOT_ID, USER_ID)));

        verifyNoInteractions(bookingRepository);
    }

    // --- slot já reservado ---

    @Test
    void deveLancarExcecaoQuandoSlotJaReservado() {
        var slotReservado = new Slot(SLOT_ID, ResourceId.of(UUID.randomUUID()),
                START, END, SlotStatus.RESERVED, 0L);
        when(slotRepository.findByIdWithLock(SLOT_ID)).thenReturn(slotReservado);

        assertThrows(SlotNotAvailableException.class,
                () -> service.create(new CreateBookingCommand(SLOT_ID, USER_ID)));

        verify(slotRepository, never()).save(any());
        verifyNoInteractions(bookingRepository);
    }
}
