package com.bookingengine.infrastructure.persistence.repository;

import com.bookingengine.domain.exception.SlotNotFoundException;
import com.bookingengine.domain.model.ResourceId;
import com.bookingengine.domain.model.Slot;
import com.bookingengine.domain.model.SlotId;
import com.bookingengine.domain.model.SlotStatus;
import com.bookingengine.domain.port.out.SlotRepository;
import com.bookingengine.infrastructure.persistence.mapper.SlotMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SlotRepositoryAdapter implements SlotRepository {

    private final SlotJpaRepository jpaRepository;

    public SlotRepositoryAdapter(SlotJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Slot findByIdWithLock(SlotId id) {
        // locking otimista: a verificação de versão ocorre no commit via @Version
        return jpaRepository.findById(id.value())
                .map(SlotMapper::toDomain)
                .orElseThrow(() -> new SlotNotFoundException(
                        "slot não encontrado: " + id.value()));
    }

    @Override
    public List<Slot> findAvailableByResourceId(ResourceId resourceId) {
        return jpaRepository
                .findByResourceIdAndStatus(resourceId.value(), SlotStatus.AVAILABLE)
                .stream()
                .map(SlotMapper::toDomain)
                .toList();
    }

    @Override
    public Slot save(Slot slot) {
        var saved = jpaRepository.save(SlotMapper.toEntity(slot));
        return SlotMapper.toDomain(saved);
    }
}
