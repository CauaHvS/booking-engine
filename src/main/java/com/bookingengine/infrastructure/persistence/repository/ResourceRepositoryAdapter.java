package com.bookingengine.infrastructure.persistence.repository;

import com.bookingengine.domain.model.Resource;
import com.bookingengine.domain.model.ResourceId;
import com.bookingengine.domain.port.out.ResourceRepository;
import com.bookingengine.infrastructure.persistence.mapper.ResourceMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ResourceRepositoryAdapter implements ResourceRepository {

    private final ResourceJpaRepository jpaRepository;

    public ResourceRepositoryAdapter(ResourceJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Resource save(Resource resource) {
        var saved = jpaRepository.save(ResourceMapper.toEntity(resource));
        return ResourceMapper.toDomain(saved);
    }

    @Override
    public Optional<Resource> findById(ResourceId id) {
        return jpaRepository.findById(id.value()).map(ResourceMapper::toDomain);
    }
}
