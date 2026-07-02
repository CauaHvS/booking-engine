package com.bookingengine.infrastructure.persistence.mapper;

import com.bookingengine.domain.model.Resource;
import com.bookingengine.domain.model.ResourceId;
import com.bookingengine.infrastructure.persistence.entity.ResourceJpaEntity;

public class ResourceMapper {

    private ResourceMapper() {}

    public static Resource toDomain(ResourceJpaEntity entity) {
        return new Resource(
                ResourceId.of(entity.getId()),
                entity.getName()
        );
    }

    public static ResourceJpaEntity toEntity(Resource resource) {
        var entity = new ResourceJpaEntity();
        entity.setId(resource.getId().value());
        entity.setName(resource.getName());
        return entity;
    }
}
