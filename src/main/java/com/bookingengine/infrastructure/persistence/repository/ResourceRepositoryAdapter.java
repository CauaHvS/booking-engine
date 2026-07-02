package com.bookingengine.infrastructure.persistence.repository;

import com.bookingengine.domain.model.Resource;
import com.bookingengine.domain.model.ResourceId;
import com.bookingengine.domain.port.out.ResourceRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ResourceRepositoryAdapter implements ResourceRepository {

    @Override
    public Optional<Resource> findById(ResourceId id) {
        throw new UnsupportedOperationException("não implementado");
    }
}
