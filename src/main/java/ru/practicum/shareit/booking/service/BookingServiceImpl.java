package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingLastOrNextDto;
import ru.practicum.shareit.booking.dto.BookingRowMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repositories.BookingStorage;
import ru.practicum.shareit.enums.BookingStatus;
import ru.practicum.shareit.exception.ShareItNotFoundException;
import ru.practicum.shareit.exception.UnsupportedStatus;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentStorage;
import ru.practicum.shareit.item.comment.CommentRowMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemRowMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repositories.ItemStorage;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserRowMapper;
import ru.practicum.shareit.user.repositories.UserStorage;

import javax.transaction.Transactional;
import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final UserStorage userStorage;
    private final ItemStorage itemStorage;
    private final BookingStorage bookingStorage;
    private final CommentStorage commentStorage;

    @Override
    public BookingDto save(BookingDto bookingDto, long bookerId) {
        if (!userStorage.existsById(bookerId)) {
            throw new ShareItNotFoundException("Пользователя с указанным Id не существует.");
        }
        if (!itemStorage.findById(bookingDto.getItemId()).orElseThrow(() -> new ShareItNotFoundException("Вещь с указанным Id не существует.")).getAvailable()) {
            throw new ValidationException("Вещь для бронирования недоступна.");
        }
        if (!itemStorage.existsById(bookingDto.getItemId())) {
            throw new ShareItNotFoundException("Вещь с указанным Id не существует.");
        }
        if (bookingDto.getEnd().isBefore(LocalDateTime.now()) || bookingDto.getEnd().isBefore(bookingDto.getStart()) ||
                bookingDto.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Ошибка в дате бронирования.");
        }
        if (itemStorage.findById(bookingDto.getItemId()).get().getOwnerId() == bookerId) {
            throw new ShareItNotFoundException("Пользователь вещи не может ее забронировать.");
        }

        Booking booking = bookingStorage.save(BookingRowMapper.toBooking(bookingDto, bookerId));

        return BookingRowMapper.toBookingDto(
                booking,
                ItemRowMapper.toItemDto(
                        itemStorage.findById(booking.getItemId()).orElseThrow(() -> new ShareItNotFoundException("Вещь с указанным Id не существует.")),
                        new BookingLastOrNextDto(),
                        new BookingLastOrNextDto(), getCommentDtos(booking.getItemId())),
                UserRowMapper.toUserDto(
                        userStorage.findById(booking.getBookerId()).orElseThrow(() -> new ShareItNotFoundException("Пользователя с указанным Id не существует.")))
        );
    }

    @Override
    public BookingDto update(long bookingId, long ownerId, boolean approved) {
        Booking booking = bookingStorage.findById(bookingId).orElseThrow(() -> new ShareItNotFoundException("Вещь с указанным Id не существует."));
        if (itemStorage.findById(booking.getItemId()).get().getOwnerId() != ownerId) {
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
        bookingStorage.save(booking);

        return BookingRowMapper.toBookingDto(
                booking,
                getItemDto(booking),
                UserRowMapper.toUserDto(Optional.of(userStorage
                                .findById(booking.getBookerId())
                                .orElseThrow(() -> new ShareItNotFoundException("Пользователя с указанным Id не существует.")))
                        .get())
        );
    }

    @Override
    public BookingDto findById(long bookingId, long userId) {
        if (!userStorage.existsById(userId)) {
            throw new ShareItNotFoundException("Пользователя с указанным Id не существует.");
        }
        Booking booking = bookingStorage.findById(bookingId).orElseThrow(() -> new ShareItNotFoundException("Вещь с указанным Id не существует."));

        if (booking.getBookerId() != userId &&
                itemStorage.findById(booking.getItemId()).get().getOwnerId() != userId) {
            throw new ShareItNotFoundException("Просматривать вещь может или пользователь, который ее забронировал, или ее владелец.");
        }

        return BookingRowMapper.toBookingDto(booking,
                getItemDto(booking),
                UserRowMapper.toUserDto(
                        Optional.of(userStorage
                                .findById(booking.getBookerId())
                                .orElseThrow(() -> new ShareItNotFoundException("Пользователя с указанным Id не существует."))).get()
                ));

    }

    @Override
    public List<BookingDto> findByState(long userId, String state) {
        if (!userStorage.existsById(userId)) {
            throw new ShareItNotFoundException("Пользователя с указанным Id не существует.");
        }
        BookingStatus stateStatus;
        try {
            stateStatus = BookingStatus.valueOf(state);
        } catch (Exception e) {
            throw new UnsupportedStatus("Неправильное именование статуса бронирования.");
        }

        List<Booking> bookings;
        List<BookingDto> bookingDtos = new ArrayList<>();
        switch (stateStatus) {

            case ALL:
                bookings = bookingStorage.findAllByBookerIdOrderByStartDesc(userId);
                return getBookingDtosForBooker(bookings, bookingDtos, userId);

            case WAITING:
                bookings = bookingStorage.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
                return getBookingDtosForBooker(bookings, bookingDtos, userId);

            case REJECTED:
                bookings = bookingStorage.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
                return getBookingDtosForBooker(bookings, bookingDtos, userId);

            case FUTURE:
                bookings = bookingStorage.findAllByBookerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now());
                return getBookingDtosForBooker(bookings, bookingDtos, userId);

            case PAST:
                bookings = bookingStorage.findAllByBookerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now());
                return getBookingDtosForBooker(bookings, bookingDtos, userId);

            case CURRENT:
                bookings = bookingStorage.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, LocalDateTime.now(), LocalDateTime.now());
                return getBookingDtosForBooker(bookings, bookingDtos, userId);
        }
        return null;
    }

    @Override
    public List<BookingDto> findOwnerItems(long ownerId, String state) {
        if (!userStorage.existsById(ownerId)) {
            throw new ShareItNotFoundException("Пользователя с указанным Id не существует.");
        }
        BookingStatus stateStatus;
        try {
            stateStatus = BookingStatus.valueOf(state);
        } catch (Exception e) {
            throw new UnsupportedStatus("Неправильное именование статуса бронирования.");
        }

        List<Item> items = itemStorage.findAllByOwnerId(ownerId);
        List<Booking> bookings = new ArrayList<>();
        List<BookingDto> bookingDtos = new ArrayList<>();
        switch (stateStatus) {
            case ALL:
                for (Item item : items) {
                    if (item.getOwnerId() == ownerId) {
                        bookings.addAll(bookingStorage.findByItemId(item.getId()));
                    }
                }
                return getBookingDtosForOwner(bookings, bookingDtos).stream().sorted(Comparator.comparing(BookingDto::getStart).reversed())
                        .collect(Collectors.toList());

            case WAITING:
                for (Item item : items) {
                    if (item.getOwnerId() == ownerId) {
                        bookings.addAll(bookingStorage.findByItemIdAndStatus(item.getId(), BookingStatus.WAITING));
                    }
                }
                return getBookingDtosForOwner(bookings, bookingDtos).stream().sorted(Comparator.comparing(BookingDto::getStart).reversed())
                        .collect(Collectors.toList());

            case REJECTED:
                for (Item item : items) {
                    if (item.getOwnerId() == ownerId) {
                        bookings.addAll(bookingStorage.findByItemIdAndStatus(item.getId(), BookingStatus.REJECTED));
                    }
                }
                return getBookingDtosForOwner(bookings, bookingDtos).stream().sorted(Comparator.comparing(BookingDto::getStart).reversed())
                        .collect(Collectors.toList());

            case FUTURE:
                for (Item item : items) {
                    if (item.getOwnerId() == ownerId) {
                        bookings.addAll(bookingStorage.findByItemIdAndStartAfter(item.getId(), LocalDateTime.now()));
                    }
                }
                return getBookingDtosForOwner(bookings, bookingDtos).stream().sorted(Comparator.comparing(BookingDto::getStart).reversed())
                        .collect(Collectors.toList());

            case PAST:
                for (Item item : items) {
                    if (item.getOwnerId() == ownerId) {
                        bookings.addAll(bookingStorage.findByItemIdAndEndBefore(item.getId(), LocalDateTime.now()));
                    }
                }
                return getBookingDtosForOwner(bookings, bookingDtos).stream().sorted(Comparator.comparing(BookingDto::getStart).reversed())
                        .collect(Collectors.toList());

            case CURRENT:
                for (Item item : items) {
                    if (item.getOwnerId() == ownerId) {
                        bookings.addAll(bookingStorage.findByItemIdAndStartBeforeAndEndAfter(item.getId(), LocalDateTime.now(), LocalDateTime.now()));
                    }
                }
                return getBookingDtosForOwner(bookings, bookingDtos).stream().sorted(Comparator.comparing(BookingDto::getStart).reversed())
                        .collect(Collectors.toList());
        }
        return null;
    }

    private ArrayList<CommentDto> getCommentDtos(long itemId) {
        ArrayList<CommentDto> commentDtos = new ArrayList<>();
        List<Comment> comments = commentStorage.findAllByItem(itemId);
        for (Comment comment : comments) {
            commentDtos.add(CommentRowMapper.mapToCommentDto(comment, commentStorage.authorName(comment.getAuthor()).get()));
        }
        return commentDtos;
    }

    private ItemDto getItemDto(Booking booking) {
        Item item = itemStorage.findById(booking.getItemId()).orElseThrow(() -> new ShareItNotFoundException("Вещь с указанным Id не существует."));

        Optional<Booking> bookingLast = bookingStorage.findFirstByItemIdAndStartBeforeOrderByStartDesc(item.getId(), LocalDateTime.now());
        Optional<Booking> bookingNext = bookingStorage.findFirstByItemIdAndStartAfterOrderByStart(item.getId(), LocalDateTime.now());

        ItemDto itemDto;
        if (bookingLast.isPresent() && bookingNext.isPresent()) {
            itemDto = ItemRowMapper.toItemDto(
                    item,
                    BookingRowMapper.toBookingLastAndNext(bookingLast.get()),
                    BookingRowMapper.toBookingLastAndNext(bookingNext.get()),
                    getCommentDtos(booking.getItemId())
            );
        } else {
            itemDto = ItemRowMapper.toItemDto(
                    item,
                    new BookingLastOrNextDto(),
                    new BookingLastOrNextDto(),
                    getCommentDtos(booking.getItemId())
            );
        }
        return itemDto;
    }

    private List<BookingDto> getBookingDtosForBooker(List<Booking> bookings, List<BookingDto> bookingDtos, long userId) {
        for (Booking booking : bookings) {
            UserDto userDto = UserRowMapper.toUserDto(userStorage.findById(userId).orElseThrow(() -> new ShareItNotFoundException("Пользователь с указанным Id не существует.")));
            bookingDtos.add(BookingRowMapper.toBookingDto(booking, getItemDto(booking), userDto));
        }
        return bookingDtos;
    }

    private List<BookingDto> getBookingDtosForOwner(List<Booking> bookings, List<BookingDto> bookingDtos) {
        for (Booking booking : bookings) {
            UserDto userDto = UserRowMapper.toUserDto(userStorage.findById(booking.getBookerId()).orElseThrow(() -> new ShareItNotFoundException("Вещь с указанным Id не существует.")));
            bookingDtos.add(BookingRowMapper.toBookingDto(booking, getItemDto(booking), userDto));
        }
        return bookingDtos;
    }

}
