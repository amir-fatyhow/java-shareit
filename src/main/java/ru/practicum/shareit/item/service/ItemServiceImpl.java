package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;

import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;

    @Override
    public ItemDto createItem(Item item, long userId) {
        return itemRepository.createItem(item, userId);
    }

    @Override
    public ItemDto updateItem(Map<Object, Object> fields, long itemId, long userId) {
        return itemRepository.updateItem(fields, itemId, userId);
    }

    @Override
    public ItemDto getItemById(long itemId) {
        return itemRepository.getItemById(itemId);
    }

    @Override
    public List<ItemDto> getAllItemsByUserId(long userId) {
        return itemRepository.getAllItemsByUserId(userId);
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        return itemRepository.searchItems(text);
    }

    @Override
    public void deleteItemById(long itemId) {
        itemRepository.deleteItemById(itemId);
    }

}
