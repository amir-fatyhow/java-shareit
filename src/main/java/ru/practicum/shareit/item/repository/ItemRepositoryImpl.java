package ru.practicum.shareit.item.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.ReflectionUtils;
import ru.practicum.shareit.exception.NotFound;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemRowMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.repository.UserRepositoryImpl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepository {
    private final List<Item> items = new ArrayList<>();

    private final UserRepositoryImpl userRepository;

    private static long id = 0;

    @Override
    public ItemDto createItem(Item item, long userId) {
        if (userRepository.getUsers().stream().noneMatch(user -> user.getId() == userId)) {
            throw new NotFound("Пользователя с указанным Id не существует.");
        }

        item.setId(++id);
        item.setOwnerId(userId);
        items.add(item);
        return ItemRowMapper.toItemDto(item);
    }

    @Override
    public ItemDto updateItem(Map<Object, Object> fields, long itemId, long userId) {
        if (items.stream().noneMatch(item -> item.getOwnerId() == userId)) {
            throw new NotFound("Пользователя с указанным Id не существует.");
        }

        Item targetItem = items.stream().filter(item -> item.getId() == itemId).findAny()
                .orElseThrow(() -> new IllegalArgumentException("Неверно указан Id вещи."));

        fields.forEach((key, value) -> {
            Field field = ReflectionUtils.findField(Item.class, (String) key);
            assert field != null;
            field.setAccessible(true);
            ReflectionUtils.setField(field, targetItem, value);
        });
        return ItemRowMapper.toItemDto(targetItem);
    }

    @Override
    public ItemDto getItemById(long itemId) {
        return items.stream().filter(item -> item.getId() == itemId).findFirst().map(ItemRowMapper::toItemDto).get();
    }

    @Override
    public List<ItemDto> getAllItemsByUserId(long userId) {
        return items.stream().filter(item -> item.getOwnerId() == userId)
                .map(ItemRowMapper::toItemDto).collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        return text.isBlank() ? new ArrayList<>() : items.stream()
                .filter(item -> (item.getName().toLowerCase().contains(text.toLowerCase()) ||
                item.getDescription().toLowerCase().contains(text.toLowerCase())) && item.getAvailable().equals(true))
                .map(ItemRowMapper::toItemDto).collect(Collectors.toList());
    }

    @Override
    public void deleteItemById(long itemId) {
        items.removeIf(item -> item.getId() == itemId);
    }
}
