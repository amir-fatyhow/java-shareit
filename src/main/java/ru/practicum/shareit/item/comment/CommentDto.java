package ru.practicum.shareit.item.comment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class CommentDto {

    private long id;

    @NotNull
    @NotBlank
    private String text;

    private long itemId;

    private String authorName;

    private LocalDateTime created;
}
