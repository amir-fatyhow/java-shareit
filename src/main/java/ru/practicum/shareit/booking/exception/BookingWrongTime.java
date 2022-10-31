package ru.practicum.shareit.booking.exception;

public class BookingWrongTime extends RuntimeException {
    public BookingWrongTime(String message) {
        super(message);
    }
}
