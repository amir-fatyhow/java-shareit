package ru.practicum.shareit.exception;

//404
public class ShareItNotFoundException extends RuntimeException {

    public ShareItNotFoundException(String message) {
        super(message);
    }
}
