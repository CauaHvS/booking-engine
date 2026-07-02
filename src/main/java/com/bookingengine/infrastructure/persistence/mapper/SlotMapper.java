package com.bookingengine.infrastructure.persistence.mapper;

import com.bookingengine.domain.model.ResourceId;
import com.bookingengine.domain.model.Slot;
import com.bookingengine.domain.model.SlotId;
import com.bookingengine.infrastructure.persistence.entity.SlotJpaEntity;

public class SlotMapper {

    private SlotMapper() {}

    public static Slot toDomain(SlotJpaEntity entity) {
        return new Slot(
                SlotId.of(entity.getId()),
                ResourceId.of(entity.getResourceId()),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getStatus(),
                entity.getVersion()
        );
    }

    public static SlotJpaEntity toEntity(Slot slot) {
        var entity = new SlotJpaEntity();
        entity.setId(slot.getId().value());
        entity.setResourceId(slot.getResourceId().value());
        entity.setStartTime(slot.getStartTime());
        entity.setEndTime(slot.getEndTime());
        entity.setStatus(slot.getStatus());
        // versão nula em slots novos: Hibernate inicializa como 0 no INSERT
        entity.setVersion(slot.getVersion());
        return entity;
    }
}
