package ru.practicum.shareit.item.comment;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "COMMENTS")
@Getter
@Setter
public class Comment {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "TEXT", length = 1024)
    @NotBlank
    @NotNull
    private String text;

    @Column(name = "ITEM_ID")
    private long item;

    @Column(name = "AUTHOR_ID")
    private long author;

    @Column(name = "CREATED")
    private LocalDateTime created;
}

