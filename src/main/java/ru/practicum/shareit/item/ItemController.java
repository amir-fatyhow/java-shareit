package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    /**
     * Добавляем новый Item
     */
    @PostMapping
    public ItemDto createItem(@RequestBody @Valid Item item, @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.createItem(item, userId);
    }

    /**
     * Получаем Item по id
     */
    @GetMapping("/{id}")
    public ItemDto getItemById(@PathVariable long id) {
        return itemService.getItemById(id);
    }

    /**
     * Получаем все Item у User
     */
    @GetMapping()
    public List<ItemDto> getAllItemsByUserId(@RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.getAllItemsByUserId(userId);
    }

    /**
     * Поиск доступных Item
     */
    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        return itemService.searchItems(text);
    }

    /**
     * Обновляем Item по id
     */
    @PatchMapping("/{id}")
    public ItemDto updateItem(@PathVariable long id, @RequestBody Map<Object,Object> fields,
                              @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.updateItem(fields, id, userId);
    }

    /**
     * Удаляем Item по id
     */
    @DeleteMapping("/{itemId}")
    public void deleteItemById(@PathVariable long itemId) {
        itemService.deleteItemById(itemId);
    }

}
