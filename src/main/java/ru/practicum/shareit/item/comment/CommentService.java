package ru.practicum.shareit.item.comment;

public interface CommentService {
    CommentDto save(CommentDto commentDto, long author, long item);
}
