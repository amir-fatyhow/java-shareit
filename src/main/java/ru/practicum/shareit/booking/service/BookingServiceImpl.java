package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingLastOrNextDto;
import ru.practicum.shareit.booking.dto.BookingRowMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.enums.BookingStatus;
import ru.practicum.shareit.exception.UnsupportedStatus;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.comment.CommentRowMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemRowMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserRowMapper;
import ru.practicum.shareit.user.repository.UserRepository;
import javax.transaction.Transactional;
import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService{
    private final UserRepository userRepository;

    private final ItemRepository itemRepository;

    private final BookingRepository bookingRepository;

    private final CommentRepository commentRepository;

    @Override
    public BookingDto save(BookingDto bookingDto, long bookerId) {
        if (!userRepository.existsById(bookerId)) {
            throw new NullPointerException("Пользователя с указанным Id не существует.");
        }
        if (!itemRepository.findById(bookingDto.getItemId()).orElseThrow(NullPointerException::new).getAvailable()) {
            throw new ValidationException("Вещь для бронирования недоступна.");
        }
        if (!itemRepository.existsById(bookingDto.getItemId())) {
            throw new NullPointerException("Вещь с указанным Id не существует.");
        }
        if (bookingDto.getEnd().isBefore(LocalDateTime.now()) || bookingDto.getEnd().isBefore(bookingDto.getStart()) ||
            bookingDto.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Ошибка в дате бронирования.");
        }
        if (itemRepository.findById(bookingDto.getItemId()).get().getOwnerId() == bookerId) {
            throw new NullPointerException("Пользователь вещи не может ее забронировать.");
        }

        Booking booking = bookingRepository.save(BookingRowMapper.mapToBooking(bookingDto, bookerId));

        return BookingRowMapper.mapToBookingDto(
                booking,
                ItemRowMapper.mapToItemDto(
                        Optional.of(itemRepository.findById(booking.getItemId()).orElseThrow(NullPointerException::new)).get(),
                        new BookingLastOrNextDto(),
                        new BookingLastOrNextDto(), getCommentDtos(booking.getItemId())),
                UserRowMapper.mapToUserDto(
                        Optional.of(userRepository.findById(booking.getBookerId()).orElseThrow(NullPointerException::new)).get())
        );
    }

    @Override
    public BookingDto update(long bookingId, long ownerId, boolean approved) {
        Optional<Booking> booking = Optional.of(bookingRepository.findById(bookingId).orElseThrow(NullPointerException::new));
        if (itemRepository.findById(booking.get().getItemId()).get().getOwnerId() != ownerId) {
            throw new NullPointerException("Менять статус может только владелец.");
        }
        if (approved) {
            if (booking.get().getStatus().equals(BookingStatus.APPROVED)) {
                throw new ValidationException("Второе подтверждение статуса.");
            }
            booking.get().setStatus(BookingStatus.APPROVED);
        } else {
            booking.get().setStatus(BookingStatus.REJECTED);
        }
        bookingRepository.save(booking.get());

        return BookingRowMapper.mapToBookingDto(
                booking.get(),
                getItemDto(booking.get()),
                UserRowMapper.mapToUserDto(Optional.of(userRepository
                                                        .findById(booking
                                                        .get()
                                                        .getBookerId())
                                                        .orElseThrow(NullPointerException::new))
                                                        .get())
        );
    }

    @Override
    public BookingDto findById(long bookingId, long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NullPointerException("Пользователя с указанным Id не существует.");
        }
        Optional<Booking> booking = Optional.of(bookingRepository.findById(bookingId).orElseThrow(NullPointerException::new));

        if (booking.get().getBookerId() != userId &&
            itemRepository.findById(booking.get().getItemId()).get().getOwnerId() != userId) {
            throw new NullPointerException("");
        }

        return  BookingRowMapper.mapToBookingDto(booking.get(),
                                                getItemDto(booking.get()),
                                                UserRowMapper.mapToUserDto(
                                                        Optional.of(userRepository
                                                        .findById(booking.get().getBookerId())
                                                        .orElseThrow(NullPointerException::new)).get()
                                                ));

    }

    @Override
    public List<BookingDto> findByState(long userId, String state) {
        if (!userRepository.existsById(userId)) {
            throw new NullPointerException("Пользователя с указанным Id не существует.");
        }
        List<Booking> bookings;
        List<BookingDto> bookingDtos = new ArrayList<>();
        switch (state) {

            case "ALL":
                bookings = bookingRepository.findAllByBookerIdOrderByStartDesc(userId);
                return getBookingDtosForBooker(bookings, bookingDtos, userId);

            case "WAITING":
                bookings = bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
                return getBookingDtosForBooker(bookings, bookingDtos, userId);

            case "REJECTED":
                bookings = bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
                return getBookingDtosForBooker(bookings, bookingDtos, userId);

            case "FUTURE":
                bookings = bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now());
                return getBookingDtosForBooker(bookings, bookingDtos, userId);

            case "PAST":
                bookings = bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now());
                return getBookingDtosForBooker(bookings, bookingDtos, userId);

            case "CURRENT":
                bookings = bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, LocalDateTime.now(), LocalDateTime.now());
                return getBookingDtosForBooker(bookings, bookingDtos, userId);

            default:
                throw new UnsupportedStatus("");
        }
    }
    @Override
    public List<BookingDto> findOwnerItems(long ownerId, String state) {
        if (!userRepository.existsById(ownerId)) {
            throw new NullPointerException("Пользователя с указанным Id не существует.");
        }
        List<Item> items = itemRepository.findAllByOwnerId(ownerId);
        List<Booking> bookings = new ArrayList<>();
        List<BookingDto> bookingDtos = new ArrayList<>();
        switch (state) {
            case "ALL":
                for (Item item : items) {
                    if (item.getOwnerId() == ownerId) {
                        bookings.addAll(bookingRepository.findByItemId(item.getId()));
                    }
                }
                return getBookingDtosForOwner(bookings, bookingDtos).stream().sorted(Comparator.comparing(BookingDto::getStart).reversed())
                        .collect(Collectors.toList());

            case "WAITING":
                for (Item item : items) {
                    if (item.getOwnerId() == ownerId) {
                        bookings.addAll(bookingRepository.findByItemIdAndStatus(item.getId(), BookingStatus.WAITING));
                    }
                }
                return getBookingDtosForOwner(bookings, bookingDtos).stream().sorted(Comparator.comparing(BookingDto::getStart).reversed())
                        .collect(Collectors.toList());

            case "REJECTED":
                for (Item item : items) {
                    if (item.getOwnerId() == ownerId) {
                        bookings.addAll(bookingRepository.findByItemIdAndStatus(item.getId(), BookingStatus.REJECTED));
                    }
                }
                return getBookingDtosForOwner(bookings, bookingDtos).stream().sorted(Comparator.comparing(BookingDto::getStart).reversed())
                        .collect(Collectors.toList());

            case "FUTURE":
                for (Item item : items) {
                    if (item.getOwnerId() == ownerId) {
                        bookings.addAll(bookingRepository.findByItemIdAndStartAfter(item.getId(), LocalDateTime.now()));
                    }
                }
                return getBookingDtosForOwner(bookings, bookingDtos).stream().sorted(Comparator.comparing(BookingDto::getStart).reversed())
                        .collect(Collectors.toList());

            case "PAST":
                for (Item item : items) {
                    if (item.getOwnerId() == ownerId) {
                        bookings.addAll(bookingRepository.findByItemIdAndEndBefore(item.getId(), LocalDateTime.now()));
                    }
                }
                return getBookingDtosForOwner(bookings, bookingDtos).stream().sorted(Comparator.comparing(BookingDto::getStart).reversed())
                        .collect(Collectors.toList());

            case "CURRENT":
                for (Item item : items) {
                    if (item.getOwnerId() == ownerId) {
                        bookings.addAll(bookingRepository.findByItemIdAndStartBeforeAndEndAfter(item.getId(), LocalDateTime.now(), LocalDateTime.now()));
                    }
                }
                return getBookingDtosForOwner(bookings, bookingDtos).stream().sorted(Comparator.comparing(BookingDto::getStart).reversed())
                        .collect(Collectors.toList());
            default:
                throw new UnsupportedStatus("");
        }
    }

    private  ArrayList<CommentDto> getCommentDtos(long itemId) {
        ArrayList<CommentDto> commentDtos = new ArrayList<>();
        List<Comment> comments = commentRepository.findAllByItem(itemId);
        for (Comment comment : comments) {
            commentDtos.add(CommentRowMapper.mapToCommentDto(comment, commentRepository.authorName(comment.getAuthor()).get()));
        }
        return commentDtos;
    }

    private ItemDto getItemDto(Booking booking) {
        Item item = Optional.of(itemRepository.findById(booking.getItemId()).orElseThrow(NullPointerException::new)).get();

        Optional<Booking> bookingLast = bookingRepository.findFirstByItemIdAndStartBeforeOrderByStartDesc(item.getId(), LocalDateTime.now());
        Optional<Booking> bookingNext = bookingRepository.findFirstByItemIdAndStartAfterOrderByStart(item.getId(), LocalDateTime.now());

        ItemDto itemDto;
        if (bookingLast.isPresent() && bookingNext.isPresent()) {
            itemDto = ItemRowMapper.mapToItemDto(
                    item,
                    BookingRowMapper.mapToBookingLastAndNext(bookingLast.get()),
                    BookingRowMapper.mapToBookingLastAndNext(bookingNext.get()),
                    getCommentDtos(booking.getItemId())
            );
        } else {
            itemDto = ItemRowMapper.mapToItemDto(
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
            UserDto userDto = UserRowMapper.mapToUserDto(Optional.of(userRepository.findById(userId).orElseThrow(NullPointerException::new)).get());
            bookingDtos.add(BookingRowMapper.mapToBookingDto(booking, getItemDto(booking), userDto));
        }
        return bookingDtos;
    }

    private List<BookingDto> getBookingDtosForOwner(List<Booking> bookings, List<BookingDto> bookingDtos ) {
        for (Booking booking : bookings) {
            UserDto userDto = UserRowMapper.mapToUserDto(Optional.of(userRepository.findById(booking.getBookerId()).orElseThrow(NullPointerException::new)).get());
            bookingDtos.add(BookingRowMapper.mapToBookingDto(booking, getItemDto(booking), userDto));
        }
        return bookingDtos;
    }

}
