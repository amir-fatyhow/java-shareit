package ru.practicum.shareit.exception;

//404
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
