package ru.practicum.shareit.request.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.request.model.entity.ItemRequest;
import ru.practicum.shareit.user.model.User;

public interface RequestStorage extends JpaRepository<ItemRequest, Long> {

    Page<ItemRequest> findAllByRequestor_IdOrderByCreatedDesc(long user, Pageable pageable);

    @Query(value = "select * from REQUESTS R " +
            "join USERS U ON R.REQUESTOR != U.ID", nativeQuery = true)
    Page<ItemRequest> findAllByRequestorNotContainsOwner(long user, Pageable pageable);

    Page<ItemRequest> findAllByRequestorNot(User requestor, Pageable pageable);
}
