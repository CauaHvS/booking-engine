package com.bookingengine.infrastructure.persistence.repository;

import com.bookingengine.infrastructure.persistence.entity.ResourceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ResourceJpaRepository extends JpaRepository<ResourceJpaEntity, UUID> {}
