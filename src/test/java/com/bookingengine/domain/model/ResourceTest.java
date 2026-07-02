package com.bookingengine.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ResourceTest {

    @Test
    void deveCriarRecursoComDadosValidos() {
        var id = ResourceId.of(UUID.randomUUID());

        var resource = new Resource(id, "Sala de Reuniões A");

        assertEquals(id, resource.getId());
        assertEquals("Sala de Reuniões A", resource.getName());
    }

    @Test
    void deveLancarExcecaoQuandoIdForNulo() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> new Resource(null, "Sala A"));

        assertEquals("id do recurso não pode ser nulo", ex.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoNomeForNulo() {
        var id = ResourceId.of(UUID.randomUUID());

        var ex = assertThrows(IllegalArgumentException.class,
                () -> new Resource(id, null));

        assertEquals("nome do recurso não pode ser vazio", ex.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoNomeForVazio() {
        var id = ResourceId.of(UUID.randomUUID());

        var ex = assertThrows(IllegalArgumentException.class,
                () -> new Resource(id, ""));

        assertEquals("nome do recurso não pode ser vazio", ex.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoNomeForSomenteEspacos() {
        var id = ResourceId.of(UUID.randomUUID());

        var ex = assertThrows(IllegalArgumentException.class,
                () -> new Resource(id, "   "));

        assertEquals("nome do recurso não pode ser vazio", ex.getMessage());
    }
}
