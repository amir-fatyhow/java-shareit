package ru.practicum.shareit.item.service;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ShareItNotFoundException;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final RequestRepository requestRepository;
    private final ObjectMapper objectMapper;
    private static final String USER_NOT_FOUND = "Пользователя с указанным Id не существует.";
    private static final String ITEM_NOT_FOUND = "Вещь с указанным Id не существует.";
    private static final String REQUEST_NOT_FOUND = "Запрос вещи с указанным Id не существует.";

    @Override
    public ItemDto save(ItemDto itemDto, long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new ShareItNotFoundException(USER_NOT_FOUND));
        Item item;
        if (itemDto.getRequestId() == null) {
            item = ItemMapper.toItem(itemDto, owner, null);
        } else {
            item = ItemMapper.toItem(itemDto, owner,requestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new ShareItNotFoundException(REQUEST_NOT_FOUND)));
        }

        itemRepository.save(item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto update(Map<Object,Object> fields, long itemId, long userId) throws JsonMappingException {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ShareItNotFoundException(ITEM_NOT_FOUND));

        if (item.getOwner().getId() != userId) {
            throw new ShareItNotFoundException("Обновлять вещь может только ее владелец.");
        }

        Item updateItem = itemRepository.save(objectMapper.updateValue(item, fields));
        return ItemMapper.toItemDto(updateItem);
    }

    @Override
    public void deleteItem(long itemId) {
        itemRepository.deleteById(itemId);
    }

    @Override
    public ItemResponseDto findById(long itemId, long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ShareItNotFoundException(ITEM_NOT_FOUND));

        User user  = userRepository.findById(userId)
                .orElseThrow(() -> new ShareItNotFoundException(USER_NOT_FOUND));

        List<CommentDto> comments = CommentMapper
                .toCommentDtos(commentRepository.findAllByItemIdOrderByCreatedDesc(itemId));
        if (user.equals(item.getOwner())) {
            Booking lastBooking = bookingRepository.findFirstByItemIdAndStartBeforeOrderByStartDesc(itemId, LocalDateTime.now());
            Booking nextBooking = bookingRepository.findFirstByItemIdAndStartAfterOrderByStart(itemId, LocalDateTime.now());
            return ItemMapper.toItemResponseDto(item, lastBooking, nextBooking, comments);
        }
        return ItemMapper.toItemResponseDto(item, null, null, comments);
    }

    @Override
    public List<ItemResponseDto> findAllItemsByUserId(long userId, Integer from, Integer size) {
        List<ItemResponseDto> itemResponseDtos = new ArrayList<>();
        User owner  = userRepository.findById(userId)
                .orElseThrow(() -> new ShareItNotFoundException(USER_NOT_FOUND));

        Page<Item> items = itemRepository.findAllByOwnerIdOrderByIdAsc(owner.getId(), PageRequest.of(from, size));
        for (Item item : items) {
            List<CommentDto> comments = CommentMapper
                    .toCommentDtos(commentRepository.findAllByItemIdOrderByCreatedDesc(item.getId()));
            itemResponseDtos.add(ItemMapper.toItemResponseDto(item,
                    bookingRepository.findFirstByItemIdAndStartBeforeOrderByStartDesc(item.getId(), LocalDateTime.now()),
                    bookingRepository.findFirstByItemIdAndStartAfterOrderByStart(item.getId(), LocalDateTime.now()),
                    comments));
        }
        return itemResponseDtos;
    }

    @Override
    public List<ItemDto> search(String text, Integer from, Integer size) {
        if (!text.isEmpty()) {
            return itemRepository.findAllByNameAndDescriptionLowerCase(text, text, PageRequest.of(from, size)).stream()
                    .map(ItemMapper::toItemDto).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public CommentDto postComment(long itemId, long userId, CommentDto commentDto) {
        if (!bookingRepository.existsBookingByItemIdAndBookerIdAndEndBeforeAndStatusNotLike(itemId, userId, LocalDateTime.now(),
                BookingStatus.REJECTED)) {
            throw new ValidationException("Комментарий может оставлять только пользователь, который бронировал данную вещь.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ShareItNotFoundException(USER_NOT_FOUND));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ShareItNotFoundException(ITEM_NOT_FOUND));

        Comment comment = new Comment();
        comment.setAuthor(user);
        comment.setItem(item);
        comment.setText(commentDto.getText());

        comment = commentRepository.save(comment);
        return CommentMapper.toCommentDto(comment);
    }

    @Override
    public List<CommentDto> getAllCommentsByItem(long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new ShareItNotFoundException(ITEM_NOT_FOUND);
        }
        List<Comment> comments = commentRepository.findAllByItemIdOrderByCreatedDesc(itemId);
        return comments.stream().map(CommentMapper::toCommentDto).collect(Collectors.toList());
    }

}
