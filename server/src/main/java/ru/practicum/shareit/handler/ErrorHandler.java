package ru.practicum.shareit.handler;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.exception.ShareItNotFoundException;
import ru.practicum.shareit.exception.UnsupportedStatus;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.request.controller.RequestController;
import ru.practicum.shareit.user.controller.UserController;

import javax.xml.bind.ValidationException;
import java.util.Map;

@RestControllerAdvice(assignableTypes = {BookingController.class, UserController.class, ItemController.class,
        RequestController.class})
public class ErrorHandler {
    public static final String ERROR = "error";

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleShareItNotFoundException(final ShareItNotFoundException e) {
        return Map.of(ERROR, "ShareItNotFoundException");
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationException(final ValidationException e) {
        return Map.of(ERROR, "ValidationException");
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleDataIntegrityViolationException(final DataIntegrityViolationException e) {
        return Map.of(ERROR, "DataIntegrityViolationException");
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleUnsupportedStatus(final UnsupportedStatus e) {
        return Map.of(ERROR, "Unknown state: UNSUPPORTED_STATUS");
    }
}