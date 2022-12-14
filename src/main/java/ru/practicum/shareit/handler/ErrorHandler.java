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
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.user.controller.UserController;
import javax.validation.ValidationException;
import java.util.Map;

/*@RestControllerAdvice
public class ErrorHandler {
    public static final String ERROR = "error";

    //400
    @ExceptionHandler({ValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationException(final ValidationException e) {
        return Map.of(ERROR, "ValidationException");
    }

    //404
    @ExceptionHandler({ShareItNotFoundException.class, ItemNotFound.class})
    public ResponseEntity<String> userNotFoundHandler(final RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    //400
    @ExceptionHandler({ItemNullParametr.class})
    public ResponseEntity<ErrorResponse> itemBadParam(final RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
    }

    //400
    @ExceptionHandler({BadRequestException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse badRequestHandler(final RuntimeException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler({UserNotBooker.class})
    public ResponseEntity<String> wrongBooker(final RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler({BookingWrongTime.class})
    public ResponseEntity<String> wrongTime(final RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}*/

@RestControllerAdvice(assignableTypes = {BookingController.class, UserController.class, ItemController.class,
        ItemRequestController.class})
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