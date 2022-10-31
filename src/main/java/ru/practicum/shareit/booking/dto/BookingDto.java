package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.booking.enums.BookingStatus;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class BookingDto {

    private long id;
    private long itemId;
    private long bookerId;
    @FutureOrPresent
    private LocalDateTime start;
    @Future
    private LocalDateTime end;
    private BookingStatus status;
}
