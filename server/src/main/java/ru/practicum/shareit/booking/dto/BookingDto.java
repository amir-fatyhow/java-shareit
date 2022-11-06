package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.booking.enums.BookingStatus;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class BookingDto {

    private long id;
    private long itemId;
    private long bookerId;
    private LocalDateTime start;
    private LocalDateTime end;
    private BookingStatus status;
}
