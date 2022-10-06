package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.enums.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;


public class BookingRowMapper {
    public static Booking mapToBooking(BookingDto bookingDto, long bookerId) {
        Booking booking = new Booking();
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItemId(bookingDto.getItemId());
        booking.setBookerId(bookerId);
        booking.setStatus(BookingStatus.WAITING);

        return booking;
    }

    public static BookingDto mapToBookingDto(Booking booking, ItemDto itemDto, UserDto userDto) {
        return new BookingDto(
                booking.getId(),
                booking.getItemId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus(),
                itemDto,
                userDto);
    }

    public static BookingLastOrNextDto mapToBookingLastAndNext(Booking booking) {
        return new BookingLastOrNextDto(booking.getId(), booking.getBookerId());
    }

    /*public static List<BookingDto> mapToBookingDto(Iterable<Booking> bookings, ItemServiceImpl itemService, UserServiceImpl userService) {
        List<BookingDto> dtos = new ArrayList<>();
        for (Booking booking : bookings) {
            dtos.add(mapToBookingDto(booking, itemService, userService));
        }
        return dtos;
    }*/
}
