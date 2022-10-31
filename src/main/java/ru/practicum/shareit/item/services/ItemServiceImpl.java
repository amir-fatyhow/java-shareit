package ru.practicum.shareit.item.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repositories.BookingStorage;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.comment.CommentStorage;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.exceptions.ItemNotFound;
import ru.practicum.shareit.item.exceptions.ItemNullParametr;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repositories.ItemStorage;
import ru.practicum.shareit.request.model.entity.ItemRequest;
import ru.practicum.shareit.request.repositories.RequestStorage;
import ru.practicum.shareit.user.exceptions.UserNotBooker;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.services.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Создает конструктор из тех полей которые нужны
public class ItemServiceImpl implements ItemService {

    private final ItemStorage itemStorage; // Если стоит final для неинициализированного поля то конструктор нужен обязательно
    private final UserService userService;
    private final BookingStorage bookingStorage;
    private final CommentStorage commentStorage;

    private final RequestStorage requestStorage;


    @Override
    public ItemDto createItem(ItemDto itemDto, long userId) throws BadRequestException, NotFoundException {
        User owner = userService.checkUser(userId);
        Item item;
        if (itemDto.getRequestId() == null) {
            item = ItemMapper.toItem(itemDto, owner, null);
        } else {
            Optional<ItemRequest> optionalItemRequest = requestStorage.findById(itemDto.getRequestId());
            if (optionalItemRequest.isPresent()) {
                item = ItemMapper.toItem(itemDto, owner, optionalItemRequest.get());
            } else {
                throw new NotFoundException(String.format("ItemRequest with ID: %s not found", itemDto.getRequestId()));
            }
        }
        if (item.getAvailable() == null) {
            throw new BadRequestException(String.format("Available not exist - %s", item.getAvailable()));
        }
        if (item.getName() == null || item.getName().isEmpty()) {
            throw new BadRequestException(String.format("Name not exist - %s", item.getName()));
        }
        if (item.getDescription() == null || item.getDescription().isEmpty()) {
            throw new BadRequestException(String.format("Description not exist - %s", item.getDescription()));
        }
        itemStorage.save(item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, long itemId, long userId) throws NotFoundException {
        Item item = checkItem(itemId);
        User owner = userService.checkUser(userId);
        if (item.getOwner().equals(owner)) {
            if (itemDto.getAvailable() != null) {
                item.setAvailable(itemDto.getAvailable());
            }
            if (itemDto.getName() != null) {
                item.setName(itemDto.getName());
            }
            if (itemDto.getDescription() != null) {
                item.setDescription(itemDto.getDescription());
            }
            itemStorage.save(item);
            return ItemMapper.toItemDto(item);
        } else {
            throw new NotFoundException(String.format("User by ID: %s - is not Owner of this Item", userId));
        }
    }

    @Override
    public void deleteItem(long itemId) {
        Item item = checkItem(itemId);
        itemStorage.delete(item);
    }

    @Override
    public List<ItemDto> findAll() {
        return itemStorage.findAll().stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemResponseDto findItemById(long itemId,
                                        long userId) {
        Item item = checkItem(itemId);
        User user = userService.checkUser(userId);
        List<CommentDto> comments = CommentMapper
                .toCommentDtos(commentStorage.getCommentsByItem_idOrderByCreatedDesc(itemId));
        if (user.equals(item.getOwner())) {
            Booking lastBooking = bookingStorage.findFirstByItem_idAndEndBeforeOrderByEndDesc(itemId, LocalDateTime.now());
            Booking nextBooking = bookingStorage.findFirstByItem_idAndStartAfterOrderByStartDesc(itemId, LocalDateTime.now());
            return ItemMapper.toItemResponseDto(item, lastBooking, nextBooking, comments);
        }
        return ItemMapper.toItemResponseDto(item, null, null, comments);
    }

    @Override
    public List<ItemResponseDto> findAllItemsByUserId(long userId, Integer from, Integer size) {
        List<ItemResponseDto> itemResponseDtos = new ArrayList<>();
        User owner = userService.checkUser(userId);
        Page<Item> items = itemStorage.findAllByOwnerIdOrderByIdAsc(owner.getId(), PageRequest.of(from, size));
        for (Item item : items) {
            List<CommentDto> comments = CommentMapper
                    .toCommentDtos(commentStorage.getCommentsByItem_idOrderByCreatedDesc(item.getId()));
            itemResponseDtos.add(ItemMapper.toItemResponseDto(item,
                    bookingStorage.findFirstByItem_idAndEndBeforeOrderByEndDesc(item.getId(), LocalDateTime.now()),
                    bookingStorage.findFirstByItem_idAndStartAfterOrderByStartDesc(item.getId(), LocalDateTime.now()),
                    comments));
        }
        return itemResponseDtos;
    }

    @Override
    public List<ItemDto> searchItemsByNameAndDescription(String text, Integer from, Integer size) {
        if (!text.isEmpty()) {
            text = text.toLowerCase();
            return itemStorage.findAllByNameAndDescriptionLowerCase(text, text, PageRequest.of(from, size)).stream()
                    .map(ItemMapper::toItemDto).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public Item checkItem(long itemId) throws ItemNotFound {
        Optional<Item> optionalItem = itemStorage.findById(itemId);
        if (optionalItem.isPresent()) {
            return optionalItem.get();
        } else {
            throw new ItemNotFound("Item by ID: %s  - not found", itemId);
        }
    }

    public CommentDto postComment(long itemId, long userId, CommentDto commentDto)
            throws UserNotBooker, ItemNullParametr {
        if (commentDto.getText().isEmpty()) {
            throw new BadRequestException("Comment is empty text");
        }
        User user = userService.checkUser(userId);
        Item item = checkItem(itemId);
        List<Booking> bookings = bookingStorage.findByItem_IdAndBooker_IdOrderByStartDesc(itemId, userId);
        if (!bookings.isEmpty()) {
            if (bookings.stream().anyMatch(booking ->
                    (!BookingStatus.REJECTED.equals(booking.getStatus())
                            && !BookingStatus.WAITING.equals(booking.getStatus())) &&
                            !booking.getStart().isAfter(LocalDateTime.now()))) {
                Comment comment = new Comment();
                comment.setAuthor(user);
                comment.setItem(item);
                comment.setText(commentDto.getText());
                comment = commentStorage.save(comment);
                return CommentMapper.toCommentDto(comment);
            }
            throw new BadRequestException("Booking bad status");
        } else {
            throw new UserNotBooker("This User not Booker for this Item", userId);
        }
    }

    @Override
    public List<CommentDto> getAllCommentsByItem(long itemId) {
        Item item = checkItem(itemId);
        List<Comment> comments = commentStorage.getCommentsByItem_idOrderByCreatedDesc(itemId);
        return comments.stream().map(CommentMapper::toCommentDto).collect(Collectors.toList());
    }

    public static Boolean checkPaging(Integer from, Integer size) throws BadRequestException {
        if (from == null && size == null) {
            return false;
        }
        if (size == 0) {
            throw new BadRequestException("size == 0");
        }
        return true;
    }
}
