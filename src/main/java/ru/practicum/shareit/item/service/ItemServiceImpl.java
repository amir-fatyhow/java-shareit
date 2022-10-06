package ru.practicum.shareit.item.service;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingLastOrNextDto;
import ru.practicum.shareit.booking.dto.BookingRowMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.comment.CommentRowMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemRowMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;

    private final UserRepository userRepository;

    private final BookingRepository bookingRepository;

    private final CommentRepository commentRepository;

    private final ObjectMapper objectMapper;

    @Override
    public ItemDto save(ItemDto itemDto, long ownerId) {
        if (!userRepository.existsById(ownerId)) {
            throw new NullPointerException("Пользователя с указанным Id не существует.");
        }
        Item item = itemRepository.save(ItemRowMapper.toItem(itemDto, ownerId));

        return ItemRowMapper.toItemDto(item, new BookingLastOrNextDto(), new BookingLastOrNextDto(), new ArrayList<>());
    }

    @Override
    public ItemDto update(Map<Object, Object> fields, long itemId, long userId) throws JsonMappingException {
        if (!itemRepository.existsById(itemId)) {
            throw new NullPointerException("Вещи с указанным Id не существует.");
        }

        Optional<Item> targetItem = Optional.of(itemRepository.findById(itemId).orElseThrow(NullPointerException::new));

        if (targetItem.get().getOwnerId() != userId) {
            throw new NullPointerException("Обновлять вещь может только ее владелец.");
        }

        Item updateItem = objectMapper.updateValue(targetItem.get(), fields);
        itemRepository.save(updateItem);

        Optional<Booking> bookingLast = bookingRepository.findFirstByItemIdAndStartBeforeOrderByStartDesc(itemId, LocalDateTime.now());
        Optional<Booking> bookingNext = bookingRepository.findFirstByItemIdAndStartAfterOrderByStart(itemId, LocalDateTime.now());


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
        Optional<Item> targetItem = Optional.of(itemRepository.findById(itemId).orElseThrow(NullPointerException::new));

        Optional<Booking> bookingLast = bookingRepository.findFirstByItemIdAndStartBeforeOrderByStartDesc(itemId, LocalDateTime.now());
        Optional<Booking> bookingNext = bookingRepository.findFirstByItemIdAndStartAfterOrderByStart(itemId, LocalDateTime.now());

        if (bookingLast.isPresent() && bookingNext.isPresent()
                && bookingNext.get().getBookerId() != userId && bookingLast.get().getBookerId() != userId) {
            return ItemRowMapper.toItemDto(targetItem.get(),
                    BookingRowMapper.toBookingLastAndNext(bookingLast.get()),
                    BookingRowMapper.toBookingLastAndNext(bookingNext.get()),
                    getCommentDtos(itemId));
        }
        return ItemRowMapper.toItemDto(targetItem.get(), null, null, getCommentDtos(itemId));
    }

    @Override
    public List<ItemDto> findAllByOwnerId(long ownerId) {
        List<Item> items = itemRepository.findAllByOwnerId(ownerId);
        List<ItemDto> itemDtos = new ArrayList<>();
        for (Item item : items) {
            Optional<Booking> bookingLast = bookingRepository.findFirstByItemIdAndStartBeforeOrderByStartDesc(item.getId(), LocalDateTime.now());
            Optional<Booking> bookingNext = bookingRepository.findFirstByItemIdAndStartAfterOrderByStart(item.getId(), LocalDateTime.now());

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
        } else {
            List<ItemDto> itemDtos = new ArrayList<>();
            List<Item> items =  itemRepository.search(text);
            for (Item item : items) {
                Optional<Booking> bookingLast = bookingRepository.findFirstByItemIdAndStartBeforeOrderByStartDesc(item.getId(), LocalDateTime.now());
                Optional<Booking> bookingNext = bookingRepository.findFirstByItemIdAndStartAfterOrderByStart(item.getId(), LocalDateTime.now());

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
    }

    @Override
    public void deleteById(long itemId) {
        itemRepository.deleteById(itemId);
    }

    private ArrayList<CommentDto> getCommentDtos(long itemId) {
        ArrayList<CommentDto> commentDtos = new ArrayList<>();
        List<Comment> comments = commentRepository.findAllByItem(itemId);
        for (Comment comment : comments) {
            commentDtos.add(CommentRowMapper.mapToCommentDto(comment, commentRepository.authorName(comment.getAuthor()).get()));
        }
        return commentDtos;
    }
}
