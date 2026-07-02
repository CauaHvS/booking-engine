package com.bookingengine.infrastructure.persistence.repository;

import com.bookingengine.domain.model.SlotStatus;
import com.bookingengine.infrastructure.persistence.entity.SlotJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SlotJpaRepository extends JpaRepository<SlotJpaEntity, UUID> {

    List<SlotJpaEntity> findByResourceIdAndStatus(UUID resourceId, SlotStatus status);
}
