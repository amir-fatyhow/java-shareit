package ru.practicum.shareit.booking.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.exception.BookingWrongTime;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.exceptions.ItemNotFound;
import ru.practicum.shareit.user.exceptions.UserNotBooker;

import java.util.List;

@Service
public interface BookingService {

    BookingResponseDto createBooking(BookingDto bookingDto, long userId)
            throws NotFoundException, ItemNotFound, BookingWrongTime;

    BookingResponseDto ownerDecision(long bookingId, long userId, boolean approved)
            throws NotFoundException;

    BookingResponseDto getBooking(long bookingId, long userID)
            throws NotFoundException, UserNotBooker;

    List<BookingResponseDto> getAllBookingsByBooker(String state, long userId, Integer from, Integer size)
            throws NotFoundException;

    List<BookingResponseDto> getAllBookingsByOwner(String state, long userId, Integer from, Integer size)
            throws NotFoundException;

    Booking getLastBookingByItem(long itemId);

    Booking getNextBookingByItem(long itemId);

    Booking checkBooking(long bookingId) throws NotFoundException;

}
