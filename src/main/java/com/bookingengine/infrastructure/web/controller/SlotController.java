package com.bookingengine.infrastructure.web.controller;

import com.bookingengine.domain.port.in.QuerySlotsUseCase;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/slots")
public class SlotController {

    private final QuerySlotsUseCase querySlotsUseCase;

    public SlotController(QuerySlotsUseCase querySlotsUseCase) {
        this.querySlotsUseCase = querySlotsUseCase;
    }
}
