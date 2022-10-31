package ru.practicum.shareit.bookingTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.ShareItNotFoundException;
import ru.practicum.shareit.exception.UnsupportedStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.entity.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceMockTest {
    private static final String USER_NOT_FOUND = "Пользователя с указанным Id не существует.";
    private static final String ITEM_NOT_FOUND = "Вещь с указанным Id не существует.";
    private static final String BOOKING_NOT_FOUND = "Бронирование вещи с указанным Id не существует.";
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;

    private BookingServiceImpl bookingService;

    @BeforeEach
    void setUp() {

        bookingService = new BookingServiceImpl(bookingRepository, userRepository, itemRepository);
    }

    @Test
    void saveTest() {
        // Assign
        User owner = user();
        User booker = user();
        booker.setId(2L);

        ItemRequest itemRequest = itemRequest(booker);
        Item item = item(owner, itemRequest);

        Booking booking = getTestBooking(booker, item);

        when(userRepository.findById(anyLong()))
                        .thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong()))
                        .thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        // Act
        BookingResponseDto bookingResponseDto = bookingService.save(BookingMapper.toBookingDto(booking), booker.getId());

        // Assert
        assertNotNull(bookingResponseDto);
        assertEquals(booking.getId(), bookingResponseDto.getId());
        assertEquals(booking.getBooker().getId(), bookingResponseDto.getBooker().getId());
        assertEquals(item.getId(), bookingResponseDto.getItem().getId());
        assertEquals(booking.getStatus(), bookingResponseDto.getStatus());
    }

    @Test
    void saveBookerNotFoundTest() {
        // Assign
        User owner = user();
        ItemRequest itemRequest = itemRequest(owner);
        Item item = item(owner, itemRequest);
        Booking booking = getTestBooking(owner, item);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        // Act
        ShareItNotFoundException exception = assertThrows(ShareItNotFoundException.class, () -> {
            bookingService.save(BookingMapper.toBookingDto(booking), item.getId());
        });

        // Assert
        assertEquals(USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void saveItemNotFoundTest() {
        // Assign
        User owner = user();
        ItemRequest itemRequest = itemRequest(owner);
        Item item = item(owner, itemRequest);
        Booking booking = getTestBooking(owner, item);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(owner));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        // Act
        ShareItNotFoundException exception = assertThrows(ShareItNotFoundException.class, () -> {
            bookingService.save(BookingMapper.toBookingDto(booking), item.getId());
        });

        // Assert
        assertEquals(ITEM_NOT_FOUND, exception.getMessage());
    }

    @Test
    void saveInvalidBookerTest() {
        // Assign
        User owner = user();
        ItemRequest itemRequest = itemRequest(owner);
        Item item = item(owner, itemRequest);
        Booking booking = getTestBooking(owner, item);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(owner));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));

        // Act
        ShareItNotFoundException exception = assertThrows(ShareItNotFoundException.class, () -> {
            bookingService.save(BookingMapper.toBookingDto(booking), item.getId());
        });

        // Assert
        assertEquals("Пользователь вещи не может ее забронировать.", exception.getMessage());
    }

    @Test
    void saveInvalidTimeTest() {
        // Assign
        User owner = user();
        User booker = user();
        booker.setId(2L);
        ItemRequest itemRequest = itemRequest(booker);
        Item item = item(owner, itemRequest);
        Booking booking = getTestBooking(booker, item);
        booking.setStart(LocalDateTime.now().plusSeconds(10));
        booking.setEnd(LocalDateTime.now().minusSeconds(10));

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));
        // Act
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            bookingService.save(BookingMapper.toBookingDto(booking), item.getId());
        });

        // Assert
        assertEquals("Ошибка в дате бронирования.", exception.getMessage());
    }

    @Test
    void saveInvalidItemTest() {
        // Assign
        User owner = user();
        User booker = user();
        booker.setId(2L);
        ItemRequest itemRequest = itemRequest(booker);
        Item item = item(owner, itemRequest);
        item.setAvailable(false);
        Booking booking = getTestBooking(booker, item);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));

        // Act
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            bookingService.save(BookingMapper.toBookingDto(booking), item.getId());
        });

        // Assert
        assertEquals("Вещь для бронирования недоступна.", exception.getMessage());
    }

    @Test
    void updateTest() {
        // Assign
        User owner = user();
        User booker = user();
        booker.setId(2L);
        ItemRequest itemRequest = itemRequest(booker);
        Item item = item(owner, itemRequest);
        Booking booking = getTestBooking(booker, item);

        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        // Act

        BookingResponseDto bookingResponseDto = bookingService.update(booking.getId(), owner.getId(), true);

        // Assert
        assertNotNull(bookingResponseDto);
        assertEquals(booking.getId(), bookingResponseDto.getId());
        assertEquals(booking.getBooker().getId(), bookingResponseDto.getBooker().getId());
        assertEquals(item.getId(), bookingResponseDto.getItem().getId());
        assertEquals(BookingStatus.APPROVED, bookingResponseDto.getStatus());
    }

    @Test
    void updateBookingNotFoundTest() {
        // Assign
        User owner = user();
        User booker = user();
        booker.setId(2L);
        ItemRequest itemRequest = itemRequest(booker);
        Item item = item(owner, itemRequest);
        Booking booking = getTestBooking(booker, item);

        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        // Act
        ShareItNotFoundException exception = assertThrows(ShareItNotFoundException.class, () -> {
           bookingService.update(booking.getId(), owner.getId(), true);
        });

        // Assert
        assertEquals(BOOKING_NOT_FOUND, exception.getMessage());
    }

    @Test
    void updateStatusRejectTest() {
        // Assign
        User owner = user();
        User booker = user();
        booker.setId(2L);
        ItemRequest itemRequest = itemRequest(booker);
        Item item = item(owner, itemRequest);
        Booking booking = getTestBooking(booker, item);

        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        // Act
        BookingResponseDto bookingDto = bookingService.update(booking.getId(), item.getId(), false);

        // Assert
        assertEquals(BookingStatus.REJECTED, bookingDto.getStatus());
    }

    @Test
    void updateDoubleApprovedTest() {
        // Assign
        User owner = user();
        User booker = user();
        booker.setId(2L);
        ItemRequest itemRequest = itemRequest(booker);
        Item item = item(owner, itemRequest);
        Booking booking = getTestBooking(booker, item);
        booking.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        // Act
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            bookingService.update(booking.getId(), item.getId(), true);
        });

        // Assert
        assertEquals("Второе подтверждение статуса.", exception.getMessage());
    }

    @Test
    void updateNotOwnerTest() {
        // Assign
        User booker = user();
        booker.setId(2L);
        ItemRequest itemRequest = itemRequest(booker);
        Item item = item(booker, itemRequest);
        Booking booking = getTestBooking(booker, item);

        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        // Act
        ShareItNotFoundException thrown = assertThrows(ShareItNotFoundException.class, () -> {
            bookingService.update(booking.getId(), item.getId(), true);
        });

        // Assert
        assertEquals("Менять статус может только владелец.", thrown.getMessage());
    }

    @Test
    void findByIdTest() {
        // Assign
        User owner = user();
        User booker = user();
        booker.setId(2L);
        ItemRequest itemRequest = itemRequest(booker);
        Item item = item(owner, itemRequest);
        Booking booking = getTestBooking(booker, item);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));

        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        // Act
        BookingResponseDto bookingResponseDto = bookingService.findById(booking.getId(), booker.getId());

        // Assert
        assertNotNull(bookingResponseDto);
        Assertions.assertEquals(booking.getId(), bookingResponseDto.getId());
        Assertions.assertEquals(booking.getBooker().getId(), bookingResponseDto.getBooker().getId());
        Assertions.assertEquals(item.getId(), bookingResponseDto.getItem().getId());
        Assertions.assertEquals(BookingStatus.WAITING, bookingResponseDto.getStatus());
    }

    @Test
    void findByIdBookingNotFoundTest() {
        // Assign
        User owner = user();
        User booker = user();
        booker.setId(2L);
        ItemRequest itemRequest = itemRequest(booker);
        Item item = item(owner, itemRequest);
        Booking booking = getTestBooking(booker, item);

        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        // Act
        ShareItNotFoundException exception = assertThrows(ShareItNotFoundException.class, () -> {
            bookingService.findById(booking.getId(), booker.getId());
        });

        // Assert
        assertEquals(BOOKING_NOT_FOUND, exception.getMessage());
    }

    @Test
    void findByIdUserNotFoundTest() {
        // Assign
        User owner = user();
        User booker = user();
        booker.setId(2L);
        ItemRequest itemRequest = itemRequest(booker);
        Item item = item(owner, itemRequest);
        Booking booking = getTestBooking(booker, item);

        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        // Act
        ShareItNotFoundException exception = assertThrows(ShareItNotFoundException.class, () -> {
            bookingService.findById(booking.getId(), booker.getId());
        });

        // Assert
        assertEquals(USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void findByIdNotOwnerAndBookerTest() {
        // Assign
        User booker = user();
        User owner = user();
        owner.setId(2L);
        User otherUser = user();
        otherUser.setId(3L);
        ItemRequest itemRequest = itemRequest(booker);
        Item item = item(owner, itemRequest);
        Booking booking = getTestBooking(booker, item);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(otherUser));

        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        // Act
        ShareItNotFoundException thrown = assertThrows(ShareItNotFoundException.class, () -> {
            bookingService.findById(booking.getId(), otherUser.getId());
        });

        // Assert
        assertEquals(thrown.getMessage(), "Просматривать вещь может или пользователь, " +
                "который ее забронировал, или ее владелец.");
    }

    @Test
    void findByIdBookerTest() {
        // Assign
        User booker = user();
        User owner = user();
        owner.setId(2L);
        User otherUser = user();
        otherUser.setId(1L);
        ItemRequest itemRequest = itemRequest(booker);
        Item item = item(owner, itemRequest);
        Booking booking = getTestBooking(booker, item);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(otherUser));

        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        // Act
        BookingResponseDto bookingResponseDto = bookingService.findById(booking.getId(), otherUser.getId());

        // Assert
        assertNotNull(bookingResponseDto);
        Assertions.assertEquals(booking.getId(), bookingResponseDto.getId());
        Assertions.assertEquals(booking.getBooker().getId(), bookingResponseDto.getBooker().getId());
        Assertions.assertEquals(item.getId(), bookingResponseDto.getItem().getId());
        Assertions.assertEquals(BookingStatus.WAITING, bookingResponseDto.getStatus());
    }

    @Test
    void findByIdOwnerTest() {
        // Assign
        User booker = user();
        User owner = user();
        owner.setId(2L);
        User otherUser = user();
        otherUser.setId(2L);
        ItemRequest itemRequest = itemRequest(booker);
        Item item = item(owner, itemRequest);
        Booking booking = getTestBooking(booker, item);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(otherUser));

        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        // Act
        BookingResponseDto bookingResponseDto = bookingService.findById(booking.getId(), otherUser.getId());

        // Assert
        assertNotNull(bookingResponseDto);
        Assertions.assertEquals(booking.getId(), bookingResponseDto.getId());
        Assertions.assertEquals(booking.getBooker().getId(), bookingResponseDto.getBooker().getId());
        Assertions.assertEquals(item.getId(), bookingResponseDto.getItem().getId());
        Assertions.assertEquals(BookingStatus.WAITING, bookingResponseDto.getStatus());
    }

    @Test
    void findByStateUserNotFoundTest() {
        // Assign
        User owner = user();
        User booker = user();
        booker.setId(2L);
        ItemRequest itemRequest = itemRequest(booker);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        // Act
        ShareItNotFoundException exception = assertThrows(ShareItNotFoundException.class, () -> {
            bookingService.findByState("ALL", booker.getId(), 0, 10);
        });

        // Assert
        assertEquals(USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void findByStateAllTest() {
        // Assign
        User owner = user();
        User booker = user();
        booker.setId(2L);
        ItemRequest itemRequest = itemRequest(booker);
        Item item = item(owner, itemRequest);
        Booking booking1 = getTestBooking(booker, item);
        Booking booking2 = getTestBooking(booker, item);
        booking2.setId(2L);

        BookingResponseDto bookingResponseDto1 = BookingMapper.toBookingResponseDto(booking1);
        BookingResponseDto bookingResponseDto2 = BookingMapper.toBookingResponseDto(booking2);
        List<BookingResponseDto> bookings = List.of(bookingResponseDto1, bookingResponseDto2);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerOrderByStartDesc(any(User.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(booking1, booking2)));

        // Act
        List<BookingResponseDto> actualBookings = bookingService.findByState("ALL", booker.getId(), 0, 10);

        // Assert
        assertNotNull(actualBookings);
        Assertions.assertEquals(bookings.size(), actualBookings.size());
    }

    @Test
    void findByStateFutureTest() {
        // Assign
        User owner = user();
        User booker = user();
        booker.setId(2L);
        ItemRequest itemRequest = itemRequest(booker);
        Item item = item(owner, itemRequest);
        Booking booking1 = getTestBooking(booker, item);
        Booking booking2 = getTestBooking(booker, item);
        booking2.setId(2L);

        BookingResponseDto bookingResponseDto1 = BookingMapper.toBookingResponseDto(booking1);
        BookingResponseDto bookingResponseDto2 = BookingMapper.toBookingResponseDto(booking2);
        List<BookingResponseDto> bookings = List.of(bookingResponseDto1, bookingResponseDto2);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any(LocalDateTime.class)))
                .thenReturn(List.of(booking1, booking2));

        // Act
        List<BookingResponseDto> actualBookings = bookingService.findByState("FUTURE", booker.getId(), 0, 10);

        // Assert
        assertNotNull(actualBookings);
        Assertions.assertEquals(bookings.size(), actualBookings.size());
    }

    @Test
    void findByStateCurrentTest() {
        // Assign
        User owner = user();
        User booker = user();
        booker.setId(2L);
        ItemRequest itemRequest = itemRequest(booker);
        Item item = item(owner, itemRequest);
        Booking booking1 = getTestBooking(booker, item);
        Booking booking2 = getTestBooking(booker, item);
        booking2.setId(2L);

        BookingResponseDto bookingResponseDto1 = BookingMapper.toBookingResponseDto(booking1);
        BookingResponseDto bookingResponseDto2 = BookingMapper.toBookingResponseDto(booking2);
        List<BookingResponseDto> bookings = List.of(bookingResponseDto1, bookingResponseDto2);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerAndEndIsAfterAndStartIsBefore(any(User.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(booking1, booking2));

        // Act
        List<BookingResponseDto> actualBookings = bookingService.findByState("CURRENT", booker.getId(), 0, 10);

        // Assert
        assertNotNull(actualBookings);
        Assertions.assertEquals(bookings.size(), actualBookings.size());
    }

    @Test
    void findByStatePastTest() {
        // Assign
        User owner = user();
        User booker = user();
        booker.setId(2L);
        ItemRequest itemRequest = itemRequest(booker);
        Item item = item(owner, itemRequest);
        Booking booking1 = getTestBooking(booker, item);
        Booking booking2 = getTestBooking(booker, item);
        booking2.setId(2L);

        BookingResponseDto bookingResponseDto1 = BookingMapper.toBookingResponseDto(booking1);
        BookingResponseDto bookingResponseDto2 = BookingMapper.toBookingResponseDto(booking2);
        List<BookingResponseDto> bookings = List.of(bookingResponseDto1, bookingResponseDto2);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerAndEndIsBefore(any(User.class), any(LocalDateTime.class)))
                .thenReturn(List.of(booking1, booking2));

        // Act
        List<BookingResponseDto> actualBookings = bookingService.findByState("PAST", booker.getId(), 0, 10);

        // Assert
        assertNotNull(actualBookings);
        Assertions.assertEquals(bookings.size(), actualBookings.size());
    }

    @Test
    void findByStateWaitingTest() {
        // Assign
        User owner = user();
        User booker = user();
        booker.setId(2L);
        ItemRequest itemRequest = itemRequest(booker);
        Item item = item(owner, itemRequest);
        Booking booking1 = getTestBooking(booker, item);
        Booking booking2 = getTestBooking(booker, item);
        booking2.setId(2L);

        BookingResponseDto bookingResponseDto1 = BookingMapper.toBookingResponseDto(booking1);
        BookingResponseDto bookingResponseDto2 = BookingMapper.toBookingResponseDto(booking2);
        List<BookingResponseDto> bookings = List.of(bookingResponseDto1, bookingResponseDto2);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerAndStatusOrderByStartDesc(any(User.class), any(BookingStatus.class)))
                .thenReturn(List.of(booking1, booking2));

        // Act
        List<BookingResponseDto> actualBookings = bookingService.findByState("WAITING", booker.getId(), 0, 10);

        // Assert
        assertNotNull(actualBookings);
        Assertions.assertEquals(bookings.size(), actualBookings.size());
    }

    @Test
    void findByStateRejectedTest() {
        // Assign
        User owner = user();
        User booker = user();
        booker.setId(2L);
        ItemRequest itemRequest = itemRequest(booker);
        Item item = item(owner, itemRequest);
        Booking booking1 = getTestBooking(booker, item);
        Booking booking2 = getTestBooking(booker, item);
        booking2.setId(2L);

        BookingResponseDto bookingResponseDto1 = BookingMapper.toBookingResponseDto(booking1);
        BookingResponseDto bookingResponseDto2 = BookingMapper.toBookingResponseDto(booking2);
        List<BookingResponseDto> bookings = List.of(bookingResponseDto1, bookingResponseDto2);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerAndStatusOrderByStartDesc(any(User.class), any(BookingStatus.class)))
                .thenReturn(List.of(booking1, booking2));

        // Act
        List<BookingResponseDto> actualBookings = bookingService.findByState("REJECTED", booker.getId(), 0, 10);

        // Assert
        assertNotNull(actualBookings);
        Assertions.assertEquals(bookings.size(), actualBookings.size());
    }

    @Test
    void findByStateUnsupportedTest() {
        // Assign
        User owner = user();
        User booker = user();
        booker.setId(2L);
        ItemRequest itemRequest = itemRequest(booker);
        Item item = item(owner, itemRequest);
        Booking booking1 = getTestBooking(booker, item);
        Booking booking2 = getTestBooking(booker, item);
        booking2.setId(2L);

        BookingResponseDto bookingResponseDto1 = BookingMapper.toBookingResponseDto(booking1);
        BookingResponseDto bookingResponseDto2 = BookingMapper.toBookingResponseDto(booking2);
        List<BookingResponseDto> bookings = List.of(bookingResponseDto1, bookingResponseDto2);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));

        // Act
        UnsupportedStatus exception = assertThrows(UnsupportedStatus.class, () -> {
            bookingService.findByState("UNSUPPORTED_STATUS", booker.getId(), 0, 10);
        });

        // Assert
        assertEquals("Неправильное именование статуса бронирования.", exception.getMessage());
    }

    @Test
    void findOwnerItemsUserNotFoundTest() {
        // Assign
        User owner = user();
        User booker = user();
        booker.setId(2L);
        ItemRequest itemRequest = itemRequest(booker);
        Item item = item(owner, itemRequest);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        // Act
        ShareItNotFoundException exception = assertThrows(ShareItNotFoundException.class, () -> {
            bookingService.findOwnerItems("ALL", owner.getId(), 0, 10);
        });

        // Assert
        assertEquals(USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void findOwnerItemsStateAllTest() {
        // Assign
        User owner = user();
        User booker = user();
        booker.setId(2L);
        ItemRequest itemRequest = itemRequest(booker);
        Item item = item(owner, itemRequest);
        Booking booking1 = getTestBooking(booker, item);
        Booking booking2 = getTestBooking(booker, item);
        booking2.setId(2L);

        BookingResponseDto bookingResponseDto1 = BookingMapper.toBookingResponseDto(booking1);
        BookingResponseDto bookingResponseDto2 = BookingMapper.toBookingResponseDto(booking2);
        List<BookingResponseDto> bookings = List.of(bookingResponseDto1, bookingResponseDto2);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByItemOwnerOrderByStartDesc(any(User.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(booking1, booking2)));

        // Act
        List<BookingResponseDto> actualBookings = bookingService.findOwnerItems("ALL", owner.getId(), 0, 10);

        // Assert
        assertNotNull(actualBookings);
        Assertions.assertEquals(bookings.size(), actualBookings.size());
    }

    @Test
    void findOwnerItemsStateFutureTest() {
        // Assign
        User owner = user();
        User booker = user();
        booker.setId(2L);
        ItemRequest itemRequest = itemRequest(booker);
        Item item = item(owner, itemRequest);
        Booking booking1 = getTestBooking(booker, item);
        Booking booking2 = getTestBooking(booker, item);
        booking2.setId(2L);

        BookingResponseDto bookingResponseDto1 = BookingMapper.toBookingResponseDto(booking1);
        BookingResponseDto bookingResponseDto2 = BookingMapper.toBookingResponseDto(booking2);
        List<BookingResponseDto> bookings = List.of(bookingResponseDto1, bookingResponseDto2);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByOwnerAndStatusFutureOrderByStartDesc(anyLong(), anyString(), anyString()))
                .thenReturn(List.of(booking1, booking2));

        // Act
        List<BookingResponseDto> actualBookings = bookingService.findOwnerItems("FUTURE", owner.getId(), 0, 10);

        // Assert
        assertNotNull(actualBookings);
        Assertions.assertEquals(bookings.size(), actualBookings.size());
    }

    @Test
    void findOwnerItemsStateCurrentTest() {
        // Assign
        User owner = user();
        User booker = user();
        booker.setId(2L);
        ItemRequest itemRequest = itemRequest(booker);
        Item item = item(owner, itemRequest);
        Booking booking1 = getTestBooking(booker, item);
        Booking booking2 = getTestBooking(booker, item);
        booking2.setId(2L);

        BookingResponseDto bookingResponseDto1 = BookingMapper.toBookingResponseDto(booking1);
        BookingResponseDto bookingResponseDto2 = BookingMapper.toBookingResponseDto(booking2);
        List<BookingResponseDto> bookings = List.of(bookingResponseDto1, bookingResponseDto2);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByOwnerAndEndIsAfterAndStartIsBefore(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(booking1, booking2));

        // Act
        List<BookingResponseDto> actualBookings = bookingService.findOwnerItems("CURRENT", owner.getId(), 0, 10);

        // Assert
        assertNotNull(actualBookings);
        Assertions.assertEquals(bookings.size(), actualBookings.size());
    }

    @Test
    void findOwnerItemsStatePastTest() {
        // Assign
        User owner = user();
        User booker = user();
        booker.setId(2L);
        ItemRequest itemRequest = itemRequest(booker);
        Item item = item(owner, itemRequest);
        Booking booking1 = getTestBooking(booker, item);
        Booking booking2 = getTestBooking(booker, item);
        booking2.setId(2L);

        BookingResponseDto bookingResponseDto1 = BookingMapper.toBookingResponseDto(booking1);
        BookingResponseDto bookingResponseDto2 = BookingMapper.toBookingResponseDto(booking2);
        List<BookingResponseDto> bookings = List.of(bookingResponseDto1, bookingResponseDto2);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByOwnerAndEndIsBefore(anyLong(), any(LocalDateTime.class)))
                .thenReturn(List.of(booking1, booking2));

        // Act
        List<BookingResponseDto> actualBookings = bookingService.findOwnerItems("PAST", owner.getId(), 0, 10);

        // Assert
        assertNotNull(actualBookings);
        Assertions.assertEquals(bookings.size(), actualBookings.size());
    }

    @Test
    void findOwnerItemsStateWaitingTest() {
        // Assign
        User owner = user();
        User booker = user();
        booker.setId(2L);
        ItemRequest itemRequest = itemRequest(booker);
        Item item = item(owner, itemRequest);
        Booking booking1 = getTestBooking(booker, item);
        Booking booking2 = getTestBooking(booker, item);
        booking2.setId(2L);

        BookingResponseDto bookingResponseDto1 = BookingMapper.toBookingResponseDto(booking1);
        BookingResponseDto bookingResponseDto2 = BookingMapper.toBookingResponseDto(booking2);
        List<BookingResponseDto> bookings = List.of(bookingResponseDto1, bookingResponseDto2);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByItemOwnerAndStatusOrderByStartDesc(anyLong(), anyString()))
                .thenReturn(List.of(booking1, booking2));

        // Act
        List<BookingResponseDto> actualBookings = bookingService.findOwnerItems("WAITING", owner.getId(), 0, 10);

        // Assert
        assertNotNull(actualBookings);
        Assertions.assertEquals(bookings.size(), actualBookings.size());
    }

    @Test
    void findOwnerItemsStateRejectedTest() {
        // Assign
        User owner = user();
        User booker = user();
        booker.setId(2L);
        ItemRequest itemRequest = itemRequest(booker);
        Item item = item(owner, itemRequest);
        Booking booking1 = getTestBooking(booker, item);
        Booking booking2 = getTestBooking(booker, item);
        booking2.setId(2L);

        BookingResponseDto bookingResponseDto1 = BookingMapper.toBookingResponseDto(booking1);
        BookingResponseDto bookingResponseDto2 = BookingMapper.toBookingResponseDto(booking2);
        List<BookingResponseDto> bookings = List.of(bookingResponseDto1, bookingResponseDto2);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByItemOwnerAndStatusOrderByStartDesc(anyLong(), anyString()))
                .thenReturn(List.of(booking1, booking2));

        // Act
        List<BookingResponseDto> actualBookings = bookingService.findOwnerItems("REJECTED", owner.getId(), 0, 10);

        // Assert
        assertNotNull(actualBookings);
        Assertions.assertEquals(bookings.size(), actualBookings.size());
    }

    @Test
    void findOwnerItemsStateUnsupportedTest() {
        // Assign
        User owner = user();
        User booker = user();
        booker.setId(2L);
        ItemRequest itemRequest = itemRequest(booker);
        Item item = item(owner, itemRequest);
        Booking booking1 = getTestBooking(booker, item);
        Booking booking2 = getTestBooking(booker, item);
        booking2.setId(2L);

        BookingResponseDto bookingResponseDto1 = BookingMapper.toBookingResponseDto(booking1);
        BookingResponseDto bookingResponseDto2 = BookingMapper.toBookingResponseDto(booking2);
        List<BookingResponseDto> bookings = List.of(bookingResponseDto1, bookingResponseDto2);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(owner));

        // Act
        UnsupportedStatus exception = assertThrows(UnsupportedStatus.class, () -> {
            bookingService.findOwnerItems("UNSUPPORTED_STATUS", owner.getId(), 0, 10);
        });

        // Assert
        assertEquals("Неправильное именование статуса бронирования.", exception.getMessage());
    }

    private User user() {
        User user = new User();
        user.setId(1L);
        user.setEmail("@");
        user.setName("Test");
        return user;
    }

    private ItemRequest itemRequest(User requestor) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        itemRequest.setDescription("Test Description ItemRequest");
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(LocalDateTime.now());
        return itemRequest;
    }

    private Item item(User owner, ItemRequest itemRequest) {
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
