package ru.practicum.shareit.user.exceptions;

public class UserNotBooker extends RuntimeException {
    public UserNotBooker(String message, long userId) {
        super(message);
    }
}
