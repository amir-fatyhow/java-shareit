package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.enums.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;


public class BookingRowMapper {
    public static Booking toBooking(BookingDto bookingDto, long bookerId) {
        Booking booking = new Booking();
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItemId(bookingDto.getItemId());
        booking.setBookerId(bookerId);
        booking.setStatus(BookingStatus.WAITING);

        return booking;
    }
    public static BookingDto toBookingDto(Booking booking, ItemDto itemDto, UserDto userDto) {
        return new BookingDto(
                booking.getId(),
                booking.getItemId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus(),
                itemDto,
                userDto);
    }
    public static BookingLastOrNextDto toBookingLastAndNext(Booking booking) {
        return new BookingLastOrNextDto(booking.getId(), booking.getBookerId());
    }

}
