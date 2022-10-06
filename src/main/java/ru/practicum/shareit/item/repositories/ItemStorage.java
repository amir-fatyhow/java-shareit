package ru.practicum.shareit.item.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Repository
public interface ItemStorage extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerId(long ownerId);

    @Query(value = "SELECT * " +
            "FROM ITEMS AS I " +
            "WHERE (I.NAME ~* ?1 " +
            "OR I.DESCRIPTION ~* ?1)" +
            "AND I.IS_AVAILABLE = true",
            nativeQuery = true)
    List<Item> search(String text);
}
