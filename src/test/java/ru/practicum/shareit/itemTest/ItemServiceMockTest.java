package ru.practicum.shareit.itemTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.model.entity.ItemRequest;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceMockTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private RequestRepository requestRepository;
    private ItemServiceImpl itemService;

    @BeforeEach
    void init() {
        ObjectMapper objectMapper = new ObjectMapper();
        itemService = new ItemServiceImpl(
                itemRepository,
                userRepository,
                bookingRepository,
                commentRepository,
                requestRepository,
                objectMapper);
    }

    @Test
    void saveTest() {
        // Assign
        User user = user();
        ItemRequest itemRequest = itemRequest();
        ItemDto itemDto = itemDto();
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(requestRepository.findById(anyLong()))
                .thenReturn(Optional.of(itemRequest));
        // Act
        ItemDto actualDto = itemService.save(itemDto, 1L);

        // Assert
        assertEquals(itemDto.getName(), actualDto.getName());
        assertEquals(itemDto.getDescription(), actualDto.getDescription());

    }

    @Test
    void saveUserNotFoundTest() {
        // Assign
        User user = user();
        ItemDto itemDto = itemDto();
        itemDto.setRequestId(2L);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        // Act
        ShareItNotFoundException exception = assertThrows(ShareItNotFoundException.class, () -> {
            itemService.save(itemDto, user.getId());
        });

        // Assert
        assertEquals("Пользователя с указанным Id не существует.", exception.getMessage());
    }

    @Test
    void saveRequestNullTest() {
        // Assign
        User user = user();
        ItemDto itemDto = itemDto();
        itemDto.setRequestId(null);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));

        // Act
        ItemDto actualDto = itemService.save(itemDto, user.getId());

        // Assert
        assertNotNull(actualDto);
        assertEquals("name", actualDto.getName());
        assertEquals("description", actualDto.getDescription());
        assertEquals(0, actualDto.getRequestId());
    }

    @Test
    void saveRequestNotNullTest() {
        // Assign
        User user = user();
        ItemDto itemDto = itemDto();
        ItemRequest itemRequest = itemRequest();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));

        when(requestRepository.findById(anyLong()))
                .thenReturn(Optional.of(itemRequest));

        // Act
        ItemDto actualDto = itemService.save(itemDto, user.getId());

        // Assert
        assertNotNull(actualDto);
        assertEquals("name", actualDto.getName());
        assertEquals("description", actualDto.getDescription());
        assertEquals(1, actualDto.getRequestId());
    }

    @Test
    void saveRequestRequestNotFoundTest() {
        // Assign
        User user = user();
        ItemDto itemDto = itemDto();
        ItemRequest itemRequest = itemRequest();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));

        when(requestRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        // Act
        ShareItNotFoundException exception = assertThrows(ShareItNotFoundException.class, () -> {
            itemService.save(itemDto, user.getId());
        });

        // Assert
        assertEquals("Запрос вещи с указанным Id не существует.", exception.getMessage());
    }

    @Test
    void updateItemTest() throws Exception {
        // Assign
        User user = user();

        Map<Object, Object> fields = new HashMap<>();
        fields.put("name", "name");
        fields.put("description", "description");
        fields.put("available", true);

        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item()));

        when(itemRepository.save(any(Item.class)))
                .thenReturn(item());

        // Act
        ItemDto actualDto = itemService.update(fields, 1L, user.getId());

        // Assert
        assertEquals(1L, actualDto.getId());
        assertEquals("name", actualDto.getName());
        assertEquals("description", actualDto.getDescription());
        assertEquals(Boolean.TRUE, actualDto.getAvailable());
        assertEquals(1L, actualDto.getRequestId());
    }

    @Test
    void updateItemNotFoundTest() {
        // Assign
        User otherUser = user();
        otherUser.setId(2L);
        Item item = item();
        item.setOwner(otherUser);
        ItemDto itemDto = ItemMapper.toItemDto(item);
        Map<Object, Object> fields = new HashMap<>();

        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        // Act
        ShareItNotFoundException thrown = assertThrows(ShareItNotFoundException.class, () ->
                itemService.update(fields, itemDto.getId(), 3));

        // Assert
        assertEquals("Вещь с указанным Id не существует.", thrown.getMessage());
    }

    @Test
    void updateItemNotOwnerTest() {
        // Assign
        User otherUser = user();
        otherUser.setId(2L);
        Item item = item();
        item.setOwner(otherUser);
        ItemDto itemDto = ItemMapper.toItemDto(item);
        Map<Object, Object> fields = new HashMap<>();

        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));

        // Act
        ShareItNotFoundException thrown = assertThrows(ShareItNotFoundException.class, () ->
            itemService.update(fields, itemDto.getId(), 3));

        // Assert
        assertEquals("Обновлять вещь может только ее владелец.", thrown.getMessage());
    }

    @Test
    void deleteItemTest() {
        // Assign
        Item item = item();

        // Act
        itemService.deleteItem(item.getId());

        // Assert
        verify(itemRepository, times(1)).deleteById(item.getId());
    }

    @Test
    void findItemByIdOwnerTest() {
        // Assign
        Item item1 = item();
        User user = user();
        Booking booking1 = booking(user, item1);
        Comment comment1 = comment(item1, user);
        List<Comment> comments = List.of(comment1);
        ItemResponseDto itemResponseDto = ItemMapper.toItemResponseDto(item1, booking1, null, CommentMapper.toCommentDtos(comments));

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item1));

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));

        when(commentRepository.findAllByItemIdOrderByCreatedDesc(item1.getId()))
                .thenReturn(comments);

        when(bookingRepository.findFirstByItemIdAndStartBeforeOrderByStartDesc(anyLong(), any(LocalDateTime.class)))
                .thenReturn(booking1);

        when(bookingRepository.findFirstByItemIdAndStartAfterOrderByStart(anyLong(), any(LocalDateTime.class)))
                .thenReturn(null);


        // Act
        ItemResponseDto actualItemResponseDto = itemService.findById(item1.getId(), user.getId());

        // Assert
        assertNotNull(actualItemResponseDto);
        assertEquals(itemResponseDto.getId(), actualItemResponseDto.getId());
        assertEquals(itemResponseDto.getName(), actualItemResponseDto.getName());
        assertEquals(itemResponseDto.getDescription(), actualItemResponseDto.getDescription());
        assertEquals(itemResponseDto.getAvailable(), actualItemResponseDto.getAvailable());
        assertEquals(itemResponseDto.getLastBooking().getId(), actualItemResponseDto.getLastBooking().getId());
        Assertions.assertNull(actualItemResponseDto.getNextBooking());

    }

    @Test
    void findItemByIdNotOwnerTest() {
        // Assign
        Item item1 = item();
        User user = user();

        User user1 = user();
        user1.setId(20L);
        item1.setOwner(user1);

        Booking booking1 = booking(user, item1);
        Comment comment1 = comment(item1, user);
        List<Comment> comments = List.of(comment1);
        ItemResponseDto itemResponseDto = ItemMapper.toItemResponseDto(item1, booking1, null, CommentMapper.toCommentDtos(comments));

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item1));

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));

        when(commentRepository.findAllByItemIdOrderByCreatedDesc(item1.getId()))
                .thenReturn(comments);

        // Act
        ItemResponseDto actualItemResponseDto = itemService.findById(item1.getId(), user.getId());

        // Assert
        assertNotNull(actualItemResponseDto);
        assertEquals(itemResponseDto.getId(), actualItemResponseDto.getId());
        assertEquals(itemResponseDto.getName(), actualItemResponseDto.getName());
        assertEquals(itemResponseDto.getDescription(), actualItemResponseDto.getDescription());
        assertEquals(itemResponseDto.getAvailable(), actualItemResponseDto.getAvailable());
        Assertions.assertNull(actualItemResponseDto.getNextBooking());

    }

    @Test
    void findByIdItemNotFoundTest() {
        // Assign
        User user = user();
        ItemDto itemDto = itemDto();
        itemDto.setRequestId(2L);

        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        // Act
        ShareItNotFoundException exception = assertThrows(ShareItNotFoundException.class, () -> {
            itemService.findById(itemDto.getId(), user.getId());
        });

        // Assert
        assertEquals("Вещь с указанным Id не существует.", exception.getMessage());
    }

    @Test
    void findByIdUserNotFoundTest() {
        // Assign
        User user = user();
        Item item = item();
        ItemDto itemDto = itemDto();
        itemDto.setRequestId(2L);

        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        // Act
        ShareItNotFoundException exception = assertThrows(ShareItNotFoundException.class, () -> {
            itemService.findById(itemDto.getId(), user.getId());
        });

        // Assert
        assertEquals("Пользователя с указанным Id не существует.", exception.getMessage());
    }

    @Test
    void findAllItemsByUserNotFoundTest() {
        // Assign
        User user = user();
        ItemDto itemDto = itemDto();
        itemDto.setRequestId(2L);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        // Act
        ShareItNotFoundException exception = assertThrows(ShareItNotFoundException.class, () -> {
            itemService.findAllItemsByUserId(user.getId(), 0, 10);
        });

        // Assert
        assertEquals("Пользователя с указанным Id не существует.", exception.getMessage());
    }

    @Test
    void findAllItemsByUserIdTest() {
        // Assign
        Item item1 = item();
        Item item2 = item();
        List<Item> items = List.of(item1, item2);
        item2.setId(2L);
        User user = user();
        Booking booking1 = booking(user, item1);
        Booking booking2 = booking(user, item2);
        booking2.setId(2L);
        Comment comment1 = comment(item1, user);
        List<Comment> comments = List.of(comment1);
        ItemResponseDto itemResponseDto1 = ItemMapper.toItemResponseDto(item1, booking1, null, CommentMapper.toCommentDtos(comments));
        ItemResponseDto itemResponseDto2 = ItemMapper.toItemResponseDto(item2, booking2, null, new ArrayList<>());
        List<ItemResponseDto> itemsDtos = List.of(itemResponseDto1, itemResponseDto2);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));

        when(itemRepository.findAllByOwnerIdOrderByIdAsc(anyLong(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(items));

        when(commentRepository.findAllByItemIdOrderByCreatedDesc(item1.getId()))
                .thenReturn(comments);

        when(bookingRepository.findFirstByItemIdAndStartBeforeOrderByStartDesc(anyLong(), any(LocalDateTime.class)))
                .thenReturn(booking1);

        when(bookingRepository.findFirstByItemIdAndStartAfterOrderByStart(anyLong(), any(LocalDateTime.class)))
                .thenReturn(null);

        // Act
        List<ItemResponseDto> actualItemDtos = itemService.findAllItemsByUserId(user.getId(), 0, 10);

        // Assert
        assertNotNull(actualItemDtos);
        assertEquals(actualItemDtos.size(), itemsDtos.size());
    }

    @Test
    void searchNotEmptyTextTest() {
        // Assign
        Item item1 = item();
        Item item2 = item();
        item2.setId(2L);
        item2.setName("Search");
        List<Item> items = List.of(item1, item2);

        when(itemRepository.findAllByNameAndDescriptionLowerCase(anyString(), anyString(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(items));

        // Act
        List<ItemDto> actualItems = itemService.search("search", 0, 10);

        // Assert
        assertNotNull(actualItems);
        assertEquals(items.size(), actualItems.size());
    }

    @Test
    void searchEmptyTextTest() {
        // Assign
        Item item2 = item();
        item2.setId(2L);
        item2.setName("Search");

        // Act
        List<ItemDto> actualItems = itemService.search("", 0, 10);

        // Assert
        assertNotNull(actualItems);
        assertEquals(0, actualItems.size());
    }

    @Test
    void postCommentByInvalidUserTest() {
        // Assign
        CommentDto commentDto = new CommentDto(1, "Text", 1, "author", LocalDateTime.now());

        when(bookingRepository.existsBookingByItemIdAndBookerIdAndEndBeforeAndStatusNotLike(anyLong(),
                anyLong(), any(LocalDateTime.class), any(BookingStatus.class)))
                .thenReturn(false);

        // Act
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            itemService.postComment(1L, 1L, commentDto);
        });

        // Assert
        assertEquals("Комментарий может оставлять только пользователь, который бронировал данную вещь.",
                exception.getMessage());
    }

    @Test
    void postCommentByUserNotFoundTest() {
        // Assign
        CommentDto commentDto = new CommentDto(1, "Text", 1, "author", LocalDateTime.now());

        when(bookingRepository.existsBookingByItemIdAndBookerIdAndEndBeforeAndStatusNotLike(anyLong(),
                anyLong(), any(LocalDateTime.class), any(BookingStatus.class)))
                .thenReturn(true);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        // Act
        ShareItNotFoundException exception = assertThrows(ShareItNotFoundException.class, () -> {
            itemService.postComment(1L, 1L, commentDto);
        });

        // Assert
        assertEquals("Пользователя с указанным Id не существует.", exception.getMessage());
    }

    @Test
    void postCommentByItemNotFoundTest() {
        // Assign
        User booker = user();
        CommentDto commentDto = new CommentDto(1, "Text", 1, "author", LocalDateTime.now());

        when(bookingRepository.existsBookingByItemIdAndBookerIdAndEndBeforeAndStatusNotLike(anyLong(),
                anyLong(), any(LocalDateTime.class), any(BookingStatus.class)))
                .thenReturn(true);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));

        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        // Act
        ShareItNotFoundException exception = assertThrows(ShareItNotFoundException.class, () -> {
            itemService.postComment(1L, 1L, commentDto);
        });

        // Assert
        assertEquals("Вещь с указанным Id не существует.", exception.getMessage());
    }

    @Test
    void postCommentByItemTest() {
        // Assign
        User booker = user();
        Item item = item();
        Comment comment = comment(item, booker);
        CommentDto commentDto = new CommentDto(1, "Text", 1, "author", LocalDateTime.now());

        when(bookingRepository.existsBookingByItemIdAndBookerIdAndEndBeforeAndStatusNotLike(anyLong(),
                anyLong(), any(LocalDateTime.class), any(BookingStatus.class)))
                .thenReturn(true);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));

        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));

        when(commentRepository.save(any(Comment.class)))
                .thenReturn(comment);

        // Act
       CommentDto actualCommentDto =  itemService.postComment(1L, 1L, commentDto);

        // Assert
        assertEquals(commentDto.getId(), actualCommentDto.getId());
    }

    @Test
    void getAllCommentsByItemTest() {
        // Assign
        User booker = user();
        Item item = item();
        Comment comment = comment(item, booker);
        List<Comment> comments = List.of(comment);

        when(itemRepository.existsById(anyLong()))
                .thenReturn(true);

        when(commentRepository.findAllByItemIdOrderByCreatedDesc(item.getId()))
                .thenReturn(comments);

        // Act
        List<CommentDto> actualCommentDtos = itemService.getAllCommentsByItem(item.getId());

        // Assert
        assertNotNull(actualCommentDtos);
        assertEquals(comments.size(), actualCommentDtos.size());
    }

    @Test
    void getAllCommentsByItemNotFoundTest() {
        // Assign
        User booker = user();
        Item item = item();
        Comment comment = comment(item, booker);

        when(itemRepository.existsById(anyLong()))
                .thenReturn(false);

        // Act
        ShareItNotFoundException exception = assertThrows(ShareItNotFoundException.class, () -> {
            itemService.getAllCommentsByItem(item.getId());
        });

        // Assert
        assertEquals("Вещь с указанным Id не существует.", exception.getMessage());
    }

    private User user() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@mail");
        user.setName("name");
        return user;
    }

    private ItemDto itemDto() {
        return new ItemDto(1L, "name", "description", Boolean.TRUE, 1L);
    }

    private ItemRequest itemRequest() {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setRequestor(user());
        itemRequest.setId(1L);
        itemRequest.setDescription("Description");
        itemRequest.setCreated(LocalDateTime.now());
        return itemRequest;
    }

    private Item item() {
        Item item = new Item();
        item.setId(1L);
        item.setName("name");
        item.setDescription("description");
        item.setAvailable(Boolean.TRUE);
        item.setOwner(user());
        item.setRequest(itemRequest());
        return item;
    }

    private Comment comment(Item item, User author) {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setText("comment");
        comment.setCreated(LocalDateTime.now());
        return comment;
    }

    private Booking booking(User booker, Item item) {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStart((LocalDateTime.now().minusSeconds(10)));
        booking.setEnd((LocalDateTime.now().plusSeconds(20)));
        booking.setStatus(BookingStatus.WAITING);
        return booking;
    }
}
