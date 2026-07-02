package com.bookingengine.application.usecase;

import com.bookingengine.domain.model.ResourceId;
import com.bookingengine.domain.port.in.QuerySlotsUseCase;
import com.bookingengine.domain.port.in.SlotResult;
import com.bookingengine.domain.port.out.SlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class QuerySlotsService implements QuerySlotsUseCase {

    private final SlotRepository slotRepository;

    public QuerySlotsService(SlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    @Override
    public List<SlotResult> findAvailable(ResourceId resourceId) {
        return slotRepository.findAvailableByResourceId(resourceId).stream()
                .map(s -> new SlotResult(
                        s.getId().value(),
                        s.getResourceId().value(),
                        s.getStartTime(),
                        s.getEndTime(),
                        s.getStatus().name()))
                .toList();
    }
}
