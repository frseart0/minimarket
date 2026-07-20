package com.minimarket.exception;

import com.minimarket.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Manejo centralizado de excepciones para toda la API: convierte las
 * excepciones de negocio, validación y seguridad en respuestas HTTP
 * consistentes ({@link ErrorResponse}).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleException ex, HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errores = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errores.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ResponseEntity<ErrorResponse> response = construir(HttpStatus.BAD_REQUEST,
                "Error de validación en los datos enviados", request, errores);
        return response;
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return construir(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos", request, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return construir(HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta operación", request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor: " + ex.getMessage(), request, null);
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus status, String message,
                                                      HttpServletRequest request,
                                                      Map<String, String> validationErrors) {
        ErrorResponse body = new ErrorResponse(status.value(), status.getReasonPhrase(), message, request.getRequestURI());
        body.setValidationErrors(validationErrors);
        return ResponseEntity.status(status).body(body);
    }
}
