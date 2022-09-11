package ru.practicum.shareit.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.exception.NotFound;

@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler({NotFound.class})
    public ResponseEntity<String> runtimeHandler(final RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
    }
}
