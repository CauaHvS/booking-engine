package com.bookingengine.infrastructure.web.controller;

import com.bookingengine.domain.model.Resource;
import com.bookingengine.domain.model.ResourceId;
import com.bookingengine.domain.model.Slot;
import com.bookingengine.domain.model.SlotId;
import com.bookingengine.domain.model.SlotStatus;
import com.bookingengine.infrastructure.persistence.repository.BookingRepositoryAdapter;
import com.bookingengine.infrastructure.persistence.repository.ResourceRepositoryAdapter;
import com.bookingengine.infrastructure.persistence.repository.SlotRepositoryAdapter;
import com.bookingengine.infrastructure.web.dto.BookingResponse;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class BookingIntegrationTest {

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
    private com.bookingengine.infrastructure.persistence.repository.BookingJpaRepository bookingJpaRepository;

    @Autowired
    private com.bookingengine.infrastructure.persistence.repository.SlotJpaRepository slotJpaRepository;

    @Autowired
    private com.bookingengine.infrastructure.persistence.repository.ResourceJpaRepository resourceJpaRepository;

    private static final LocalDateTime START = LocalDateTime.of(2026, 9, 1, 9, 0);
    private static final LocalDateTime END   = LocalDateTime.of(2026, 9, 1, 10, 0);

    @AfterEach
    void limpar() {
        bookingJpaRepository.deleteAll();
        slotJpaRepository.deleteAll();
        resourceJpaRepository.deleteAll();
    }

    // --- 201 Created ---

    @Test
    void deveCriarReservaERetornar201() {
        var slot = criarSlotDisponivel();
        var body = "{\"slotId\": \"%s\", \"userId\": \"user-42\"}".formatted(slot.getId().value());

        var request = RequestEntity
                .post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);

        var response = restTemplate.exchange(request, BookingResponse.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getHeaders().getLocation());
        assertTrue(response.getHeaders().getLocation().toString().startsWith("/api/bookings/"));

        var booking = response.getBody();
        assertNotNull(booking);
        assertEquals(slot.getId().value(), booking.slotId());
        assertEquals("user-42", booking.userId());
        assertEquals("CONFIRMED", booking.status());
    }

    @Test
    void deveMudarStatusDoSlotParaReservedAposCriarReserva() {
        var slot = criarSlotDisponivel();
        var body = "{\"slotId\": \"%s\", \"userId\": \"user-42\"}".formatted(slot.getId().value());

        var request = RequestEntity
                .post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);

        restTemplate.exchange(request, BookingResponse.class);

        var slotAtualizado = slotAdapter.findByIdWithLock(slot.getId());
        assertEquals(SlotStatus.RESERVED, slotAtualizado.getStatus());
    }

    // --- 409 Conflict ---

    @Test
    void deveRetornar409QuandoSlotJaEstaReservado() {
        var slot = criarSlotReservado();
        var body = "{\"slotId\": \"%s\", \"userId\": \"user-42\"}".formatted(slot.getId().value());

        var request = RequestEntity
                .post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);

        var response = restTemplate.exchange(request, String.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getHeaders().getContentType().toString()
                .startsWith("application/problem+json"));
    }

    // --- 404 Not Found ---

    @Test
    void deveRetornar404QuandoSlotNaoExiste() {
        var body = "{\"slotId\": \"%s\", \"userId\": \"user-42\"}".formatted(UUID.randomUUID());

        var request = RequestEntity
                .post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);

        var response = restTemplate.exchange(request, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getHeaders().getContentType().toString()
                .startsWith("application/problem+json"));
    }

    // --- helpers ---

    private Slot criarSlotDisponivel() {
        var resource = resourceAdapter.save(new Resource(ResourceId.of(UUID.randomUUID()), "Sala IT"));
        return slotAdapter.save(new Slot(
                SlotId.of(UUID.randomUUID()),
                resource.getId(),
                START, END,
                SlotStatus.AVAILABLE,
                null
        ));
    }

    private Slot criarSlotReservado() {
        var slot = criarSlotDisponivel();
        slot.reserve();
        return slotAdapter.save(slot);
    }
}
