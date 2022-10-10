package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;
    private final CommentService commentService;

    @PostMapping
    public ItemDto save(@RequestBody ItemDto itemDto, @RequestHeader("X-Sharer-User-Id") long ownerId) {
        return itemService.save(itemDto, ownerId);
    }

    @GetMapping("/{id}")
    public ItemDto findById(@PathVariable long id, @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.findById(id, userId);
    }

    @GetMapping
    public List<ItemDto> findAllByOwnerId(@RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.findAllByOwnerId(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        return itemService.search(text);
    }

    @PatchMapping("/{id}")
    public ItemDto update(@PathVariable long id, @RequestBody Map<Object,Object> fields,
                              @RequestHeader("X-Sharer-User-Id") long ownerId) throws JsonMappingException {
        return itemService.update(fields, id, ownerId);
    }

    @DeleteMapping("/{itemId}")
    public void deleteById(@PathVariable long itemId) {
        itemService.deleteById(itemId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto postComment(@RequestBody CommentDto commentDto,
                                  @PathVariable long itemId, @RequestHeader("X-Sharer-User-Id") long authorId) {
        return commentService.save(commentDto, authorId, itemId);
    }
}
