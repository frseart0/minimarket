package com.minimarket.exception;

/** Se lanza cuando un recurso solicitado (por id u otro criterio) no existe. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException forEntity(String entidad, Long id) {
        return new ResourceNotFoundException(entidad + " con id " + id + " no fue encontrado(a)");
    }
}
