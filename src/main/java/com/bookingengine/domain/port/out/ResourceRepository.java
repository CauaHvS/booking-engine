package com.bookingengine.domain.port.out;

import com.bookingengine.domain.model.Resource;
import com.bookingengine.domain.model.ResourceId;

import java.util.Optional;

public interface ResourceRepository {

    Resource save(Resource resource);

    Optional<Resource> findById(ResourceId id);
}
