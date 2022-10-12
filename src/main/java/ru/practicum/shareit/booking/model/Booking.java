package ru.practicum.shareit.booking.model;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.enums.BookingStatus;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "BOOKINGS")
@Getter
@Setter
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "START_DATE")
    private LocalDateTime start;

    @Column(name = "END_DATE")
    private LocalDateTime end;

    @Column(name = "ITEM_ID")
    private long itemId;

    @Column(name = "BOOKER_ID")
    private long bookerId;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;
}
