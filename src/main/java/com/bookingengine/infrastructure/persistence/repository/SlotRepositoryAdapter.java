package com.bookingengine.infrastructure.persistence.repository;

import com.bookingengine.domain.model.ResourceId;
import com.bookingengine.domain.model.Slot;
import com.bookingengine.domain.model.SlotId;
import com.bookingengine.domain.port.out.SlotRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SlotRepositoryAdapter implements SlotRepository {

    @Override
    public Slot findByIdWithLock(SlotId id) {
        throw new UnsupportedOperationException("não implementado");
    }

    @Override
    public List<Slot> findAvailableByResourceId(ResourceId resourceId) {
        throw new UnsupportedOperationException("não implementado");
    }

    @Override
    public Slot save(Slot slot) {
        throw new UnsupportedOperationException("não implementado");
    }
}
