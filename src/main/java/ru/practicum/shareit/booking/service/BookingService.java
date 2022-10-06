package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import java.util.List;

public interface BookingService {
    BookingDto save(BookingDto bookingDto, long bookerId);

    BookingDto update(long bookingId, long ownerId, boolean approved);

    BookingDto findById(long bookingId, long userId);

    List<BookingDto> findByState(long userId, String state);

    List<BookingDto> findOwnerItems(long ownerId, String state);
}
