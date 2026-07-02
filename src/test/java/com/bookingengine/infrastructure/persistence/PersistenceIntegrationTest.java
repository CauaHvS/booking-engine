package com.bookingengine.infrastructure.persistence;

import com.bookingengine.BaseIntegrationTest;
import com.bookingengine.domain.model.Booking;
import com.bookingengine.domain.model.BookingId;
import com.bookingengine.domain.model.BookingStatus;
import com.bookingengine.domain.model.Resource;
import com.bookingengine.domain.model.ResourceId;
import com.bookingengine.domain.model.Slot;
import com.bookingengine.domain.model.SlotId;
import com.bookingengine.domain.model.SlotStatus;
import com.bookingengine.infrastructure.persistence.repository.BookingRepositoryAdapter;
import com.bookingengine.infrastructure.persistence.repository.ResourceRepositoryAdapter;
import com.bookingengine.infrastructure.persistence.repository.SlotRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class PersistenceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ResourceRepositoryAdapter resourceAdapter;

    @Autowired
    private SlotRepositoryAdapter slotAdapter;

    @Autowired
    private BookingRepositoryAdapter bookingAdapter;

    private static final LocalDateTime START = LocalDateTime.of(2026, 8, 1, 9, 0);
    private static final LocalDateTime END   = LocalDateTime.of(2026, 8, 1, 10, 0);

    // --- Resource ---

    @Test
    void deveSalvarERecuperarResource() {
        var resource = salvarResource();

        var encontrado = resourceAdapter.findById(resource.getId());

        assertTrue(encontrado.isPresent());
        assertEquals(resource.getId(), encontrado.get().getId());
        assertEquals("Sala A", encontrado.get().getName());
    }

    @Test
    void deveRetornarVazioParaResourceInexistente() {
        var resultado = resourceAdapter.findById(ResourceId.of(UUID.randomUUID()));

        assertTrue(resultado.isEmpty());
    }

    // --- Slot ---

    @Test
    void deveSalvarERecuperarSlot() {
        var resource = salvarResource();
        var slot = salvarSlot(resource.getId());

        var encontrado = slotAdapter.findByIdWithLock(slot.getId());

        assertEquals(slot.getId(), encontrado.getId());
        assertEquals(SlotStatus.AVAILABLE, encontrado.getStatus());
        assertNotNull(encontrado.getVersion());
    }

    @Test
    void deveListarSlotsDisponiveisPorResource() {
        var resource = salvarResource();
        salvarSlot(resource.getId());

        var disponiveis = slotAdapter.findAvailableByResourceId(resource.getId());

        assertEquals(1, disponiveis.size());
        assertEquals(SlotStatus.AVAILABLE, disponiveis.get(0).getStatus());
    }

    @Test
    void deveIncrementarVersaoAoSalvarSlotReservado() {
        var resource = salvarResource();
        var slot = salvarSlot(resource.getId());
        var versaoOriginal = slot.getVersion();

        slot.reserve();
        var atualizado = slotAdapter.save(slot);

        assertEquals(SlotStatus.RESERVED, atualizado.getStatus());
        assertEquals(versaoOriginal + 1, atualizado.getVersion());
    }

    @Test
    void naoDeveListarSlotReservadoComoDisponivel() {
        var resource = salvarResource();
        var slot = salvarSlot(resource.getId());

        slot.reserve();
        slotAdapter.save(slot);

        var disponiveis = slotAdapter.findAvailableByResourceId(resource.getId());
        assertTrue(disponiveis.isEmpty());
    }

    // --- Booking ---

    @Test
    void deveSalvarERecuperarBooking() {
        var resource = salvarResource();
        var slot = salvarSlot(resource.getId());
        var booking = salvarBooking(slot.getId(), "user-42");

        var encontrado = bookingAdapter.findById(booking.getId());

        assertTrue(encontrado.isPresent());
        assertEquals("user-42", encontrado.get().getUserId());
        assertEquals(BookingStatus.CONFIRMED, encontrado.get().getStatus());
    }

    @Test
    void deveListarBookingsPorUserId() {
        var resource = salvarResource();
        var slot1 = salvarSlot(resource.getId());
        var slot2 = salvarSlot(resource.getId());
        salvarBooking(slot1.getId(), "user-42");
        salvarBooking(slot2.getId(), "user-42");
        salvarBooking(slot1.getId(), "user-99");

        var bookings = bookingAdapter.findByUserId("user-42");

        assertEquals(2, bookings.size());
        assertTrue(bookings.stream().allMatch(b -> "user-42".equals(b.getUserId())));
    }

    @Test
    void deveRetornarVazioParaBookingInexistente() {
        var resultado = bookingAdapter.findById(BookingId.of(UUID.randomUUID()));

        assertTrue(resultado.isEmpty());
    }

    // --- helpers ---

    private Resource salvarResource() {
        return resourceAdapter.save(new Resource(ResourceId.of(UUID.randomUUID()), "Sala A"));
    }

    private Slot salvarSlot(ResourceId resourceId) {
        return slotAdapter.save(new Slot(
                SlotId.of(UUID.randomUUID()),
                resourceId,
                START, END,
                SlotStatus.AVAILABLE,
                null
        ));
    }

    private Booking salvarBooking(SlotId slotId, String userId) {
        return bookingAdapter.save(new Booking(
                BookingId.of(UUID.randomUUID()),
                slotId,
                userId,
                BookingStatus.CONFIRMED
        ));
    }
}
