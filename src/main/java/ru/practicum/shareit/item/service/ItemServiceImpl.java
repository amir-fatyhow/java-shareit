package ru.practicum.shareit.item.service;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingLastOrNextDto;
import ru.practicum.shareit.booking.dto.BookingRowMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repositories.BookingStorage;
import ru.practicum.shareit.exception.ShareItNotFoundException;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentStorage;
import ru.practicum.shareit.item.comment.CommentRowMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemRowMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repositories.ItemStorage;
import ru.practicum.shareit.user.repositories.UserStorage;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;
    private final BookingStorage bookingStorage;
    private final CommentStorage commentStorage;
    private final ObjectMapper objectMapper;

    @Override
    public ItemDto save(ItemDto itemDto, long ownerId) {
        if (!userStorage.existsById(ownerId)) {
            throw new ShareItNotFoundException("Пользователя с указанным Id не существует.");
        }
        Item item = itemStorage.save(ItemRowMapper.toItem(itemDto, ownerId));

        return ItemRowMapper.toItemDto(item, new BookingLastOrNextDto(), new BookingLastOrNextDto(), new ArrayList<>());
    }

    @Override
    public ItemDto update(Map<Object, Object> fields, long itemId, long userId) throws JsonMappingException {
        Item targetItem = itemStorage.findById(itemId).orElseThrow(() -> new ShareItNotFoundException("Вещь с указанным Id не существует."));

        if (targetItem.getOwnerId() != userId) {
            throw new ShareItNotFoundException("Обновлять вещь может только ее владелец.");
        }

        Item updateItem = objectMapper.updateValue(targetItem, fields);
        itemStorage.save(updateItem);

        Optional<Booking> bookingLast = bookingStorage.findFirstByItemIdAndStartBeforeOrderByStartDesc(itemId, LocalDateTime.now());
        Optional<Booking> bookingNext = bookingStorage.findFirstByItemIdAndStartAfterOrderByStart(itemId, LocalDateTime.now());


        if (bookingLast.isPresent() && bookingNext.isPresent()) {
            return ItemRowMapper.toItemDto(updateItem,
                    BookingRowMapper.toBookingLastAndNext(bookingLast.get()),
                    BookingRowMapper.toBookingLastAndNext(bookingNext.get()),
                    getCommentDtos(itemId));
        }
        return ItemRowMapper.toItemDto(updateItem, null, null, getCommentDtos(itemId));
    }

    @Override
    public ItemDto findById(long itemId, long userId) {
        Item targetItem = itemStorage.findById(itemId).orElseThrow(() -> new ShareItNotFoundException("Вещь с указанным Id не существует."));

        Optional<Booking> bookingLast = bookingStorage.findFirstByItemIdAndStartBeforeOrderByStartDesc(itemId, LocalDateTime.now());
        Optional<Booking> bookingNext = bookingStorage.findFirstByItemIdAndStartAfterOrderByStart(itemId, LocalDateTime.now());

        if (bookingLast.isPresent() && bookingNext.isPresent()
                && bookingNext.get().getBookerId() != userId && bookingLast.get().getBookerId() != userId) {
            return ItemRowMapper.toItemDto(targetItem,
                    BookingRowMapper.toBookingLastAndNext(bookingLast.get()),
                    BookingRowMapper.toBookingLastAndNext(bookingNext.get()),
                    getCommentDtos(itemId));
        }
        return ItemRowMapper.toItemDto(targetItem, null, null, getCommentDtos(itemId));
    }

    @Override
    public List<ItemDto> findAllByOwnerId(long ownerId) {
        List<Item> items = itemStorage.findAllByOwnerId(ownerId);
        List<ItemDto> itemDtos = new ArrayList<>();
        for (Item item : items) {
            Optional<Booking> bookingLast = bookingStorage.findFirstByItemIdAndStartBeforeOrderByStartDesc(item.getId(), LocalDateTime.now());
            Optional<Booking> bookingNext = bookingStorage.findFirstByItemIdAndStartAfterOrderByStart(item.getId(), LocalDateTime.now());

            ItemDto temp;
            if (bookingLast.isPresent() && bookingNext.isPresent()) {
                temp = ItemRowMapper.toItemDto(item,
                        BookingRowMapper.toBookingLastAndNext(bookingLast.get()),
                        BookingRowMapper.toBookingLastAndNext(bookingNext.get()),
                        getCommentDtos(item.getId()));
            } else {
                temp = ItemRowMapper.toItemDto(item, null, null, getCommentDtos(item.getId()));
            }
            itemDtos.add(temp);
        }

        return itemDtos.stream().sorted(Comparator.comparing(ItemDto::getId)).collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text.isBlank()) {
            return  new ArrayList<>();
        }
        List<ItemDto> itemDtos = new ArrayList<>();
        List<Item> items =  itemStorage.search(text, text);
        for (Item item : items) {
            Optional<Booking> bookingLast = bookingStorage.findFirstByItemIdAndStartBeforeOrderByStartDesc(item.getId(), LocalDateTime.now());
            Optional<Booking> bookingNext = bookingStorage.findFirstByItemIdAndStartAfterOrderByStart(item.getId(), LocalDateTime.now());

            ItemDto temp;
            if (bookingLast.isPresent() && bookingNext.isPresent()) {
                temp = ItemRowMapper.toItemDto(item,
                        BookingRowMapper.toBookingLastAndNext(bookingLast.get()),
                        BookingRowMapper.toBookingLastAndNext(bookingNext.get()),
                        getCommentDtos(item.getId()));
            } else {
                temp = ItemRowMapper.toItemDto(item, null, null, getCommentDtos(item.getId()));
            }
            itemDtos.add(temp);
        }
        return itemDtos;
    }

    @Override
    public void deleteById(long itemId) {
        itemStorage.deleteById(itemId);
    }

    private ArrayList<CommentDto> getCommentDtos(long itemId) {
        ArrayList<CommentDto> commentDtos = new ArrayList<>();
        List<Comment> comments = commentStorage.findAllByItem(itemId);
        for (Comment comment : comments) {
            commentDtos.add(CommentRowMapper.mapToCommentDto(comment, commentStorage.authorName(comment.getAuthor()).get()));
        }
        return commentDtos;
    }
}
