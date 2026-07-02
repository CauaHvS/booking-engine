package com.bookingengine.domain.model;

public class Resource {

    private final ResourceId id;
    private final String name;

    public Resource(ResourceId id, String name) {
        if (id == null) {
            throw new IllegalArgumentException("id do recurso não pode ser nulo");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("nome do recurso não pode ser vazio");
        }
        this.id = id;
        this.name = name;
    }

    public ResourceId getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
