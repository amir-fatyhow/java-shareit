package ru.practicum.shareit.item.comment;

public class CommentRowMapper {
    public static Comment mapToComment(CommentDto commentDto, long author) {
        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(commentDto.getItemId());
        comment.setAuthor(author);
        comment.setCreated(commentDto.getCreated());
        return comment;
    }

    public static CommentDto mapToCommentDto(Comment comment, String authorName) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getItem(),
                authorName,
                comment.getCreated()
        );
    }
}
