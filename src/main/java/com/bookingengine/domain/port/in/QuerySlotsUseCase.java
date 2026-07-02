package com.bookingengine.domain.port.in;

import com.bookingengine.domain.model.ResourceId;

import java.util.List;

public interface QuerySlotsUseCase {

    List<SlotResult> findAvailable(ResourceId resourceId);
}
