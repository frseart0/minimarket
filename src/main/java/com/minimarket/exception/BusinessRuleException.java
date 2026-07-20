package com.minimarket.exception;

/** Se lanza cuando una operación viola una regla de negocio (p. ej. stock insuficiente). */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
