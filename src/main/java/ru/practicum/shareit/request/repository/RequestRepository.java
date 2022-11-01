package ru.practicum.shareit.request.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.request.model.entity.ItemRequest;
import ru.practicum.shareit.user.model.User;

public interface RequestRepository extends JpaRepository<ItemRequest, Long> {

    Page<ItemRequest> findAllByRequestorIdOrderByCreatedDesc(long user, Pageable pageable);

    Page<ItemRequest> findAllByRequestorNot(User requestor, Pageable pageable);
}
