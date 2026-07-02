package com.bookingengine.infrastructure.web.controller;

import com.bookingengine.domain.model.ResourceId;
import com.bookingengine.domain.port.in.QuerySlotsUseCase;
import com.bookingengine.infrastructure.web.dto.SlotResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Slots", description = "Consulta de slots disponíveis por recurso")
@RestController
@RequestMapping("/api/slots")
public class SlotController {

    private final QuerySlotsUseCase querySlotsUseCase;

    public SlotController(QuerySlotsUseCase querySlotsUseCase) {
        this.querySlotsUseCase = querySlotsUseCase;
    }

    @Operation(summary = "Listar slots disponíveis", description = "Retorna todos os slots com status AVAILABLE para o recurso informado.")
    @GetMapping
    public ResponseEntity<List<SlotResponse>> findAvailable(@RequestParam UUID resourceId) {
        var results = querySlotsUseCase.findAvailable(ResourceId.of(resourceId)).stream()
                .map(SlotResponse::from)
                .toList();
        return ResponseEntity.ok(results);
    }
}
