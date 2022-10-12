package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.booking.dto.BookingLastOrNextDto;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;


public class ItemRowMapper  {

    public static ItemDto toItemDto(Item item, BookingLastOrNextDto last, BookingLastOrNextDto next, ArrayList<CommentDto> comments) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequestId(),
                last,
                next,
                comments
        );
    }

    public static Item toItem(ItemDto itemDto, long ownerId) {
        Item item = new Item();
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setOwnerId(ownerId);
        item.setRequestId(itemDto.getRequestId());
        return item;
    }

}
