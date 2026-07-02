package com.bookingengine.infrastructure.config;

import com.bookingengine.domain.exception.BookingAlreadyCancelledException;
import com.bookingengine.domain.exception.BookingNotFoundException;
import com.bookingengine.domain.exception.ResourceNotFoundException;
import com.bookingengine.domain.exception.SlotNotAvailableException;
import com.bookingengine.domain.exception.SlotNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- 409 Conflict ---

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ProblemDetail> handleOptimisticLocking(
            ObjectOptimisticLockingFailureException ex, HttpServletRequest request) {
        return conflict("slot_conflict", "Conflito de reserva",
                "O slot foi modificado por outra transação. Tente novamente.", request);
    }

    @ExceptionHandler(SlotNotAvailableException.class)
    public ResponseEntity<ProblemDetail> handleSlotNotAvailable(
            SlotNotAvailableException ex, HttpServletRequest request) {
        return conflict("slot_not_available", "Slot não disponível", ex.getMessage(), request);
    }

    @ExceptionHandler(BookingAlreadyCancelledException.class)
    public ResponseEntity<ProblemDetail> handleBookingAlreadyCancelled(
            BookingAlreadyCancelledException ex, HttpServletRequest request) {
        return conflict("booking_already_cancelled", "Reserva já cancelada", ex.getMessage(), request);
    }

    // --- 404 Not Found ---

    @ExceptionHandler(SlotNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleSlotNotFound(
            SlotNotFoundException ex, HttpServletRequest request) {
        return notFound("slot_not_found", "Slot não encontrado", ex.getMessage(), request);
    }

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleBookingNotFound(
            BookingNotFoundException ex, HttpServletRequest request) {
        return notFound("booking_not_found", "Reserva não encontrada", ex.getMessage(), request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        return notFound("resource_not_found", "Recurso não encontrado", ex.getMessage(), request);
    }

    // --- 422 Unprocessable Entity (validação de campos) ---

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        var problem = problem(HttpStatus.UNPROCESSABLE_ENTITY,
                "validation_error",
                "Campos inválidos",
                "A requisição contém campos inválidos.",
                request);

        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of(
                        "field",   fe.getField(),
                        "code",    Objects.requireNonNullElse(fe.getCode(), "invalid").toLowerCase(),
                        "message", Objects.requireNonNullElse(fe.getDefaultMessage(), "inválido")))
                .toList();
        problem.setProperty("errors", errors);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    // --- helpers ---

    private ResponseEntity<ProblemDetail> conflict(
            String code, String title, String detail, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem(HttpStatus.CONFLICT, code, title, detail, request));
    }

    private ResponseEntity<ProblemDetail> notFound(
            String code, String title, String detail, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem(HttpStatus.NOT_FOUND, code, title, detail, request));
    }

    private ProblemDetail problem(
            HttpStatus status, String code, String title, String detail, HttpServletRequest request) {
        var pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setInstance(URI.create(request.getRequestURI()));
        pd.setProperty("code", code);
        return pd;
    }
}
