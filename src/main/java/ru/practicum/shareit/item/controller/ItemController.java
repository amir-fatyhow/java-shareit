package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.services.ItemService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ItemDto save(@RequestBody @Valid ItemDto itemDto, @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.save(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestBody Map<Object,Object> fields,
                              @PathVariable long itemId,
                              @RequestHeader("X-Sharer-User-Id") long userId) throws JsonMappingException {
        return itemService.update(fields, itemId, userId);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@PathVariable long itemId) {
        itemService.deleteItem(itemId);
    }

    @GetMapping("/{itemId}")
    public ItemResponseDto findById(@PathVariable long itemId,
                                        @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.findById(itemId, userId);
    }

    @GetMapping
    public List<ItemResponseDto> findAllItemsByUserId(@RequestHeader("X-Sharer-User-Id") long userId,
                                  @PositiveOrZero @RequestParam(required = false, defaultValue = "0") Integer from,
                                  @Positive @RequestParam(required = false, defaultValue = "100") Integer size) {
        return itemService.findAllItemsByUserId(userId, from, size);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text,
                                @PositiveOrZero @RequestParam(required = false, defaultValue = "0") Integer from,
                                @Positive @RequestParam(required = false, defaultValue = "100") Integer size) {
        return itemService.search(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto postComment(@PathVariable long itemId,
                                  @RequestHeader("X-Sharer-User-Id") long userId,
                                  @RequestBody @Valid CommentDto commentDto) {
        return itemService.postComment(itemId, userId, commentDto);
    }
}
