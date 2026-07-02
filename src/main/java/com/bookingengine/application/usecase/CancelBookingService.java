package com.bookingengine.application.usecase;

import com.bookingengine.domain.port.in.CancelBookingCommand;
import com.bookingengine.domain.port.in.CancelBookingUseCase;
import com.bookingengine.domain.port.out.BookingRepository;
import com.bookingengine.domain.port.out.SlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CancelBookingService implements CancelBookingUseCase {

    private final BookingRepository bookingRepository;
    private final SlotRepository slotRepository;

    public CancelBookingService(BookingRepository bookingRepository, SlotRepository slotRepository) {
        this.bookingRepository = bookingRepository;
        this.slotRepository = slotRepository;
    }

    @Override
    public void cancel(CancelBookingCommand command) {
        throw new UnsupportedOperationException("não implementado");
    }
}
