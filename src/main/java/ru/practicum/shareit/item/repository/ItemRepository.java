package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Map;

public interface ItemRepository {
    ItemDto createItem(Item item, long userId);

    ItemDto updateItem(Map<Object,Object> fields, long itemId, long userId);

    List<ItemDto> getAllItemsByUserId(long userId);

    ItemDto getItemById(long itemId);

    List<ItemDto> searchItems(String text);

    void deleteItemById(long itemId);
}
