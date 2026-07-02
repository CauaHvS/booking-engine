package com.bookingengine.application.usecase;

import com.bookingengine.domain.exception.BookingNotFoundException;
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
        var booking = bookingRepository.findById(command.bookingId())
                .orElseThrow(() -> new BookingNotFoundException(
                        "reserva %s não encontrada".formatted(command.bookingId().value())));

        booking.cancel(); // lança BookingAlreadyCancelledException se já cancelada

        var slot = slotRepository.findByIdWithLock(booking.getSlotId());
        slot.release();   // libera o slot de volta para AVAILABLE

        slotRepository.save(slot);
        bookingRepository.save(booking);
    }
}
