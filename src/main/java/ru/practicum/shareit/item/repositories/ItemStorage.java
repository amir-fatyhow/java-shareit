package ru.practicum.shareit.item.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Repository
public interface ItemStorage extends JpaRepository<Item, Long> {

    Page<Item> findAllByOwnerIdOrderByIdAsc(Long userId, Pageable pageable);

    @Query(value = "select * from ITEMS I " +
            "where (lower(I.NAME) LIKE lower(concat('%', :name, '%')) " +
            "OR lower(I.DESCRIPTION) LIKE lower(concat('%', :description, '%'))" +
            "AND I.AVAILABLE = TRUE)", nativeQuery = true)
    Page<Item> findAllByNameAndDescriptionLowerCase(String name, String description, Pageable pageable);

    List<Item> findAllByRequest_IdOrderByRequestIdDesc(long requestId);

}
