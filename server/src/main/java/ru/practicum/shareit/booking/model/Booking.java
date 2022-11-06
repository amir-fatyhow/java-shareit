package ru.practicum.shareit.booking.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "BOOKINGS")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "START_DATE")
    private LocalDateTime start;

    @Column(name = "END_DATE")
    private LocalDateTime end;

    @ManyToOne
    @JoinColumn(name = "ITEM_ID")
    private Item item;

    @ManyToOne
    @JoinColumn(name = "BOOKER_ID")
    private User booker;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private BookingStatus status;

}
