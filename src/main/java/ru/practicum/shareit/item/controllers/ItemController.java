package ru.practicum.shareit.item.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.exceptions.ItemNotFound;
import ru.practicum.shareit.item.services.ItemService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto createItem(@RequestBody ItemDto itemDto,
                              @RequestHeader("X-Sharer-User-Id") long userId) throws NotFoundException {
        return itemService.createItem(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    public ItemDto updateItem(@RequestBody ItemDto itemDto,
                              @PathVariable long itemId,
                              @RequestHeader("X-Sharer-User-Id") long userId) throws ItemNotFound, NotFoundException {
        return itemService.updateItem(itemDto, itemId, userId);
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable long itemId) {
        itemService.deleteItem(itemId);
    }

    @GetMapping("/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    public ItemResponseDto findItemById(@PathVariable long itemId,
                                        @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.findItemById(itemId, userId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ItemResponseDto> findAllItemsByUserId(@RequestHeader("X-Sharer-User-Id") long userId,
                                                      @RequestParam(required = false, defaultValue = "0") @Min(0) Integer from,
                                                      @RequestParam(required = false, defaultValue = "100") @Min(1) Integer size) {
        return itemService.findAllItemsByUserId(userId, from, size);
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<ItemDto> searchItemsByNameAndDescription(@RequestParam String text,
                                                         @RequestParam(required = false, defaultValue = "0") @Min(0) Integer from,
                                                         @RequestParam(required = false, defaultValue = "100") @Min(1) Integer size) {
        return itemService.searchItemsByNameAndDescription(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto postComment(@PathVariable long itemId,
                                  @RequestHeader("X-Sharer-User-Id") long userId,
                                  @RequestBody @Valid CommentDto commentDto) {
        return itemService.postComment(itemId, userId, commentDto);
    }
}
