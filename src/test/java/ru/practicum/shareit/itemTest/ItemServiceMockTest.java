package ru.practicum.shareit.itemTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repositories.ItemStorage;
import ru.practicum.shareit.item.services.ItemServiceImpl;
import ru.practicum.shareit.request.model.entity.ItemRequest;
import ru.practicum.shareit.request.repositories.RequestStorage;
import ru.practicum.shareit.user.exceptions.UserNotBooker;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.services.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceMockTest {

    // Мокаем все сервисы и репозитории, которые не надо тестировать
    @Mock
    private ItemStorage itemStorage;

    @Mock
    private UserService userService;

    @Mock
    private BookingStorage bookingStorage;

    @Mock
    private CommentStorage commentStorage;

    @Mock
    private RequestStorage requestStorage;

    private ItemServiceImpl itemService;

    @BeforeEach
    void init() {
        // Инициализируем сервис бинами заглушками
        itemService = new ItemServiceImpl(
                itemStorage,
                userService,
                bookingStorage,
                commentStorage,
                requestStorage);
    }

    @Test
    public void createItemTest() throws Exception {
        // Assign
        Mockito.when(userService.checkUser(anyLong())) // Когда вызовется метод checkUser() с любым long...
                .thenReturn(getTestUser()); // ... вернуть тестовое значение
        Mockito.when(requestStorage.findById(anyLong())) // Когда вызовется метод findById() с любым long...
                .thenReturn(getTestOptionalItemRequest()); // ... вернуть тестовое значение
        // Act
        ItemDto actualDto = itemService.createItem(getTestItemDto(), 1L); // Вызываем тестируемый метод с тестовыми данными

        // Assert
        Assertions.assertEquals(actualDto.getName(), "TestName"); // Сравниваем актуальные значения с предполагаемыми
        Assertions.assertEquals(actualDto.getDescription(), "Description");
        // Строки в сравнениях либо вынести в константы, либо брать из тестовой DTO. Написал их здесь для наглядности
    }

    @Test
    public void createItemErrorNullRequestTest() {
        // Assign
        User user = getTestUser();
        ItemDto itemDto = getTestItemDto();
        itemDto.setRequestId(2L);

        Mockito.when(userService.checkUser(anyLong()))
                .thenReturn(user);
        Mockito.when(requestStorage.findById(anyLong()))
                .thenReturn(Optional.empty());

        // Act
        NotFoundException thrown = Assertions.assertThrows(NotFoundException.class, () -> {
            ItemDto actualDto = itemService.createItem(itemDto, user.getId());
        });

        // Assert
        assertEquals(thrown.getMessage(), "ItemRequest with ID: 2 not found");
    }

    @Test
    public void createItemErrorNotAvailableTest() {
        // Assign
        User user = getTestUser();
        ItemDto itemDto = getTestItemDto();
        itemDto.setAvailable(null);
        Optional<ItemRequest> itemRequest = getTestOptionalItemRequest();

        Mockito.when(userService.checkUser(anyLong()))
                .thenReturn(user);
        Mockito.when(requestStorage.findById(anyLong()))
                .thenReturn(itemRequest);

        // Act
        BadRequestException thrown = Assertions.assertThrows(BadRequestException.class, () -> {
            ItemDto actualDto = itemService.createItem(itemDto, user.getId());
        });

        // Assert
        assertEquals(thrown.getMessage(), "Available not exist - null");
    }

    @Test
    public void createItemErrorNameNullTest() {
        // Assign
        User user = getTestUser();
        ItemDto itemDto = getTestItemDto();
        itemDto.setName(null);
        Optional<ItemRequest> itemRequest = getTestOptionalItemRequest();

        Mockito.when(userService.checkUser(anyLong()))
                .thenReturn(user);
        Mockito.when(requestStorage.findById(anyLong()))
                .thenReturn(itemRequest);

        // Act
        BadRequestException thrown = Assertions.assertThrows(BadRequestException.class, () -> {
            ItemDto actualDto = itemService.createItem(itemDto, user.getId());
        });

        // Assert
        assertEquals(thrown.getMessage(), "Name not exist - null");
    }

    @Test
    public void createItemErrorDescriptionNullTest() {
        // Assign
        User user = getTestUser();
        ItemDto itemDto = getTestItemDto();
        itemDto.setDescription(null);
        Optional<ItemRequest> itemRequest = getTestOptionalItemRequest();

        Mockito.when(userService.checkUser(anyLong()))
                .thenReturn(user);
        Mockito.when(requestStorage.findById(anyLong()))
                .thenReturn(itemRequest);

        // Act
        BadRequestException thrown = Assertions.assertThrows(BadRequestException.class, () -> {
            ItemDto actualDto = itemService.createItem(itemDto, user.getId());
        });

        // Assert
        assertEquals(thrown.getMessage(), "Description not exist - null");
    }

    @Test
    public void createItemErrorItemRequestNotFoundTest() {
        // Assign
        User user = getTestUser();
        ItemDto itemDto = getTestItemDto();
        itemDto.setRequestId(null);

        Mockito.when(userService.checkUser(anyLong()))
                .thenReturn(user);

        // Act
        ItemDto actualDto = itemService.createItem(itemDto, user.getId());

        // Assert
        Assertions.assertNotNull(actualDto);
        Assertions.assertEquals(actualDto.getName(), "TestName");
        Assertions.assertEquals(actualDto.getDescription(), "Description");
        Assertions.assertEquals(actualDto.getRequestId(), 0);
    }

    @Test
    public void updateItemTest() throws Exception {
        // Assign
        User testUser = getTestUser();

        Mockito.when(userService.checkUser(anyLong())).thenReturn(testUser);
        Mockito.when(itemStorage.findById(anyLong())).thenReturn(getTestOptionalItem());

        // Act
        ItemDto actualDto = itemService.updateItem(getTestItemDto(), 1L, testUser.getId());

        // Assert
        Assertions.assertEquals(actualDto.getId(), 1L);
        Assertions.assertEquals(actualDto.getName(), "TestName");
        Assertions.assertEquals(actualDto.getDescription(), "Description");
        Assertions.assertEquals(actualDto.getAvailable(), Boolean.TRUE);
        Assertions.assertEquals(actualDto.getRequestId(), 1L);
    }

    @Test
    public void updateItemErrorTest() {
        // Assign
        User owner = getTestUser();
        User otherUser = getTestUser();
        otherUser.setId(2L);
        Item item = getTestOptionalItem().get();
        item.setOwner(otherUser);
        ItemDto itemDto = ItemMapper.toItemDto(item);

        Mockito.when(itemStorage.findById(anyLong()))
                .thenReturn(Optional.of(item));
        Mockito.when(userService.checkUser(anyLong()))
                .thenReturn(owner);

        // Act
        NotFoundException thrown = Assertions.assertThrows(NotFoundException.class, () -> {
            ItemDto actualDto = itemService.updateItem(itemDto, itemDto.getId(), otherUser.getId());
        });

        // Assert
        assertEquals(thrown.getMessage(), "User by ID: 2 - is not Owner of this Item");
    }

    @Test
    public void deleteItemTest() throws Exception {
        // Assign
        Optional<Item> item = getTestOptionalItem();

        Mockito.when(itemStorage.findById(anyLong()))
                .thenReturn(item);

        // Act
        itemService.deleteItem(item.get().getId());

        // Assert
        Mockito.verify(itemStorage, Mockito.times(1)).delete(item.get());
    }

    @Test
    public void findAll() throws Exception {
        // Assign
        Item item1 = getTestOptionalItem().get();
        Item item2 = getTestOptionalItem().get();
        List<Item> items = List.of(item1, item2);

        Mockito.when(itemStorage.findAll()).thenReturn(items);
        // Act
        List<ItemDto> actualItemDtos = itemService.findAll();

        // Assert
        Assertions.assertNotNull(actualItemDtos);
        Assertions.assertEquals(items.size(), actualItemDtos.size());
    }

    @Test
    public void findItemByIdTest() throws Exception {
        // Assign
        Item item1 = getTestOptionalItem().get();
        User user = getTestUser();
        Booking booking1 = getTestBooking(user, item1);
        Comment comment1 = getTestComment(item1, user);
        List<Comment> comments = List.of(comment1);
        ItemResponseDto itemResponseDto = ItemMapper.toItemResponseDto(item1, booking1, null, CommentMapper.toCommentDtos(comments));

        Mockito.when(itemStorage.findById(anyLong())).thenReturn(Optional.of(item1));
        Mockito.when(userService.checkUser(user.getId()))
                .thenReturn(user);
        Mockito.when(commentStorage.getCommentsByItem_idOrderByCreatedDesc(item1.getId()))
                .thenReturn(comments);
        Mockito.when(bookingStorage.findFirstByItem_idAndEndBeforeOrderByEndDesc(anyLong(), any(LocalDateTime.class))).thenReturn(booking1);
        Mockito.when(bookingStorage.findFirstByItem_idAndStartAfterOrderByStartDesc(anyLong(), any(LocalDateTime.class))).thenReturn(null);


        // Act
        ItemResponseDto actualItemResponseDto = itemService.findItemById(item1.getId(), user.getId());

        // Assert
        Assertions.assertNotNull(actualItemResponseDto);
        Assertions.assertEquals(actualItemResponseDto.getId(), itemResponseDto.getId());
        Assertions.assertEquals(actualItemResponseDto.getName(), itemResponseDto.getName());
        Assertions.assertEquals(actualItemResponseDto.getDescription(), itemResponseDto.getDescription());
        Assertions.assertEquals(actualItemResponseDto.getAvailable(), itemResponseDto.getAvailable());
        Assertions.assertEquals(actualItemResponseDto.getLastBooking().getId(), itemResponseDto.getLastBooking().getId());
        Assertions.assertNull(actualItemResponseDto.getNextBooking());

    }

    @Test
    public void findAllItemsByUserIdTest() throws Exception {
        // Assign
        Item item1 = getTestOptionalItem().get();
        Item item2 = getTestOptionalItem().get();
        List<Item> items = List.of(item1, item2);
        item2.setId(2L);
        User user = getTestUser();
        Booking booking1 = getTestBooking(user, item1);
        Booking booking2 = getTestBooking(user, item2);
        booking2.setId(2L);
        Comment comment1 = getTestComment(item1, user);
        List<Comment> comments = List.of(comment1);
        ItemResponseDto itemResponseDto1 = ItemMapper.toItemResponseDto(item1, booking1, null, CommentMapper.toCommentDtos(comments));
        ItemResponseDto itemResponseDto2 = ItemMapper.toItemResponseDto(item2, booking2, null, new ArrayList<>());
        List<ItemResponseDto> itemsDtos = List.of(itemResponseDto1, itemResponseDto2);

        Mockito.when(userService.checkUser(anyLong())).thenReturn(user);
        Mockito.when(itemStorage.findAllByOwnerIdOrderByIdAsc(anyLong(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(items));
        Mockito.when(commentStorage.getCommentsByItem_idOrderByCreatedDesc(item1.getId()))
                .thenReturn(comments);
        Mockito.when(bookingStorage.findFirstByItem_idAndEndBeforeOrderByEndDesc(anyLong(), any(LocalDateTime.class))).thenReturn(booking1);
        Mockito.when(bookingStorage.findFirstByItem_idAndStartAfterOrderByStartDesc(anyLong(), any(LocalDateTime.class))).thenReturn(null);

        // Act
        List<ItemResponseDto> actualItemDtos = itemService.findAllItemsByUserId(user.getId(), 0, 10);

        // Assert
        Assertions.assertNotNull(actualItemDtos);
        Assertions.assertEquals(actualItemDtos.size(), itemsDtos.size());
    }

    @Test
    public void searchItemsByNameAndDescriptionTest() throws Exception {
        // Assign
        Item item1 = getTestOptionalItem().get();
        Item item2 = getTestOptionalItem().get();
        item2.setId(2L);
        item2.setName("Search");
        List<Item> items = List.of(item1, item2);

        Mockito.when(itemStorage.findAllByNameAndDescriptionLowerCase(anyString(), anyString(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(items));

        // Act
        List<ItemDto> actualItems = itemService.searchItemsByNameAndDescription("search", 0, 10);

        // Assert
        Assertions.assertNotNull(actualItems);
        Assertions.assertEquals(items.size(), actualItems.size());
    }

    @Test
    public void checkItemTest() throws Exception {
        // Assign
        Item item = getTestOptionalItem().get();

        Mockito.when(itemStorage.findById(anyLong()))
                .thenReturn(Optional.of(item));

        // Act
        Item actualItem = itemService.checkItem(item.getId());

        // Assert
        Assertions.assertNotNull(actualItem);
        Assertions.assertEquals(item.getId(), actualItem.getId());
        Assertions.assertEquals(item.getName(), actualItem.getName());
    }

    @Test
    public void checkItemErrorTest() {
        // Assign
        Item item = getTestOptionalItem().get();

        Mockito.when(itemStorage.findById(anyLong()))
                .thenReturn(Optional.empty());

        // Act
        ItemNotFound thrown = Assertions.assertThrows(ItemNotFound.class, () -> {
            Item actualItem = itemService.checkItem(item.getId());
        });

        // Assert
        assertEquals(thrown.getMessage(), "Item by ID: %s  - not found");
    }

    @Test
    public void postCommentTest() throws Exception {
        // Assign
        Optional<Item> item = getTestOptionalItem();
        User booker = getTestUser();
        Booking booking = getTestBooking(booker, item.get());
        booking.setStatus(BookingStatus.APPROVED);
        List<Booking> bookings = List.of(booking);
        Comment comment = getTestComment(item.get(), booker);

        Mockito.when(userService.checkUser(anyLong()))
                .thenReturn(booker);
        Mockito.when(itemStorage.findById(item.get().getId()))
                .thenReturn(item);
        Mockito.when(bookingStorage.findByItem_IdAndBooker_IdOrderByStartDesc(anyLong(), anyLong()))
                .thenReturn(bookings);
        Mockito.when(commentStorage.save(any(Comment.class)))
                .thenReturn(comment);

        // Act
        CommentDto actualComment = itemService.postComment(item.get().getId(), booker.getId(), CommentMapper.toCommentDto(comment));

        // Assert
        Assertions.assertNotNull(actualComment);
        Assertions.assertEquals(comment.getId(), actualComment.getId());
    }

    @Test
    public void postCommentErrorEmptyTextTest() {
        // Assign
        Optional<Item> item = getTestOptionalItem();
        User booker = getTestUser();
        Booking booking = getTestBooking(booker, item.get());
        booking.setStatus(BookingStatus.APPROVED);
        List<Booking> bookings = List.of(booking);
        Comment comment = getTestComment(item.get(), booker);
        comment.setText("");

        // Act
        BadRequestException thrown = Assertions.assertThrows(BadRequestException.class, () -> {
            CommentDto actualComment = itemService.postComment(item.get().getId(), booker.getId(), CommentMapper.toCommentDto(comment));
        });

        // Assert
        assertEquals(thrown.getMessage(), "Comment is empty text");
    }

    @Test
    public void postCommentErrorBookingBadStatusTest() {
        // Assign
        Optional<Item> item = getTestOptionalItem();
        User booker = getTestUser();
        Booking booking = getTestBooking(booker, item.get());
        booking.setStatus(BookingStatus.REJECTED);
        List<Booking> bookings = List.of(booking);
        Comment comment = getTestComment(item.get(), booker);

        Mockito.when(userService.checkUser(anyLong()))
                .thenReturn(booker);
        Mockito.when(itemStorage.findById(item.get().getId()))
                .thenReturn(item);
        Mockito.when(bookingStorage.findByItem_IdAndBooker_IdOrderByStartDesc(anyLong(), anyLong()))
                .thenReturn(bookings);

        // Act
        BadRequestException thrown = Assertions.assertThrows(BadRequestException.class, () -> {
            CommentDto actualComment = itemService.postComment(item.get().getId(), booker.getId(), CommentMapper.toCommentDto(comment));
        });

        // Assert
        assertEquals(thrown.getMessage(), "Booking bad status");
    }

    @Test
    public void postCommentErrorUserNotBookerTest() {
        // Assign
        Optional<Item> item = getTestOptionalItem();
        User booker = getTestUser();
        Booking booking = getTestBooking(booker, item.get());
        booking.setStatus(BookingStatus.REJECTED);
        List<Booking> bookings = new ArrayList<>();
        Comment comment = getTestComment(item.get(), booker);

        Mockito.when(userService.checkUser(anyLong()))
                .thenReturn(booker);
        Mockito.when(itemStorage.findById(item.get().getId()))
                .thenReturn(item);
        Mockito.when(bookingStorage.findByItem_IdAndBooker_IdOrderByStartDesc(anyLong(), anyLong()))
                .thenReturn(bookings);

        // Act
        UserNotBooker thrown = Assertions.assertThrows(UserNotBooker.class, () -> {
            CommentDto actualComment = itemService.postComment(item.get().getId(), booker.getId(), CommentMapper.toCommentDto(comment));
        });

        // Assert
        assertEquals(thrown.getMessage(), "This User not Booker for this Item");
    }

    @Test
    public void getAllCommentsByItemTest() throws Exception {
        // Assign
        User booker = getTestUser();
        Item item = getTestOptionalItem().get();
        Comment comment = getTestComment(item, booker);
        List<Comment> comments = List.of(comment);

        Mockito.when(itemStorage.findById(anyLong()))
                .thenReturn(Optional.of(item));
        Mockito.when(commentStorage.getCommentsByItem_idOrderByCreatedDesc(item.getId()))
                .thenReturn(comments);

        // Act
        List<CommentDto> actualCommentDtos = itemService.getAllCommentsByItem(item.getId());

        // Assert
        Assertions.assertNotNull(actualCommentDtos);
        Assertions.assertEquals(comments.size(), actualCommentDtos.size());
    }

    private User getTestUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("@");
        user.setName("Test");
        return user;
    }

    private ItemDto getTestItemDto() {
        return new ItemDto(1L, "TestName", "Description", Boolean.TRUE, 1L);
    }

    private Optional<ItemRequest> getTestOptionalItemRequest() {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setRequestor(getTestUser());
        itemRequest.setId(1L);
        itemRequest.setDescription("Description");
        itemRequest.setCreated(LocalDateTime.now());
        return Optional.of(itemRequest);
    }

    private Optional<Item> getTestOptionalItem() {
        Item item = new Item();
        item.setId(1L);
        item.setName("TestName");
        item.setDescription("Description");
        item.setAvailable(Boolean.TRUE);
        item.setOwner(getTestUser());
        item.setRequest(getTestOptionalItemRequest().get());
        return Optional.of(item);
    }

    private Comment getTestComment(Item item, User author) {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setText("Test Comment Text");
        comment.setCreated(LocalDateTime.now());
        return comment;
    }

    private Booking getTestBooking(User booker, Item item) {
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
