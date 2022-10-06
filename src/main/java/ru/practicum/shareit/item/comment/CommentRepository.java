package ru.practicum.shareit.item.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.ArrayList;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment,Long> {
    @Query(value = "SELECT U.NAME " +
            "FROM USERS AS U "+
            "WHERE U.ID = ?1 ",
            nativeQuery = true)
    Optional<String> authorName(long userId);

    ArrayList<Comment> findAllByItem(long itemId);
}
