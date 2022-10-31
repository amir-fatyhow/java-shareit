package ru.practicum.shareit.bookingTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.exception.BookingWrongTime;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repositories.BookingStorage;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.exceptions.ItemNullParametr;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.services.ItemService;
import ru.practicum.shareit.request.model.entity.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.services.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
public class BookingServiceMockTest {

    @Mock
    private BookingStorage bookingRepository;
    @Mock
    private UserService userService;
    @Mock
    private ItemService itemService;

    private BookingServiceImpl bookingService;

    @BeforeEach
    void init() {
        // Инициализируем сервис бинами заглушками
        bookingService = new BookingServiceImpl(
                bookingRepository,
                userService,
                itemService) {
        };
    }

    @Test
    public void createBookingTest() throws Exception {
        // Assign
        User owner = getTestUser();
        User booker = getTestUser();
        booker.setId(2L);
        ItemRequest itemRequest = getTestItemRequest(booker);
        Item item = getTestItem(owner, itemRequest);
        Booking booking = getTestBooking(booker, item);

        Mockito.when(userService.checkUser(anyLong())) // Когда вызовется метод checkUser() с любым long...
                .thenReturn(booker); // ... вернуть тестовое значение
        Mockito.when(itemService.checkItem(anyLong()))
                .thenReturn(item);
        Mockito.when(bookingRepository.findByItemAndBooker(any(Item.class), any(User.class)))
                .thenReturn(Optional.of(booking));
        Mockito.when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        // Act
        BookingResponseDto bookingResponseDto = bookingService.createBooking(BookingMapper.toBookingDto(booking), booker.getId());

        // Assert
        Assertions.assertNotNull(bookingResponseDto);
        Assertions.assertEquals(bookingResponseDto.getId(), booking.getId());
        Assertions.assertEquals(bookingResponseDto.getBooker().getId(), booking.getBooker().getId());
        Assertions.assertEquals(bookingResponseDto.getItem().getId(), item.getId());
        Assertions.assertEquals(bookingResponseDto.getStatus(), booking.getStatus());
    }

    @Test
    public void createBookingErrorBookerIsOwnerTest() {
        // Assign
        User owner = getTestUser();
        User booker = owner;
        ItemRequest itemRequest = getTestItemRequest(booker);
        Item item = getTestItem(owner, itemRequest);
        Booking booking = getTestBooking(booker, item);

        Mockito.when(userService.checkUser(anyLong()))
                .thenReturn(booker);
        Mockito.when(itemService.checkItem(anyLong()))
                .thenReturn(item);

        // Act
        NotFoundException thrown = Assertions.assertThrows(NotFoundException.class, () -> {
            BookingResponseDto bookingResponseDto = bookingService.createBooking(BookingMapper.toBookingDto(booking), item.getId());
        });

        // Assert
        assertEquals(thrown.getMessage(), "Booker can't book own Item");
    }

    @Test
    public void createBookingErrorRejectedTest() {
        // Assign
        User owner = getTestUser();
        User booker = getTestUser();
        booker.setId(2L);
        ItemRequest itemRequest = getTestItemRequest(booker);
        Item item = getTestItem(owner, itemRequest);
        Booking booking = getTestBooking(booker, item);
        booking.setStatus(BookingStatus.REJECTED);

        Mockito.when(userService.checkUser(anyLong()))
                .thenReturn(booker);
        Mockito.when(itemService.checkItem(anyLong()))
                .thenReturn(item);
        Mockito.when(bookingRepository.findByItemAndBooker(any(Item.class), any(User.class)))
                .thenReturn(Optional.of(booking));

        // Act
        NotFoundException thrown = Assertions.assertThrows(NotFoundException.class, () -> {
            BookingResponseDto bookingResponseDto = bookingService.createBooking(BookingMapper.toBookingDto(booking), item.getId());
        });

        // Assert
        assertEquals(thrown.getMessage(), "Booking REJECTED");
    }

    @Test
    public void createBookingErrorValidateTimeTest() {
        // Assign
        User owner = getTestUser();
        User booker = getTestUser();
        booker.setId(2L);
        ItemRequest itemRequest = getTestItemRequest(booker);
        Item item = getTestItem(owner, itemRequest);
        Booking booking = getTestBooking(booker, item);
        booking.setStart(LocalDateTime.now().plusSeconds(10));
        booking.setEnd(LocalDateTime.now().minusSeconds(10));

        // Act
        BookingWrongTime thrown = Assertions.assertThrows(BookingWrongTime.class, () -> {
            BookingResponseDto bookingResponseDto = bookingService.createBooking(BookingMapper.toBookingDto(booking), item.getId());
        });

        // Assert
        assertEquals(thrown.getMessage(), "Booking wrong Time");
    }

    @Test
    public void createBookingErrorUnavailableItemTest() {
        // Assign
        User owner = getTestUser();
        User booker = getTestUser();
        booker.setId(2L);
        ItemRequest itemRequest = getTestItemRequest(booker);
        Item item = getTestItem(owner, itemRequest);
        item.setAvailable(false);
        Booking booking = getTestBooking(booker, item);

        Mockito.when(userService.checkUser(anyLong()))
                .thenReturn(booker);
        Mockito.when(itemService.checkItem(anyLong()))
                .thenReturn(item);

        // Act
        ItemNullParametr thrown = Assertions.assertThrows(ItemNullParametr.class, () -> {
            BookingResponseDto bookingResponseDto = bookingService.createBooking(BookingMapper.toBookingDto(booking), item.getId());
        });

        // Assert
        assertEquals(thrown.getMessage(), "Item is unavailable");
    }

    @Test
    public void ownerDecisionTest() throws Exception {
        // Assign
        User owner = getTestUser();
        User booker = getTestUser();
        booker.setId(2L);
        ItemRequest itemRequest = getTestItemRequest(booker);
        Item item = getTestItem(owner, itemRequest);
        Booking booking = getTestBooking(booker, item);

        Mockito.when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));
        Mockito.when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        // Act

        BookingResponseDto bookingResponseDto = bookingService.ownerDecision(booking.getId(), owner.getId(), true);

        // Assert
        Assertions.assertNotNull(bookingResponseDto);
        Assertions.assertEquals(bookingResponseDto.getId(), booking.getId());
        Assertions.assertEquals(bookingResponseDto.getBooker().getId(), booking.getBooker().getId());
        Assertions.assertEquals(bookingResponseDto.getItem().getId(), item.getId());
        Assertions.assertEquals(bookingResponseDto.getStatus(), BookingStatus.APPROVED);
    }

    @Test
    public void ownerDecisionStatusRejectedTest() {
        // Assign
        User owner = getTestUser();
        User booker = getTestUser();
        booker.setId(2L);
        ItemRequest itemRequest = getTestItemRequest(booker);
        Item item = getTestItem(owner, itemRequest);
        Booking booking = getTestBooking(booker, item);

        Mockito.when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));
        Mockito.when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        // Act
        BookingResponseDto bookingDto = bookingService.ownerDecision(booking.getId(), item.getId(), false);

        // Assert
        assertEquals(bookingDto.getStatus(), BookingStatus.REJECTED);
    }

    @Test
    public void ownerDecisionErrorStatusNotWaitingTest() {
        // Assign
        User owner = getTestUser();
        User booker = getTestUser();
        booker.setId(2L);
        ItemRequest itemRequest = getTestItemRequest(booker);
        Item item = getTestItem(owner, itemRequest);
        Booking booking = getTestBooking(booker, item);
        booking.setStatus(BookingStatus.APPROVED);

        Mockito.when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        // Act
        BadRequestException thrown = Assertions.assertThrows(BadRequestException.class, () -> {
            BookingResponseDto bookingDto = bookingService.ownerDecision(booking.getId(), item.getId(), true);
        });

        // Assert
        assertEquals(thrown.getMessage(), "Status not WAITING");
    }

    @Test
    public void ownerDecisionErrorUserNotOwnerTest() {
        // Assign
        User owner = getTestUser();
        User booker = getTestUser();
        booker.setId(2L);
        ItemRequest itemRequest = getTestItemRequest(booker);
        Item item = getTestItem(booker, itemRequest);
        Booking booking = getTestBooking(booker, item);

        Mockito.when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        // Act
        NotFoundException thrown = Assertions.assertThrows(NotFoundException.class, () -> {
            BookingResponseDto bookingDto = bookingService.ownerDecision(booking.getId(), item.getId(), true);
        });

        // Assert
        assertEquals(thrown.getMessage(), "This User not Owner for this Item");
    }

    @Test
    public void getBookingTest() throws Exception {
        // Assign
        User owner = getTestUser();
        User booker = getTestUser();
        booker.setId(2L);
        ItemRequest itemRequest = getTestItemRequest(booker);
        Item item = getTestItem(owner, itemRequest);
        Booking booking = getTestBooking(booker, item);

        Mockito.when(userService.checkUser(anyLong())) // Когда вызовется метод checkUser() с любым long...
                .thenReturn(booker); // ... вернуть тестовое значение
        Mockito.when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        // Act
        BookingResponseDto bookingResponseDto = bookingService.getBooking(booking.getId(), booker.getId());

        // Assert
        Assertions.assertNotNull(bookingResponseDto);
        Assertions.assertEquals(bookingResponseDto.getId(), booking.getId());
        Assertions.assertEquals(bookingResponseDto.getBooker().getId(), booking.getBooker().getId());
        Assertions.assertEquals(bookingResponseDto.getItem().getId(), item.getId());
        Assertions.assertEquals(bookingResponseDto.getStatus(), BookingStatus.WAITING);
    }

    @Test
    public void getBookingErrorTest() {
        // Assign
        User booker = getTestUser();
        User owner = getTestUser();
        owner.setId(2L);
        User otherUser = getTestUser();
        otherUser.setId(3L);
        ItemRequest itemRequest = getTestItemRequest(booker);
        Item item = getTestItem(owner, itemRequest);
        Booking booking = getTestBooking(booker, item);

        Mockito.when(userService.checkUser(anyLong())) // Когда вызовется метод checkUser() с любым long...
                .thenReturn(otherUser); // ... вернуть тестовое значение
        Mockito.when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        // Act
        NotFoundException thrown = Assertions.assertThrows(NotFoundException.class, () -> {
            BookingResponseDto bookingDto = bookingService.getBooking(booking.getId(), otherUser.getId());
        });

        // Assert
        assertEquals(thrown.getMessage(), "User not Booker and not Owner");
    }

    @Test
    public void getAllBookingsByBookerTest() throws Exception {
        // Assign
        User owner = getTestUser();
        User booker = getTestUser();
        booker.setId(2L);
        ItemRequest itemRequest = getTestItemRequest(booker);
        Item item = getTestItem(owner, itemRequest);
        Booking booking1 = getTestBooking(booker, item);
        Booking booking2 = getTestBooking(booker, item);
        booking2.setId(2L);

        BookingResponseDto bookingResponseDto1 = BookingMapper.toBookingResponseDto(booking1);
        BookingResponseDto bookingResponseDto2 = BookingMapper.toBookingResponseDto(booking2);
        List<BookingResponseDto> bookings = List.of(bookingResponseDto1, bookingResponseDto2);

        Mockito.when(userService.checkUser(anyLong()))
                .thenReturn(booker);
        Mockito.when(bookingRepository.findAllByBookerOrderByStartDesc(any(User.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(booking1, booking2)));

        // Act
        List<BookingResponseDto> actualBookings = bookingService.getAllBookingsByBooker("ALL", booker.getId(), 0, 10);

        // Assert
        Assertions.assertNotNull(actualBookings);
        Assertions.assertEquals(bookings.size(), actualBookings.size());
    }

    @Test
    public void getAllBookingsByBookerWithoutPagingTest() {
        // Assign
        User owner = getTestUser();
        User booker = getTestUser();
        booker.setId(2L);
        ItemRequest itemRequest = getTestItemRequest(booker);
        Item item = getTestItem(owner, itemRequest);
        Booking booking1 = getTestBooking(booker, item);
        Booking booking2 = getTestBooking(booker, item);
        booking2.setId(2L);

        BookingResponseDto bookingResponseDto1 = BookingMapper.toBookingResponseDto(booking1);
        BookingResponseDto bookingResponseDto2 = BookingMapper.toBookingResponseDto(booking2);
        List<BookingResponseDto> bookings = List.of(bookingResponseDto1, bookingResponseDto2);

        Mockito.when(userService.checkUser(anyLong()))
                .thenReturn(booker);
        Mockito.when(bookingRepository.findAllByBookerOrderByStartDesc(any(User.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(booking1, booking2)));

        // Act
        List<BookingResponseDto> actualBookings = bookingService.getAllBookingsByBooker("ALL", booker.getId(), null, null);

        // Assert
        Assertions.assertNotNull(actualBookings);
        Assertions.assertEquals(bookings.size(), actualBookings.size());
    }

    @Test
    public void getAllBookingsByBookerFutureStateTest() {
        // Assign
        User owner = getTestUser();
        User booker = getTestUser();
        booker.setId(2L);
        ItemRequest itemRequest = getTestItemRequest(booker);
        Item item = getTestItem(owner, itemRequest);
        Booking booking1 = getTestBooking(booker, item);
        Booking booking2 = getTestBooking(booker, item);
        booking2.setId(2L);

        BookingResponseDto bookingResponseDto1 = BookingMapper.toBookingResponseDto(booking1);
        BookingResponseDto bookingResponseDto2 = BookingMapper.toBookingResponseDto(booking2);
        List<BookingResponseDto> bookings = List.of(bookingResponseDto1, bookingResponseDto2);

        Mockito.when(userService.checkUser(anyLong()))
                .thenReturn(booker);
        Mockito.when(bookingRepository.findAllByBookerAndStatusFutureOrderByStartDesc(anyLong(), anyString(), anyString()))
                .thenReturn(List.of(booking1, booking2));

        // Act
        List<BookingResponseDto> actualBookings = bookingService.getAllBookingsByBooker("FUTURE", booker.getId(), 0, 10);

        // Assert
        Assertions.assertNotNull(actualBookings);
        Assertions.assertEquals(bookings.size(), actualBookings.size());
    }

    @Test
    public void getAllBookingsByBookerCurrentStateTest() {
        // Assign
        User owner = getTestUser();
        User booker = getTestUser();
        booker.setId(2L);
        ItemRequest itemRequest = getTestItemRequest(booker);
        Item item = getTestItem(owner, itemRequest);
        Booking booking1 = getTestBooking(booker, item);
        Booking booking2 = getTestBooking(booker, item);
        booking2.setId(2L);

        BookingResponseDto bookingResponseDto1 = BookingMapper.toBookingResponseDto(booking1);
        BookingResponseDto bookingResponseDto2 = BookingMapper.toBookingResponseDto(booking2);
        List<BookingResponseDto> bookings = List.of(bookingResponseDto1, bookingResponseDto2);

        Mockito.when(userService.checkUser(anyLong()))
                .thenReturn(booker);
        Mockito.when(bookingRepository.findAllByBooker_idAndEndIsAfterAndStartIsBefore(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(booking1, booking2));

        // Act
        List<BookingResponseDto> actualBookings = bookingService.getAllBookingsByBooker("CURRENT", booker.getId(), 0, 10);

        // Assert
        Assertions.assertNotNull(actualBookings);
        Assertions.assertEquals(bookings.size(), actualBookings.size());
    }

    @Test
    public void getAllBookingsByBookerPastStateTest() {
        // Assign
        User owner = getTestUser();
        User booker = getTestUser();
        booker.setId(2L);
        ItemRequest itemRequest = getTestItemRequest(booker);
        Item item = getTestItem(owner, itemRequest);
        Booking booking1 = getTestBooking(booker, item);
        Booking booking2 = getTestBooking(booker, item);
        booking2.setId(2L);

        BookingResponseDto bookingResponseDto1 = BookingMapper.toBookingResponseDto(booking1);
        BookingResponseDto bookingResponseDto2 = BookingMapper.toBookingResponseDto(booking2);
        List<BookingResponseDto> bookings = List.of(bookingResponseDto1, bookingResponseDto2);

        Mockito.when(userService.checkUser(anyLong()))
                .thenReturn(booker);
        Mockito.when(bookingRepository.findAllByBooker_IdAndEndIsBefore(anyLong(), any(LocalDateTime.class)))
                .thenReturn(List.of(booking1, booking2));

        // Act
        List<BookingResponseDto> actualBookings = bookingService.getAllBookingsByBooker("PAST", booker.getId(), 0, 10);

        // Assert
        Assertions.assertNotNull(actualBookings);
        Assertions.assertEquals(bookings.size(), actualBookings.size());
    }

    @Test
    public void getAllBookingsByBookerWaitingStateTest() {
        // Assign
        User owner = getTestUser();
        User booker = getTestUser();
        booker.setId(2L);
        ItemRequest itemRequest = getTestItemRequest(booker);
        Item item = getTestItem(owner, itemRequest);
        Booking booking1 = getTestBooking(booker, item);
        Booking booking2 = getTestBooking(booker, item);
        booking2.setId(2L);

        BookingResponseDto bookingResponseDto1 = BookingMapper.toBookingResponseDto(booking1);
        BookingResponseDto bookingResponseDto2 = BookingMapper.toBookingResponseDto(booking2);
        List<BookingResponseDto> bookings = List.of(bookingResponseDto1, bookingResponseDto2);

        Mockito.when(userService.checkUser(anyLong()))
                .thenReturn(booker);
        Mockito.when(bookingRepository.findAllByBookerAndStatusOrderByStartDesc(anyLong(), anyString()))
                .thenReturn(List.of(booking1, booking2));

        // Act
        List<BookingResponseDto> actualBookings = bookingService.getAllBookingsByBooker("WAITING", booker.getId(), 0, 10);

        // Assert
        Assertions.assertNotNull(actualBookings);
        Assertions.assertEquals(bookings.size(), actualBookings.size());
    }

    @Test
    public void getAllBookingsByBookerRejectedStateTest() {
        // Assign
        User owner = getTestUser();
        User booker = getTestUser();
        booker.setId(2L);
        ItemRequest itemRequest = getTestItemRequest(booker);
        Item item = getTestItem(owner, itemRequest);
        Booking booking1 = getTestBooking(booker, item);
        Booking booking2 = getTestBooking(booker, item);
        booking2.setId(2L);

        BookingResponseDto bookingResponseDto1 = BookingMapper.toBookingResponseDto(booking1);
        BookingResponseDto bookingResponseDto2 = BookingMapper.toBookingResponseDto(booking2);
        List<BookingResponseDto> bookings = List.of(bookingResponseDto1, bookingResponseDto2);

        Mockito.when(userService.checkUser(anyLong()))
                .thenReturn(booker);
        Mockito.when(bookingRepository.findAllByBookerAndStatusOrderByStartDesc(anyLong(), anyString()))
                .thenReturn(List.of(booking1, booking2));

        // Act
        List<BookingResponseDto> actualBookings = bookingService.getAllBookingsByBooker("REJECTED", booker.getId(), 0, 10);

        // Assert
        Assertions.assertNotNull(actualBookings);
        Assertions.assertEquals(bookings.size(), actualBookings.size());
    }

    @Test
    public void getAllBookingsByBookerErrorWrongStateTest() {
        // Assign
        User owner = getTestUser();
        User booker = getTestUser();
        booker.setId(2L);
        ItemRequest itemRequest = getTestItemRequest(booker);
        Item item = getTestItem(owner, itemRequest);
        Booking booking1 = getTestBooking(booker, item);
        Booking booking2 = getTestBooking(booker, item);
        booking2.setId(2L);

        BookingResponseDto bookingResponseDto1 = BookingMapper.toBookingResponseDto(booking1);
        BookingResponseDto bookingResponseDto2 = BookingMapper.toBookingResponseDto(booking2);
        List<BookingResponseDto> bookings = List.of(bookingResponseDto1, bookingResponseDto2);

        Mockito.when(userService.checkUser(anyLong()))
                .thenReturn(booker);

        // Act
        BadRequestException thrown = Assertions.assertThrows(BadRequestException.class, () -> {
            List<BookingResponseDto> actualBookings = bookingService.getAllBookingsByBooker("UNSUPPORTED_STATUS", booker.getId(), 0, 10);
        });

        // Assert
        assertEquals(thrown.getMessage(), "Unknown state: UNSUPPORTED_STATUS");
    }

    @Test
    public void getAllBookingsByOwnerTest() throws Exception {
        // Assign
        User owner = getTestUser();
        User booker = getTestUser();
        booker.setId(2L);
        ItemRequest itemRequest = getTestItemRequest(booker);
        Item item = getTestItem(owner, itemRequest);
        Booking booking1 = getTestBooking(booker, item);
        Booking booking2 = getTestBooking(booker, item);
        booking2.setId(2L);

        BookingResponseDto bookingResponseDto1 = BookingMapper.toBookingResponseDto(booking1);
        BookingResponseDto bookingResponseDto2 = BookingMapper.toBookingResponseDto(booking2);
        List<BookingResponseDto> bookings = List.of(bookingResponseDto1, bookingResponseDto2);

        Mockito.when(userService.checkUser(anyLong()))
                .thenReturn(owner);
        Mockito.when(bookingRepository.findAllByItemOwnerOrderByStartDesc(any(User.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(booking1, booking2)));

        // Act
        List<BookingResponseDto> actualBookings = bookingService.getAllBookingsByOwner("ALL", owner.getId(), 0, 10);

        // Assert
        Assertions.assertNotNull(actualBookings);
        Assertions.assertEquals(bookings.size(), actualBookings.size());
    }

    @Test
    public void getAllBookingsByOwnerWithoutPagingTest() {
        // Assign
        User owner = getTestUser();
        User booker = getTestUser();
        booker.setId(2L);
        ItemRequest itemRequest = getTestItemRequest(booker);
        Item item = getTestItem(owner, itemRequest);
        Booking booking1 = getTestBooking(booker, item);
        Booking booking2 = getTestBooking(booker, item);
        booking2.setId(2L);

        BookingResponseDto bookingResponseDto1 = BookingMapper.toBookingResponseDto(booking1);
        BookingResponseDto bookingResponseDto2 = BookingMapper.toBookingResponseDto(booking2);
        List<BookingResponseDto> bookings = List.of(bookingResponseDto1, bookingResponseDto2);

        Mockito.when(userService.checkUser(anyLong()))
                .thenReturn(owner);
        Mockito.when(bookingRepository.findAllByItemOwnerOrderByStartDesc(any(User.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(booking1, booking2)));

        // Act
        List<BookingResponseDto> actualBookings = bookingService.getAllBookingsByOwner("ALL", owner.getId(), null, null);

        // Assert
        Assertions.assertNotNull(actualBookings);
        Assertions.assertEquals(bookings.size(), actualBookings.size());
    }

    @Test
    public void getAllBookingsByOwnerFutureStateTest() {
        // Assign
        User owner = getTestUser();
        User booker = getTestUser();
        booker.setId(2L);
        ItemRequest itemRequest = getTestItemRequest(booker);
        Item item = getTestItem(owner, itemRequest);
        Booking booking1 = getTestBooking(booker, item);
        Booking booking2 = getTestBooking(booker, item);
        booking2.setId(2L);

        BookingResponseDto bookingResponseDto1 = BookingMapper.toBookingResponseDto(booking1);
        BookingResponseDto bookingResponseDto2 = BookingMapper.toBookingResponseDto(booking2);
        List<BookingResponseDto> bookings = List.of(bookingResponseDto1, bookingResponseDto2);

        Mockito.when(userService.checkUser(anyLong()))
                .thenReturn(owner);
        Mockito.when(bookingRepository.findAllByOwnerAndStatusFutureOrderByStartDesc(anyLong(), anyString(), anyString()))
                .thenReturn(List.of(booking1, booking2));

        // Act
        List<BookingResponseDto> actualBookings = bookingService.getAllBookingsByOwner("FUTURE", owner.getId(), 0, 10);

        // Assert
        Assertions.assertNotNull(actualBookings);
        Assertions.assertEquals(bookings.size(), actualBookings.size());
    }

    @Test
    public void getAllBookingsByOwnerCurrentStateTest() {
        // Assign
        User owner = getTestUser();
        User booker = getTestUser();
        booker.setId(2L);
        ItemRequest itemRequest = getTestItemRequest(booker);
        Item item = getTestItem(owner, itemRequest);
        Booking booking1 = getTestBooking(booker, item);
        Booking booking2 = getTestBooking(booker, item);
        booking2.setId(2L);

        BookingResponseDto bookingResponseDto1 = BookingMapper.toBookingResponseDto(booking1);
        BookingResponseDto bookingResponseDto2 = BookingMapper.toBookingResponseDto(booking2);
        List<BookingResponseDto> bookings = List.of(bookingResponseDto1, bookingResponseDto2);

        Mockito.when(userService.checkUser(anyLong()))
                .thenReturn(owner);
        Mockito.when(bookingRepository.findAllByOwner_IdAndEndIsAfterAndStartIsBefore(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(booking1, booking2));

        // Act
        List<BookingResponseDto> actualBookings = bookingService.getAllBookingsByOwner("CURRENT", owner.getId(), 0, 10);

        // Assert
        Assertions.assertNotNull(actualBookings);
        Assertions.assertEquals(bookings.size(), actualBookings.size());
    }

    @Test
    public void getAllBookingsByOwnerPastStateTest() {
        // Assign
        User owner = getTestUser();
        User booker = getTestUser();
        booker.setId(2L);
        ItemRequest itemRequest = getTestItemRequest(booker);
        Item item = getTestItem(owner, itemRequest);
        Booking booking1 = getTestBooking(booker, item);
        Booking booking2 = getTestBooking(booker, item);
        booking2.setId(2L);

        BookingResponseDto bookingResponseDto1 = BookingMapper.toBookingResponseDto(booking1);
        BookingResponseDto bookingResponseDto2 = BookingMapper.toBookingResponseDto(booking2);
        List<BookingResponseDto> bookings = List.of(bookingResponseDto1, bookingResponseDto2);

        Mockito.when(userService.checkUser(anyLong()))
                .thenReturn(owner);
        Mockito.when(bookingRepository.findAllByOwner_IdAndEndIsBefore(anyLong(), any(LocalDateTime.class)))
                .thenReturn(List.of(booking1, booking2));

        // Act
        List<BookingResponseDto> actualBookings = bookingService.getAllBookingsByOwner("PAST", owner.getId(), 0, 10);

        // Assert
        Assertions.assertNotNull(actualBookings);
        Assertions.assertEquals(bookings.size(), actualBookings.size());
    }

    @Test
    public void getAllBookingsByOwnerWaitingStateTest() {
        // Assign
        User owner = getTestUser();
        User booker = getTestUser();
        booker.setId(2L);
        ItemRequest itemRequest = getTestItemRequest(booker);
        Item item = getTestItem(owner, itemRequest);
        Booking booking1 = getTestBooking(booker, item);
        Booking booking2 = getTestBooking(booker, item);
        booking2.setId(2L);

        BookingResponseDto bookingResponseDto1 = BookingMapper.toBookingResponseDto(booking1);
        BookingResponseDto bookingResponseDto2 = BookingMapper.toBookingResponseDto(booking2);
        List<BookingResponseDto> bookings = List.of(bookingResponseDto1, bookingResponseDto2);

        Mockito.when(userService.checkUser(anyLong()))
                .thenReturn(owner);
        Mockito.when(bookingRepository.findAllByItemOwnerAndStatusOrderByStartDesc(anyLong(), anyString()))
                .thenReturn(List.of(booking1, booking2));

        // Act
        List<BookingResponseDto> actualBookings = bookingService.getAllBookingsByOwner("WAITING", owner.getId(), 0, 10);

        // Assert
        Assertions.assertNotNull(actualBookings);
        Assertions.assertEquals(bookings.size(), actualBookings.size());
    }

    @Test
    public void getAllBookingsByOwnerRejectedStateTest() {
        // Assign
        User owner = getTestUser();
        User booker = getTestUser();
        booker.setId(2L);
        ItemRequest itemRequest = getTestItemRequest(booker);
        Item item = getTestItem(owner, itemRequest);
        Booking booking1 = getTestBooking(booker, item);
        Booking booking2 = getTestBooking(booker, item);
        booking2.setId(2L);

        BookingResponseDto bookingResponseDto1 = BookingMapper.toBookingResponseDto(booking1);
        BookingResponseDto bookingResponseDto2 = BookingMapper.toBookingResponseDto(booking2);
        List<BookingResponseDto> bookings = List.of(bookingResponseDto1, bookingResponseDto2);

        Mockito.when(userService.checkUser(anyLong()))
                .thenReturn(owner);
        Mockito.when(bookingRepository.findAllByItemOwnerAndStatusOrderByStartDesc(anyLong(), anyString()))
                .thenReturn(List.of(booking1, booking2));

        // Act
        List<BookingResponseDto> actualBookings = bookingService.getAllBookingsByOwner("REJECTED", owner.getId(), 0, 10);

        // Assert
        Assertions.assertNotNull(actualBookings);
        Assertions.assertEquals(bookings.size(), actualBookings.size());
    }

    @Test
    public void getAllBookingsByOwnerErrorWrongStateTest() {
        // Assign
        User owner = getTestUser();
        User booker = getTestUser();
        booker.setId(2L);
        ItemRequest itemRequest = getTestItemRequest(booker);
        Item item = getTestItem(owner, itemRequest);
        Booking booking1 = getTestBooking(booker, item);
        Booking booking2 = getTestBooking(booker, item);
        booking2.setId(2L);

        BookingResponseDto bookingResponseDto1 = BookingMapper.toBookingResponseDto(booking1);
        BookingResponseDto bookingResponseDto2 = BookingMapper.toBookingResponseDto(booking2);
        List<BookingResponseDto> bookings = List.of(bookingResponseDto1, bookingResponseDto2);

        Mockito.when(userService.checkUser(anyLong()))
                .thenReturn(owner);

        // Act
        BadRequestException thrown = Assertions.assertThrows(BadRequestException.class, () -> {
            List<BookingResponseDto> actualBookings = bookingService.getAllBookingsByOwner("UNSUPPORTED_STATUS", owner.getId(), 0, 10);
        });

        // Assert
        assertEquals(thrown.getMessage(), "Unknown state: UNSUPPORTED_STATUS");
    }

    @Test
    public void getLastBookingByItemTest() throws Exception {
        // Assign
        User owner = getTestUser();
        User booker = getTestUser();
        booker.setId(2L);
        ItemRequest itemRequest = getTestItemRequest(booker);
        Item item = getTestItem(owner, itemRequest);
        Booking booking1 = getTestBooking(booker, item);

        Mockito.when(bookingRepository.findFirstByItem_idAndEndBeforeOrderByEndDesc(anyLong(), any(LocalDateTime.class)))
                .thenReturn(booking1);

        // Act
        Booking actualBooking = bookingService.getLastBookingByItem(item.getId());

        // Assert
        Assertions.assertNotNull(actualBooking);
        Assertions.assertEquals(booking1.getId(), actualBooking.getId());
        Assertions.assertEquals(booking1.getBooker().getId(), actualBooking.getBooker().getId());
        Assertions.assertEquals(booking1.getItem().getId(), actualBooking.getItem().getId());
        Assertions.assertEquals(booking1.getStatus(), actualBooking.getStatus());
    }

    @Test
    public void getNextBookingByItem() throws Exception {
        // Assign
        User owner = getTestUser();
        User booker = getTestUser();
        booker.setId(2L);
        ItemRequest itemRequest = getTestItemRequest(booker);
        Item item = getTestItem(owner, itemRequest);
        Booking booking1 = getTestBooking(booker, item);

        Mockito.when(bookingRepository.findFirstByItem_idAndStartAfterOrderByStartDesc(anyLong(), any(LocalDateTime.class)))
                .thenReturn(booking1);

        // Act
        Booking actualBooking = bookingService.getNextBookingByItem(item.getId());

        // Assert
        Assertions.assertNotNull(actualBooking);
        Assertions.assertEquals(booking1.getId(), actualBooking.getId());
        Assertions.assertEquals(booking1.getBooker().getId(), actualBooking.getBooker().getId());
        Assertions.assertEquals(booking1.getItem().getId(), actualBooking.getItem().getId());
        Assertions.assertEquals(booking1.getStatus(), actualBooking.getStatus());
    }

    @Test
    public void checkBookingTest() {
        // Assign
        User owner = getTestUser();
        User booker = getTestUser();
        ItemRequest itemRequest = getTestItemRequest(booker);
        booker.setId(2L);
        booker.setEmail("2@email.ru");
        Item item = getTestItem(owner, itemRequest);
        Booking booking = getTestBooking(booker, item);

        Mockito.when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        // Act
        Booking actualBooking = bookingService.checkBooking(booking.getId());

        // Assert
        Assertions.assertNotNull(actualBooking);
        Assertions.assertEquals(booking.getId(), actualBooking.getId());
        Assertions.assertEquals(booking.getBooker().getId(), actualBooking.getBooker().getId());
        Assertions.assertEquals(booking.getItem().getId(), actualBooking.getItem().getId());
        Assertions.assertEquals(booking.getStatus(), actualBooking.getStatus());
    }

    @Test
    public void checkBookingTestBookingIsNull() throws Exception {
        // Assign
        User owner = getTestUser();
        User booker = getTestUser();
        ItemRequest itemRequest = getTestItemRequest(booker);
        booker.setId(2L);
        booker.setEmail("2@email.ru");
        Item item = getTestItem(owner, itemRequest);
        Booking booking = getTestBooking(booker, item);

        Mockito.when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        // Act
        NotFoundException thrown = Assertions.assertThrows(NotFoundException.class, () -> {
            Optional<Booking> actualOptionalBooking = Optional.of(bookingService.checkBooking(booking.getId()));
        });

        // Assert
        assertEquals(thrown.getMessage(), "Booking by ID: 1 - not found");
    }

    private User getTestUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("@");
        user.setName("Test");
        return user;
    }

    private ItemRequest getTestItemRequest(User requestor) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        itemRequest.setDescription("Test Description ItemRequest");
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(LocalDateTime.now());
        return itemRequest;
    }

    private Item getTestItem(User owner, ItemRequest itemRequest) {
        Item item = new Item();
        item.setId(1L);
        item.setName("Test Name Item");
        item.setDescription("Test Description Item");
        item.setOwner(owner);
        item.setRequest(itemRequest);
        item.setAvailable(true);
        return item;
    }

    private Booking getTestBooking(User booker, Item item) {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStart((LocalDateTime.now().plusSeconds(10)));
        booking.setEnd((LocalDateTime.now().plusSeconds(20)));
        booking.setStatus(BookingStatus.WAITING);
        return booking;
    }
}
