package com.company.accounts.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                             HttpServletRequest request) {
        log.warn("Invalid path variable: param={} value={} traceId={}",
            ex.getName(), ex.getValue(), MDC.get("traceId"));
        var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setType(URI.create("https://api.company.com/errors/invalid-customer-id"));
        pd.setTitle("Invalid Customer Identifier");
        pd.setDetail(String.format("customerId must be a valid UUID: '%s'", ex.getValue()));
        pd.setInstance(URI.create(request.getRequestURI()));
        pd.setProperty("traceId", MDC.get("traceId"));
        return pd;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex,
                                                    HttpServletRequest request) {
        log.warn("Constraint violation: traceId={}", MDC.get("traceId"));
        var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setType(URI.create("https://api.company.com/errors/invalid-request-parameter"));
        pd.setTitle("Invalid Request Parameter");
        pd.setDetail(ex.getConstraintViolations().stream()
            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
            .collect(Collectors.joining(", ")));
        pd.setInstance(URI.create(request.getRequestURI()));
        pd.setProperty("traceId", MDC.get("traceId"));
        return pd;
    }

    @ExceptionHandler(AccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail handleNotFound(AccountNotFoundException ex, HttpServletRequest request) {
        log.warn("Account not found: traceId={}", MDC.get("traceId"));
        var pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setType(URI.create("https://api.company.com/errors/customer-accounts-not-found"));
        pd.setTitle("Customer Accounts Not Found");
        pd.setDetail(ex.getMessage());
        pd.setInstance(URI.create(request.getRequestURI()));
        pd.setProperty("traceId", MDC.get("traceId"));
        return pd;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ProblemDetail handleGeneral(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception: traceId={}", MDC.get("traceId"), ex);
        var pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setType(URI.create("https://api.company.com/errors/internal-server-error"));
        pd.setTitle("Internal Server Error");
        pd.setDetail("An unexpected error occurred. Reference: " + MDC.get("traceId"));
        pd.setInstance(URI.create(request.getRequestURI()));
        pd.setProperty("traceId", MDC.get("traceId"));
        return pd;
    }
}