package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.enums.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Booking {
    private long id;

    private LocalDate start;

    private LocalDate end;

    private Item item;

    private User user;

    private BookingStatus bookingStatus;
}
