package com.bookingengine.application.usecase;

import com.bookingengine.domain.port.in.BookingResult;
import com.bookingengine.domain.port.in.CreateBookingCommand;
import com.bookingengine.domain.port.in.CreateBookingUseCase;
import com.bookingengine.domain.port.out.BookingRepository;
import com.bookingengine.domain.port.out.SlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        throw new UnsupportedOperationException("não implementado");
    }
}
