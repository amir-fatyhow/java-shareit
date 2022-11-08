package ru.practicum.shareit.booking.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exception.ShareItNotFoundException;

import javax.xml.bind.ValidationException;
import java.util.List;

@Service
public interface BookingService {

    BookingResponseDto save(BookingDto bookingDto, long userId)
            throws ShareItNotFoundException, ValidationException;

    BookingResponseDto update(long bookingId, long userId, boolean approved)
            throws ShareItNotFoundException, ValidationException;

    BookingResponseDto findById(long bookingId, long userID)
            throws ShareItNotFoundException;

    List<BookingResponseDto> findByState(String state, long userId, Integer from, Integer size)
            throws ShareItNotFoundException;

    List<BookingResponseDto> findOwnerItems(String state, long userId, Integer from, Integer size)
            throws ShareItNotFoundException;

}
