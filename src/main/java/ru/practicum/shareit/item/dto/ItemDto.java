package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingLastOrNextDto;
import ru.practicum.shareit.item.comment.CommentDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ItemDto {
    private long id;

    @NotBlank
    @NotNull
    private String name;

    @NotBlank
    @NotNull
    private String description;

    @NotNull
    private Boolean available;

    private Long requestId;

    private BookingLastOrNextDto lastBooking;

    private BookingLastOrNextDto nextBooking;

    private ArrayList<CommentDto> comments;
}
