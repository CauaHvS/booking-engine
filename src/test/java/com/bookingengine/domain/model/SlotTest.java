package com.bookingengine.domain.model;

import com.bookingengine.domain.exception.SlotNotAvailableException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SlotTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 7, 10, 9, 0);
    private static final LocalDateTime END   = LocalDateTime.of(2026, 7, 10, 10, 0);

    private Slot slotDisponivel() {
        return new Slot(
                SlotId.of(UUID.randomUUID()),
                ResourceId.of(UUID.randomUUID()),
                START, END,
                SlotStatus.AVAILABLE,
                0L
        );
    }

    // --- criação válida ---

    @Test
    void deveCriarSlotDisponivel() {
        var slot = slotDisponivel();

        assertEquals(SlotStatus.AVAILABLE, slot.getStatus());
        assertTrue(slot.isAvailable());
    }

    // --- invariantes de construção ---

    @Test
    void deveLancarExcecaoQuandoIdForNulo() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> new Slot(null, ResourceId.of(UUID.randomUUID()), START, END, SlotStatus.AVAILABLE, 0L));

        assertEquals("id do slot não pode ser nulo", ex.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoResourceIdForNulo() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> new Slot(SlotId.of(UUID.randomUUID()), null, START, END, SlotStatus.AVAILABLE, 0L));

        assertEquals("resourceId do slot não pode ser nulo", ex.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoStartTimeForNulo() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> new Slot(SlotId.of(UUID.randomUUID()), ResourceId.of(UUID.randomUUID()),
                        null, END, SlotStatus.AVAILABLE, 0L));

        assertEquals("horário de início não pode ser nulo", ex.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoEndTimeForNulo() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> new Slot(SlotId.of(UUID.randomUUID()), ResourceId.of(UUID.randomUUID()),
                        START, null, SlotStatus.AVAILABLE, 0L));

        assertEquals("horário de término não pode ser nulo", ex.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoEndTimeNaoForPosteriorAoStartTime() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> new Slot(SlotId.of(UUID.randomUUID()), ResourceId.of(UUID.randomUUID()),
                        END, START, SlotStatus.AVAILABLE, 0L));

        assertEquals("horário de término deve ser posterior ao de início", ex.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoStartTimeIgualAoEndTime() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> new Slot(SlotId.of(UUID.randomUUID()), ResourceId.of(UUID.randomUUID()),
                        START, START, SlotStatus.AVAILABLE, 0L));

        assertEquals("horário de término deve ser posterior ao de início", ex.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoStatusForNulo() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> new Slot(SlotId.of(UUID.randomUUID()), ResourceId.of(UUID.randomUUID()),
                        START, END, null, 0L));

        assertEquals("status do slot não pode ser nulo", ex.getMessage());
    }

    // --- comportamento: reserve() ---

    @Test
    void deveReservarSlotDisponivel() {
        var slot = slotDisponivel();

        slot.reserve();

        assertEquals(SlotStatus.RESERVED, slot.getStatus());
        assertFalse(slot.isAvailable());
    }

    @Test
    void deveLancarExcecaoAoReservarSlotJaReservado() {
        var slot = slotDisponivel();
        slot.reserve();

        var ex = assertThrows(SlotNotAvailableException.class, slot::reserve);

        assertTrue(ex.getMessage().contains("RESERVED"));
    }
}
