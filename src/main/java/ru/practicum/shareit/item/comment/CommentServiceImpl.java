package ru.practicum.shareit.item.comment;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.repositories.BookingStorage;
import ru.practicum.shareit.enums.BookingStatus;
import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentStorage commentStorage;

    private final BookingStorage bookingStorage;

    @Override
    public CommentDto save(CommentDto commentDto, long author, long item) {
        if (!bookingStorage.existsBookingByItemIdAndBookerIdAndEndBefore(item, author, LocalDateTime.now())
        || bookingStorage.existsBookingByItemIdAndBookerIdAndStatusLike(item, author, BookingStatus.REJECTED)) {
            throw new ValidationException("Комментарий может оставлять только пользователь, который бронировал данную вещь.");
        }

        Optional<String> authorName = Optional.ofNullable(commentStorage.authorName(author).orElseThrow(NullPointerException::new));
        commentDto.setCreated(LocalDateTime.now());
        commentDto.setItemId(item);

        Comment comment = commentStorage.save(CommentRowMapper.mapToComment(commentDto, author));

        return CommentRowMapper.mapToCommentDto(comment, authorName.get());
    }
}
