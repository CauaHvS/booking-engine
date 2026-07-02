package com.bookingengine.domain.port.out;

import com.bookingengine.domain.model.ResourceId;
import com.bookingengine.domain.model.Slot;
import com.bookingengine.domain.model.SlotId;

import java.util.List;

public interface SlotRepository {

    Slot findByIdWithLock(SlotId id);

    List<Slot> findAvailableByResourceId(ResourceId resourceId);

    Slot save(Slot slot);
}
