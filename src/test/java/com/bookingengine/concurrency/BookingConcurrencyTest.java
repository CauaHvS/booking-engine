package com.bookingengine.concurrency;

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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class BookingConcurrencyTest {

    private static final int THREADS = 10;

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

    @AfterEach
    void limpar() {
        bookingJpaRepository.deleteAll();
        slotJpaRepository.deleteAll();
        resourceJpaRepository.deleteAll();
    }

    @Test
    void apenasUmaReservaPorSlotSobConcorrencia() throws InterruptedException {
        // Prepara: 1 slot disponível para N threads disputarem
        var resource = resourceAdapter.save(new Resource(ResourceId.of(UUID.randomUUID()), "Sala Concorrência"));
        var slot = slotAdapter.save(new Slot(
                SlotId.of(UUID.randomUUID()),
                resource.getId(),
                LocalDateTime.of(2026, 10, 1, 9, 0),
                LocalDateTime.of(2026, 10, 1, 10, 0),
                SlotStatus.AVAILABLE,
                null
        ));

        var slotId = slot.getId().value().toString();
        var body = "{\"slotId\": \"%s\", \"userId\": \"user-%s\"}";

        var startLatch  = new CountDownLatch(1);   // dispara todas as threads ao mesmo tempo
        var finishLatch = new CountDownLatch(THREADS);
        var created     = new AtomicInteger(0);
        var conflicts   = new AtomicInteger(0);

        var executor = Executors.newFixedThreadPool(THREADS);

        for (int i = 0; i < THREADS; i++) {
            final var userId = "user-" + i;
            executor.submit(() -> {
                try {
                    startLatch.await(); // aguarda o sinal de largada
                    var request = RequestEntity
                            .post("/api/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(body.formatted(slotId, userId));

                    var status = restTemplate.exchange(request, String.class).getStatusCode();

                    if (status == HttpStatus.CREATED)        created.incrementAndGet();
                    else if (status == HttpStatus.CONFLICT)  conflicts.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown();          // larga todas as threads simultaneamente
        finishLatch.await();             // aguarda todas terminarem
        executor.shutdown();

        // Exatamente 1 criou a reserva; todas as outras receberam 409
        assertEquals(1, created.get(),   "Exatamente 1 reserva deve ser criada");
        assertEquals(THREADS - 1, conflicts.get(), "Todas as outras devem receber 409");

        // Slot deve estar RESERVED no banco
        var slotFinal = slotAdapter.findByIdWithLock(slot.getId());
        assertEquals(SlotStatus.RESERVED, slotFinal.getStatus());

        // Exatamente 1 booking no banco
        assertEquals(1, bookingJpaRepository.count());
    }
}
