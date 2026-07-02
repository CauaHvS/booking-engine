package com.bookingengine.infrastructure.web.controller;

import com.bookingengine.domain.model.BookingId;
import com.bookingengine.domain.model.SlotId;
import com.bookingengine.domain.port.in.CancelBookingCommand;
import com.bookingengine.domain.port.in.CancelBookingUseCase;
import com.bookingengine.domain.port.in.CreateBookingCommand;
import com.bookingengine.domain.port.in.CreateBookingUseCase;
import com.bookingengine.domain.port.in.QueryBookingsUseCase;
import com.bookingengine.infrastructure.web.dto.BookingResponse;
import com.bookingengine.infrastructure.web.dto.CreateBookingRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final CreateBookingUseCase createBookingUseCase;
    private final CancelBookingUseCase cancelBookingUseCase;
    private final QueryBookingsUseCase queryBookingsUseCase;

    public BookingController(CreateBookingUseCase createBookingUseCase,
                             CancelBookingUseCase cancelBookingUseCase,
                             QueryBookingsUseCase queryBookingsUseCase) {
        this.createBookingUseCase = createBookingUseCase;
        this.cancelBookingUseCase = cancelBookingUseCase;
        this.queryBookingsUseCase = queryBookingsUseCase;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> create(@RequestBody @Valid CreateBookingRequest request) {
        var command = new CreateBookingCommand(SlotId.of(request.slotId()), request.userId());
        var result = createBookingUseCase.create(command);
        var location = URI.create("/api/bookings/" + result.id());
        return ResponseEntity.created(location).body(BookingResponse.from(result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        cancelBookingUseCase.cancel(new CancelBookingCommand(BookingId.of(id), null));
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> findByUser(@RequestParam String userId) {
        var results = queryBookingsUseCase.findByUser(userId).stream()
                .map(BookingResponse::from)
                .toList();
        return ResponseEntity.ok(results);
    }
}
