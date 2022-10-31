package ru.practicum.shareit.item.services;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.exceptions.ItemNotFound;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.exceptions.UserNotBooker;

import java.util.List;

@Service
public interface ItemService {

    ItemDto createItem(ItemDto itemDto, long userId) throws NotFoundException;

    ItemDto updateItem(ItemDto itemDto, long itemId, long userId) throws ItemNotFound, NotFoundException;

    void deleteItem(long itemId) throws ItemNotFound;

    List<ItemDto> findAll();

    ItemResponseDto findItemById(long itemId, long userId) throws ItemNotFound;

    List<ItemResponseDto> findAllItemsByUserId(long userId, Integer from, Integer size);

    List<ItemDto> searchItemsByNameAndDescription(String text, Integer from, Integer size);

    Item checkItem(long itemId);

    CommentDto postComment(long itemId, long userId, CommentDto text)
            throws NotFoundException, ItemNotFound, UserNotBooker;

    List<CommentDto> getAllCommentsByItem(long itemId);
}
