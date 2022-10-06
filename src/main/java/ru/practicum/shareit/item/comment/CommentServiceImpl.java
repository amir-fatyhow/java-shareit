package ru.practicum.shareit.item.comment;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.enums.BookingStatus;
import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;

    private final BookingRepository bookingRepository;

    @Override
    public CommentDto save(CommentDto commentDto, long author, long item) {
        if (!bookingRepository.existsBookingByItemIdAndBookerIdAndEndBefore(item, author, LocalDateTime.now())
        || bookingRepository.existsBookingByItemIdAndBookerIdAndStatusLike(item, author, BookingStatus.REJECTED)) {
            throw new ValidationException("Комментарий может оставлять только пользователь, который бронировал данную вещь.");
        }

        Optional<String> authorName = Optional.ofNullable(commentRepository.authorName(author).orElseThrow(NullPointerException::new));
        commentDto.setCreated(LocalDateTime.now());
        commentDto.setItemId(item);

        Comment comment = commentRepository.save(CommentRowMapper.mapToComment(commentDto, author));

        return CommentRowMapper.mapToCommentDto(comment, authorName.get());
    }
}
