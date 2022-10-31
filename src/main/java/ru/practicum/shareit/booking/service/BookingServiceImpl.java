package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repositories.BookingRepository;
import ru.practicum.shareit.exception.ShareItNotFoundException;
import ru.practicum.shareit.exception.UnsupportedStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.pageable.FromSizeRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private static final String ITEM_NOT_FOUND = "Вещь с указанным Id не существует.";
    private static final String USER_NOT_FOUND = "Пользователя с указанным Id не существует.";
    private static final String BOOKING_NOT_FOUND = "Бронирование вещи с указанным Id не существует.";

    @Override
    public BookingResponseDto save(BookingDto bookingDto, long userId) throws ShareItNotFoundException, ValidationException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ShareItNotFoundException(USER_NOT_FOUND));

        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new ShareItNotFoundException(ITEM_NOT_FOUND));

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь для бронирования недоступна.");
        }
        if (bookingDto.getEnd().isBefore(bookingDto.getStart())) {
            throw new ValidationException("Ошибка в дате бронирования.");
        }
        if (item.getOwner().equals(user)) {
            throw new ShareItNotFoundException("Пользователь вещи не может ее забронировать.");
        }

        Booking booking = BookingMapper.toBooking(bookingDto);
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(BookingStatus.WAITING);

        booking = bookingRepository.save(booking);

        return BookingMapper.toBookingResponseDto(booking);
    }

    @Override
    public BookingResponseDto update(long bookingId, long ownerId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ShareItNotFoundException(BOOKING_NOT_FOUND));

        if (booking.getItem().getOwner().getId() != ownerId) {
            throw new ShareItNotFoundException("Менять статус может только владелец.");
        }
        if (approved) {
            if (booking.getStatus() == BookingStatus.APPROVED) {
                throw new ValidationException("Второе подтверждение статуса.");
            }
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }
        booking = bookingRepository.save(booking);
        return BookingMapper.toBookingResponseDto(booking);
    }

    @Override
    public BookingResponseDto findById(long bookingId, long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ShareItNotFoundException(BOOKING_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ShareItNotFoundException(USER_NOT_FOUND));

        if (booking.getBooker().equals(user) || booking.getItem().getOwner().equals(user)) {
            return BookingMapper.toBookingResponseDto(booking);
        } else {
            throw new ShareItNotFoundException("Просматривать вещь может или пользователь, " +
                    "который ее забронировал, или ее владелец.");
        }
    }

    @Override
    public List<BookingResponseDto> findByState(String state, long userId, Integer from, Integer size) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new ShareItNotFoundException(USER_NOT_FOUND));

        if (state.equals("ALL")) {
            Pageable pageable = FromSizeRequest.of(from, size);
            return bookingRepository.findAllByBookerOrderByStartDesc(booker, pageable)
                    .stream()
                    .map(BookingMapper::toBookingResponseDto)
                    .collect(Collectors.toList());
        }
        if (state.equals("FUTURE")) {
            return bookingRepository
                    .findAllByBookerIdAndStartAfterOrderByStartDesc(userId,
                            LocalDateTime.now()).stream()
                    .map(BookingMapper::toBookingResponseDto)
                    .collect(Collectors.toList());
        }
        if (state.equals("CURRENT")) {
            return bookingRepository.findAllByBookerAndEndIsAfterAndStartIsBefore(booker,
                            LocalDateTime.now(),
                            LocalDateTime.now().plusSeconds(1)
                    ).stream()
                    .map(BookingMapper::toBookingResponseDto)
                    .collect(Collectors.toList());
        }
        if (state.equals("PAST")) {
            return bookingRepository.findAllByBookerAndEndIsBefore(booker, LocalDateTime.now()).stream()
                    .map(BookingMapper::toBookingResponseDto)
                    .collect(Collectors.toList());
        }
        if (state.equals("WAITING")) {
            return bookingRepository.findAllByBookerAndStatusOrderByStartDesc(booker, BookingStatus.WAITING)
                    .stream()
                    .map(BookingMapper::toBookingResponseDto)
                    .collect(Collectors.toList());
        }
        if (state.equals("REJECTED")) {
            return bookingRepository.findAllByBookerAndStatusOrderByStartDesc(booker, BookingStatus.REJECTED)
                    .stream()
                    .map(BookingMapper::toBookingResponseDto)
                    .collect(Collectors.toList());
        }
        throw new UnsupportedStatus("Неправильное именование статуса бронирования.");
    }

    @Override
    public List<BookingResponseDto> findOwnerItems(String state, long userId, Integer from, Integer size) {
        if (!userRepository.existsById(userId)) {
            throw new ShareItNotFoundException(USER_NOT_FOUND);
        }

        if (state.equals("ALL")) {
            Pageable pageable = FromSizeRequest.of(from, size);
            return bookingRepository.findAllByItemOwner(userId, pageable).stream()
                    .map(BookingMapper::toBookingResponseDto)
                    .collect(Collectors.toList());
        }
        if (state.equals("FUTURE")) {
            return bookingRepository.findAllByOwnerAndStatusFutureOrderByStartDesc(userId,
                            BookingStatus.APPROVED.name(),
                            BookingStatus.WAITING.name()).stream()
                    .map(BookingMapper::toBookingResponseDto)
                    .collect(Collectors.toList());
        }
        if (state.equals("CURRENT")) {
            return bookingRepository.findAllByOwnerAndEndIsAfterAndStartIsBefore(userId,
                            LocalDateTime.now(),
                            LocalDateTime.now().plusSeconds(1)
                    ).stream()
                    .map(BookingMapper::toBookingResponseDto)
                    .collect(Collectors.toList());
        }
        if (state.equals("PAST")) {
            return bookingRepository.findAllByOwnerAndEndIsBefore(userId, LocalDateTime.now()).stream()
                    .map(BookingMapper::toBookingResponseDto)
                    .collect(Collectors.toList());
        }
        if (state.equals("WAITING")) {
            return bookingRepository.findAllByItemOwnerAndStatusOrderByStartDesc(userId, BookingStatus.WAITING.name()).stream()
                    .map(BookingMapper::toBookingResponseDto)
                    .collect(Collectors.toList());
        }
        if (state.equals("REJECTED")) {
            return bookingRepository.findAllByItemOwnerAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED.name()).stream()
                    .map(BookingMapper::toBookingResponseDto)
                    .collect(Collectors.toList());
        }
        throw new UnsupportedStatus("Неправильное именование статуса бронирования.");
    }

}
