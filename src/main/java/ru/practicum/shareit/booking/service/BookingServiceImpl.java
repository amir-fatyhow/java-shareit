package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.exception.BookingWrongTime;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repositories.BookingStorage;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.exceptions.ItemNullParametr;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.services.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.services.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingStorage bookingRepository;
    private final UserService userService;
    private final ItemService itemService;

    @Override
    public BookingResponseDto createBooking(BookingDto bookingDto, long userId) throws BookingWrongTime, NotFoundException, ItemNullParametr {
        if (validateDates(bookingDto.getStart(), bookingDto.getEnd())) {
            User booker = userService.checkUser(userId);
            Item currentItem = itemService.checkItem(bookingDto.getItemId());
            if (currentItem.getOwner().equals(booker)) {
                throw new NotFoundException("Booker can't book own Item");
            }
            if (currentItem.getAvailable()) {
                Booking booking = BookingMapper.toBooking(bookingDto);
                booking.setItem(currentItem);
                booking.setBooker(booker);
                Optional<Booking> optionalBooking = bookingRepository.findByItemAndBooker(currentItem, booker);
                if (optionalBooking.isPresent() && optionalBooking.get().getStatus().equals(BookingStatus.WAITING)) {
                    booking.setId(optionalBooking.get().getId());
                }
                if (optionalBooking.isPresent() && optionalBooking.get().getStatus().equals(BookingStatus.REJECTED)) {
                    throw new NotFoundException("Booking REJECTED");
                }
                booking.setStatus(BookingStatus.WAITING);
                bookingRepository.save(booking);
                return BookingMapper.toBookingResponseDto(booking);
            } else {
                throw new ItemNullParametr("Item is unavailable");
            }
        } else {
            throw new BookingWrongTime("Booking wrong Time");
        }

    }

    @Override
    public BookingResponseDto ownerDecision(long bookingId, long ownerId, boolean approved)
            throws NotFoundException, BadRequestException {
        Booking booking = checkBooking(bookingId);
        if (booking.getItem().getOwner().getId().equals(ownerId)) {
            if (booking.getStatus().equals(BookingStatus.WAITING)) {
                if (approved) {
                    booking.setStatus(BookingStatus.APPROVED);
                } else {
                    booking.setStatus(BookingStatus.REJECTED);
                }
                bookingRepository.save(booking);
                return BookingMapper.toBookingResponseDto(booking);
            } else {
                throw new BadRequestException("Status not WAITING");
            }
        } else {
            throw new NotFoundException("This User not Owner for this Item");
        }
    }

    @Override
    public BookingResponseDto getBooking(long bookingId, long userId) throws NotFoundException {
        Booking booking = checkBooking(bookingId);
        User user = userService.checkUser(userId);
        if (booking.getBooker().equals(user) || booking.getItem().getOwner().equals(user)) {
            return BookingMapper.toBookingResponseDto(booking);
        } else {
            throw new NotFoundException("User not Booker and not Owner");
        }
    }

    @Override
    public List<BookingResponseDto> getAllBookingsByBooker(String state, long userId, Integer from, Integer size)
            throws BadRequestException {
        User booker = userService.checkUser(userId);
        if (state.equals("ALL")) {
            if (checkPaging(from, size)) { // Проверяем какие значения пришли, если true - выводим с пагинацией
                if (from != 0) {
                    from -= 1;
                }
                Page<Booking> bookings = bookingRepository.findAllByBookerOrderByStartDesc(
                        booker, PageRequest.of(from, size));
                return bookings.stream().map(BookingMapper::toBookingResponseDto).collect(Collectors.toList());
            } else { // В противном случае выводим условно все!
                return bookingRepository.findAllByBookerOrderByStartDesc(booker, PageRequest.of(0, 100))
                        .stream()
                        .map(BookingMapper::toBookingResponseDto)
                        .collect(Collectors.toList());
            }
        }
        if (state.equals("FUTURE")) {
            return bookingRepository
                    .findAllByBookerAndStatusFutureOrderByStartDesc(userId,
                            BookingStatus.WAITING.name(),
                            BookingStatus.APPROVED.name()).stream()
                    .map(BookingMapper::toBookingResponseDto)
                    .collect(Collectors.toList());
        }
        if (state.equals("CURRENT")) {
            return bookingRepository.findAllByBooker_idAndEndIsAfterAndStartIsBefore(userId,
                            LocalDateTime.now(),
                            LocalDateTime.now().plusSeconds(1)
                    ).stream()
                    .map(BookingMapper::toBookingResponseDto)
                    .collect(Collectors.toList());
        }
        if (state.equals("PAST")) {
            return bookingRepository.findAllByBooker_IdAndEndIsBefore(userId, LocalDateTime.now()).stream()
                    .map(BookingMapper::toBookingResponseDto)
                    .collect(Collectors.toList());
        }
        if (state.equals("WAITING")) {
            return bookingRepository.findAllByBookerAndStatusOrderByStartDesc(userId, BookingStatus.WAITING.name())
                    .stream()
                    .map(BookingMapper::toBookingResponseDto)
                    .collect(Collectors.toList());
        }
        if (state.equals("REJECTED")) {
            return bookingRepository.findAllByBookerAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED.name())
                    .stream()
                    .map(BookingMapper::toBookingResponseDto)
                    .collect(Collectors.toList());
        }
        throw new BadRequestException("Unknown state: " + state);
    }

    @Override
    public List<BookingResponseDto> getAllBookingsByOwner(String state, long userId, Integer from, Integer size)
            throws BadRequestException {
        User owner = userService.checkUser(userId);
        if (state.equals("ALL")) {
            if (checkPaging(from, size)) {
                Page<Booking> bookings = bookingRepository.findAllByItemOwnerOrderByStartDesc(owner, PageRequest.of(from, size));
                return bookings.stream().map(BookingMapper::toBookingResponseDto).collect(Collectors.toList());
            }
            return bookingRepository.findAllByItemOwnerOrderByStartDesc(owner, PageRequest.of(0, 100)).stream()
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
            return bookingRepository.findAllByOwner_IdAndEndIsAfterAndStartIsBefore(userId,
                            LocalDateTime.now(),
                            LocalDateTime.now().plusSeconds(1)
                    ).stream()
                    .map(BookingMapper::toBookingResponseDto)
                    .collect(Collectors.toList());
        }
        if (state.equals("PAST")) {
            return bookingRepository.findAllByOwner_IdAndEndIsBefore(userId, LocalDateTime.now()).stream()
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
        throw new BadRequestException("Unknown state: " + state);
    }

    @Override
    public Booking getLastBookingByItem(long itemId) {
        return bookingRepository.findFirstByItem_idAndEndBeforeOrderByEndDesc(itemId, LocalDateTime.now());
    }

    @Override
    public Booking getNextBookingByItem(long itemId) {
        return bookingRepository.findFirstByItem_idAndStartAfterOrderByStartDesc(itemId, LocalDateTime.now());
    }

    @Override
    public Booking checkBooking(long bookingId) throws NotFoundException {
        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        if (optionalBooking.isPresent()) {
            return optionalBooking.get();
        } else {
            throw new NotFoundException(String.format("Booking by ID: %s - not found", bookingId));
        }
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

    public static boolean validateDates(LocalDateTime start, LocalDateTime end) {
        try {
            LocalDateTime current = LocalDateTime.now();
            return (start.isEqual(current) || start.isAfter(current)) && end.isAfter(start) && end.isAfter(current);
        } catch (DateTimeParseException ex) {
            ex.printStackTrace();
        }
        return false;
    }

}
