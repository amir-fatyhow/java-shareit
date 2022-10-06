package ru.practicum.shareit.item.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerId(long ownerId);

    @Query(value = "select * from ITEMS I " +
            "where (lower(I.NAME) LIKE lower(concat('%', :name, '%')) " +
            "OR lower(I.DESCRIPTION) LIKE lower(concat('%', :description, '%'))" +
            "AND I.IS_AVAILABLE = TRUE)", nativeQuery = true)
    List<Item> search(String name, String description);
}
