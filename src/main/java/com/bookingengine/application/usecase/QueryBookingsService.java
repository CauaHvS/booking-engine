package com.bookingengine.application.usecase;

import com.bookingengine.domain.port.in.BookingResult;
import com.bookingengine.domain.port.in.QueryBookingsUseCase;
import com.bookingengine.domain.port.out.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class QueryBookingsService implements QueryBookingsUseCase {

    private final BookingRepository bookingRepository;

    public QueryBookingsService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public List<BookingResult> findByUser(String userId) {
        throw new UnsupportedOperationException("não implementado");
    }
}
