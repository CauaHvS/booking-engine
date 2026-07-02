package com.bookingengine.domain.model;

import com.bookingengine.domain.exception.SlotNotAvailableException;

import java.time.LocalDateTime;

public class Slot {

    private final SlotId id;
    private final ResourceId resourceId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private SlotStatus status;
    private Long version;

    public Slot(SlotId id, ResourceId resourceId, LocalDateTime startTime,
                LocalDateTime endTime, SlotStatus status, Long version) {
        if (id == null) {
            throw new IllegalArgumentException("id do slot não pode ser nulo");
        }
        if (resourceId == null) {
            throw new IllegalArgumentException("resourceId do slot não pode ser nulo");
        }
        if (startTime == null) {
            throw new IllegalArgumentException("horário de início não pode ser nulo");
        }
        if (endTime == null) {
            throw new IllegalArgumentException("horário de término não pode ser nulo");
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("horário de término deve ser posterior ao de início");
        }
        if (status == null) {
            throw new IllegalArgumentException("status do slot não pode ser nulo");
        }
        this.id = id;
        this.resourceId = resourceId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.version = version;
    }

    public void reserve() {
        if (status != SlotStatus.AVAILABLE) {
            throw new SlotNotAvailableException(
                    "slot %s não está disponível para reserva (status atual: %s)"
                            .formatted(id.value(), status));
        }
        this.status = SlotStatus.RESERVED;
    }

    public boolean isAvailable() {
        return status == SlotStatus.AVAILABLE;
    }

    public SlotId getId() { return id; }
    public ResourceId getResourceId() { return resourceId; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public SlotStatus getStatus() { return status; }
    public Long getVersion() { return version; }
}
