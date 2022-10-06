package ru.practicum.shareit.item.comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "COMMENTS")
@Data
public class Comment {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "TEXT")
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

