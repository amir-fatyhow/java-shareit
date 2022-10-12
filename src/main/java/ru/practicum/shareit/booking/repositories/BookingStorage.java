package ru.practicum.shareit.booking.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.enums.BookingStatus;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Transactional
public interface BookingStorage extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(long bookerId, BookingStatus bookingStatus);

    List<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(long bookerId, LocalDateTime localDateTime);

    List<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(long bookerId, LocalDateTime localDateTime);

    List<Booking> findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(long bookerId, LocalDateTime start, LocalDateTime end);

    List<Booking> findAllByBookerIdOrderByStartDesc(long bookerId);

    List<Booking> findByItemId(long itemId);

    List<Booking> findByItemIdAndStartAfter(long itemId, LocalDateTime localDateTime);

    List<Booking> findByItemIdAndEndBefore(long itemId, LocalDateTime localDateTime);

    List<Booking> findByItemIdAndStartBeforeAndEndAfter(long itemId, LocalDateTime start, LocalDateTime end);

    List<Booking> findByItemIdAndStatus(long itemId, BookingStatus bookingStatus);

    Optional<Booking> findFirstByItemIdAndStartBeforeOrderByStartDesc(long itemId, LocalDateTime now);

    Optional<Booking> findFirstByItemIdAndStartAfterOrderByStart(long itemId, LocalDateTime now);

    boolean existsBookingByItemIdAndBookerIdAndEndBefore(long itemId, long bookerId, LocalDateTime localDateTime);

    boolean existsBookingByItemIdAndBookerIdAndStatusLike(long itemId, long bookerId, BookingStatus bookingStatus);
    
}
