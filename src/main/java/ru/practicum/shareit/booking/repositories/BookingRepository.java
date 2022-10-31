package ru.practicum.shareit.booking.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findAllByBookerOrderByStartDesc(User user, Pageable pageable);

    List<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(long bookerId, LocalDateTime localDateTime);

    List<Booking> findAllByBookerAndStatusOrderByStartDesc(User user, BookingStatus bookingStatus);

    List<Booking> findAllByBookerAndEndIsBefore(User user, LocalDateTime end);

    List<Booking> findAllByBookerAndEndIsAfterAndStartIsBefore(User user, LocalDateTime end, LocalDateTime start);

    @Query(value = "SELECT * FROM BOOKINGS B " +
            "JOIN ITEMS I ON I.ID = B.ITEM_ID " +
            "WHERE I.OWNER_ID = ?1 " +
            "AND B.END_DATE < ?2 " +
            "ORDER BY B.START_DATE DESC ",
            nativeQuery = true)
    List<Booking> findAllByOwnerAndEndIsBefore(long ownerId, LocalDateTime end);

    @Query(value = "SELECT * FROM BOOKINGS B " +
            "JOIN ITEMS I ON I.ID = B.ITEM_ID " +
            "WHERE I.OWNER_ID = ?1 " +
            "AND (B.END_DATE > ?2 AND B.START_DATE < ?3)" +
            "ORDER BY B.START_DATE DESC ",
            nativeQuery = true)
    List<Booking> findAllByOwnerAndEndIsAfterAndStartIsBefore(long ownerId, LocalDateTime end, LocalDateTime start);

    @Query(value = "SELECT * FROM BOOKINGS B " +
            "JOIN ITEMS I ON I.ID = B.ITEM_ID " +
            "WHERE I.OWNER_ID = ?1 " +
            "ORDER BY B.START_DATE DESC ",
            nativeQuery = true)
    Page<Booking> findAllByItemOwner(long userId, Pageable pageable);

    @Query(value = "SELECT * FROM BOOKINGS B " +
            "JOIN ITEMS I ON I.ID = B.ITEM_ID " +
            "WHERE I.OWNER_ID = ?1 " +
            "AND B.STATUS = ?2 " +
            "ORDER BY B.START_DATE DESC",
            nativeQuery = true)
    List<Booking> findAllByItemOwnerAndStatusOrderByStartDesc(long userId, String status);

    @Query(value = "SELECT * FROM BOOKINGS B " +
            "JOIN ITEMS I ON I.ID = B.ITEM_ID " +
            "WHERE I.OWNER_ID = ?1 " +
            "AND (B.STATUS = ?2 OR B.STATUS = ?3)" +
            "ORDER BY B.START_DATE DESC ",
            nativeQuery = true)
    List<Booking> findAllByOwnerAndStatusFutureOrderByStartDesc(long ownerId, String status1, String status2);

    //Last
    Booking findFirstByItemIdAndStartBeforeOrderByStartDesc(long itemId, LocalDateTime end);

    //Next
    Booking findFirstByItemIdAndStartAfterOrderByStart(long itemId, LocalDateTime start);

    boolean existsBookingByItemIdAndBookerIdAndEndBeforeAndStatusNotLike(long itemId, long bookerId, LocalDateTime localDateTime,
                                                                         BookingStatus bookingStatus);

}
