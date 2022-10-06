package ru.practicum.shareit.item.service;

import com.fasterxml.jackson.databind.JsonMappingException;
import ru.practicum.shareit.item.dto.ItemDto;
import java.util.List;
import java.util.Map;

public interface ItemService {
    ItemDto save(ItemDto item, long ownerId);

    ItemDto update(Map<Object,Object> fields, long itemId, long userId) throws JsonMappingException;

    ItemDto findById(long itemId, long userId);

    List<ItemDto> findAllByOwnerId(long userId);

    List<ItemDto> search(String text);

    void deleteById(long itemId);

}
