package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Map;

public interface ItemService {
    ItemDto createItem(Item item, long userId);

    ItemDto updateItem(Map<Object,Object> fields, long itemId, long userId);

    List<ItemDto> getAllItemsByUserId(long userId);

    void deleteItemById(long itemId);

    ItemDto getItemById(long itemId);

    List<ItemDto> searchItems(String text);
}
