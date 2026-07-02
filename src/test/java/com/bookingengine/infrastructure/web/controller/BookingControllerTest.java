package com.bookingengine.infrastructure.web.controller;

import com.bookingengine.domain.exception.SlotNotAvailableException;
import com.bookingengine.domain.exception.SlotNotFoundException;
import com.bookingengine.domain.port.in.BookingResult;
import com.bookingengine.domain.port.in.CancelBookingUseCase;
import com.bookingengine.domain.port.in.CreateBookingUseCase;
import com.bookingengine.domain.port.in.QueryBookingsUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateBookingUseCase createBookingUseCase;

    @MockitoBean
    private CancelBookingUseCase cancelBookingUseCase;

    @MockitoBean
    private QueryBookingsUseCase querySlotsUseCase;

    // --- 201 Created ---

    @Test
    void deveRetornar201ComLocationAoCriarReserva() throws Exception {
        var slotId    = UUID.randomUUID();
        var bookingId = UUID.randomUUID();
        when(createBookingUseCase.create(any()))
                .thenReturn(new BookingResult(bookingId, slotId, "user-42", "CONFIRMED"));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"slotId": "%s", "userId": "user-42"}
                                """.formatted(slotId)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/bookings/" + bookingId))
                .andExpect(jsonPath("$.id").value(bookingId.toString()))
                .andExpect(jsonPath("$.slotId").value(slotId.toString()))
                .andExpect(jsonPath("$.userId").value("user-42"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    // --- 422 validação ---

    @Test
    void deveRetornar422QuandoSlotIdForNulo() throws Exception {
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": \"user-42\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.code").value("validation_error"))
                .andExpect(jsonPath("$.errors[0].field").value("slotId"));
    }

    @Test
    void deveRetornar422QuandoUserIdForVazio() throws Exception {
        var body = "{\"slotId\": \"%s\", \"userId\": \"\"}".formatted(UUID.randomUUID());
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.code").value("validation_error"))
                .andExpect(jsonPath("$.errors[0].field").value("userId"));
    }

    // --- 409 Conflict ---

    @Test
    void deveRetornar409QuandoSlotJaReservado() throws Exception {
        when(createBookingUseCase.create(any()))
                .thenThrow(new SlotNotAvailableException("slot já reservado"));

        var body = "{\"slotId\": \"%s\", \"userId\": \"user-42\"}".formatted(UUID.randomUUID());
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.code").value("slot_not_available"))
                .andExpect(jsonPath("$.status").value(409));
    }

    // --- 404 Not Found ---

    @Test
    void deveRetornar404QuandoSlotNaoExistir() throws Exception {
        when(createBookingUseCase.create(any()))
                .thenThrow(new SlotNotFoundException("slot não encontrado"));

        var body404 = "{\"slotId\": \"%s\", \"userId\": \"user-42\"}".formatted(UUID.randomUUID());
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body404))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.code").value("slot_not_found"))
                .andExpect(jsonPath("$.status").value(404));
    }
}
