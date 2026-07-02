package com.bookingengine.infrastructure.web.controller;

import com.bookingengine.domain.model.Booking;
import com.bookingengine.domain.model.BookingId;
import com.bookingengine.domain.model.BookingStatus;
import com.bookingengine.domain.model.Resource;
import com.bookingengine.domain.model.ResourceId;
import com.bookingengine.domain.model.Slot;
import com.bookingengine.domain.model.SlotId;
import com.bookingengine.domain.model.SlotStatus;
import com.bookingengine.infrastructure.persistence.repository.BookingJpaRepository;
import com.bookingengine.infrastructure.persistence.repository.BookingRepositoryAdapter;
import com.bookingengine.infrastructure.persistence.repository.ResourceJpaRepository;
import com.bookingengine.infrastructure.persistence.repository.ResourceRepositoryAdapter;
import com.bookingengine.infrastructure.persistence.repository.SlotJpaRepository;
import com.bookingengine.infrastructure.persistence.repository.SlotRepositoryAdapter;
import com.bookingengine.infrastructure.web.dto.BookingResponse;
import com.bookingengine.infrastructure.web.dto.SlotResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class BookingQueryIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ResourceRepositoryAdapter resourceAdapter;

    @Autowired
    private SlotRepositoryAdapter slotAdapter;

    @Autowired
    private BookingRepositoryAdapter bookingAdapter;

    @Autowired
    private BookingJpaRepository bookingJpaRepository;

    @Autowired
    private SlotJpaRepository slotJpaRepository;

    @Autowired
    private ResourceJpaRepository resourceJpaRepository;

    private static final LocalDateTime START = LocalDateTime.of(2026, 11, 1, 9, 0);
    private static final LocalDateTime END   = LocalDateTime.of(2026, 11, 1, 10, 0);

    @AfterEach
    void limpar() {
        bookingJpaRepository.deleteAll();
        slotJpaRepository.deleteAll();
        resourceJpaRepository.deleteAll();
    }

    // --- DELETE /api/bookings/{id} ---

    @Test
    void deveCancelarReservaERetornar204() {
        var slot    = criarSlotReservado();
        var booking = criarBooking(slot.getId(), "user-42");

        var response = restTemplate.exchange(
                RequestEntity.delete("/api/bookings/" + booking.getId().value()).build(),
                Void.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // slot deve ter voltado a AVAILABLE
        var slotAtualizado = slotAdapter.findByIdWithLock(slot.getId());
        assertEquals(SlotStatus.AVAILABLE, slotAtualizado.getStatus());
    }

    @Test
    void deveRetornar404AoCancelarReservaInexistente() {
        var response = restTemplate.exchange(
                RequestEntity.delete("/api/bookings/" + UUID.randomUUID()).build(),
                String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deveRetornar409AoCancelarReservaJaCancelada() {
        var slot    = criarSlotReservado();
        var booking = criarBooking(slot.getId(), "user-42");

        // cancela uma primeira vez
        restTemplate.exchange(
                RequestEntity.delete("/api/bookings/" + booking.getId().value()).build(),
                Void.class);

        // tenta cancelar de novo
        var response = restTemplate.exchange(
                RequestEntity.delete("/api/bookings/" + booking.getId().value()).build(),
                String.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    // --- GET /api/bookings?userId= ---

    @Test
    void deveListarReservasPorUsuario() {
        var slot1 = criarSlotReservado();
        var slot2 = criarSlotReservado();
        criarBooking(slot1.getId(), "user-42");
        criarBooking(slot2.getId(), "user-42");
        criarBooking(slot1.getId(), "user-99");

        var response = restTemplate.getForEntity(
                "/api/bookings?userId=user-42", BookingResponse[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().length);
    }

    @Test
    void deveRetornarListaVaziaParaUsuarioSemReservas() {
        var response = restTemplate.getForEntity(
                "/api/bookings?userId=user-sem-reservas", BookingResponse[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().length);
    }

    // --- GET /api/slots?resourceId= ---

    @Test
    void deveListarSlotsDisponiveisPorResource() {
        var resource = resourceAdapter.save(new Resource(ResourceId.of(UUID.randomUUID()), "Sala IT"));
        criarSlotDisponivel(resource.getId());
        criarSlotDisponivel(resource.getId());
        var slotReservado = criarSlotDisponivel(resource.getId());
        slotReservado.reserve();
        slotAdapter.save(slotReservado);

        var response = restTemplate.getForEntity(
                "/api/slots?resourceId=" + resource.getId().value(), SlotResponse[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().length);
    }

    // --- helpers ---

    private Slot criarSlotDisponivel(ResourceId resourceId) {
        return slotAdapter.save(new Slot(
                SlotId.of(UUID.randomUUID()),
                resourceId,
                START, END,
                SlotStatus.AVAILABLE,
                null
        ));
    }

    private Slot criarSlotReservado() {
        var resource = resourceAdapter.save(new Resource(ResourceId.of(UUID.randomUUID()), "Sala IT"));
        var slot = criarSlotDisponivel(resource.getId());
        slot.reserve();
        return slotAdapter.save(slot);
    }

    private Booking criarBooking(SlotId slotId, String userId) {
        return bookingAdapter.save(new Booking(
                BookingId.of(UUID.randomUUID()),
                slotId,
                userId,
                BookingStatus.CONFIRMED
        ));
    }
}
