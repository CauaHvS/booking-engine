package com.bookingengine.application.usecase;

import com.bookingengine.domain.model.Booking;
import com.bookingengine.domain.model.BookingId;
import com.bookingengine.domain.model.BookingStatus;
import com.bookingengine.domain.port.in.BookingResult;
import com.bookingengine.domain.port.in.CreateBookingCommand;
import com.bookingengine.domain.port.in.CreateBookingUseCase;
import com.bookingengine.domain.port.out.BookingRepository;
import com.bookingengine.domain.port.out.SlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CreateBookingService implements CreateBookingUseCase {

    private final SlotRepository slotRepository;
    private final BookingRepository bookingRepository;

    public CreateBookingService(SlotRepository slotRepository, BookingRepository bookingRepository) {
        this.slotRepository = slotRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public BookingResult create(CreateBookingCommand command) {
        var slot = slotRepository.findByIdWithLock(command.slotId());

        slot.reserve();
        slotRepository.save(slot);

        var booking = new Booking(
                BookingId.of(UUID.randomUUID()),
                command.slotId(),
                command.userId(),
                BookingStatus.CONFIRMED
        );
        var saved = bookingRepository.save(booking);

        return toResult(saved);
    }

    private BookingResult toResult(Booking booking) {
        return new BookingResult(
                booking.getId().value(),
                booking.getSlotId().value(),
                booking.getUserId(),
                booking.getStatus().name()
        );
    }
}
