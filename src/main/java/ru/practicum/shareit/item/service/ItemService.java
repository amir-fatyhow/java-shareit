package ru.practicum.shareit.item.service;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import java.util.List;
import java.util.Map;

@Service
public interface ItemService {
    ItemDto save(ItemDto itemDto, long userId);

    ItemDto update(Map<Object,Object> fields, long itemId, long userId) throws JsonMappingException;

    void deleteItem(long itemId);

    ItemResponseDto findById(long itemId, long userId);

    List<ItemResponseDto> findAllItemsByUserId(long userId, Integer from, Integer size);

    List<ItemDto> search(String text, Integer from, Integer size);

    CommentDto postComment(long itemId, long userId, CommentDto text);

    List<CommentDto> getAllCommentsByItem(long itemId);
}
