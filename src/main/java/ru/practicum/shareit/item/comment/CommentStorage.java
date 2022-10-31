package ru.practicum.shareit.item.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentStorage extends JpaRepository<Comment, Long> {

    List<Comment> getCommentsByItem_idOrderByCreatedDesc(long itemId);

    Optional<Comment> findByItem_IdAndAuthor_Id(long itemId, long authorId);
}
