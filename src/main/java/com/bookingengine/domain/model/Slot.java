package com.bookingengine.domain.model;

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
        this.id = id;
        this.resourceId = resourceId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.version = version;
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
