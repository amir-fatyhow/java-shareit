package ru.practicum.shareit.itemTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.services.ItemService;
import ru.practicum.shareit.request.model.entity.ItemRequest;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDateTime;

@SpringBootTest
@Transactional
public class ItemServiceTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findItemByIdTest() {
        User user1 = getTestUser("user1@");
        User user2 = getTestUser("user2@");
        user1.setId(1L);
        user2.setId(2L);
        ItemRequest itemRequest1 = getTestItemRequest(user1);
        ItemRequest itemRequest2 = getTestItemRequest(user2);
        Item item1 = getTestItem(user1, itemRequest1);
        Item item2 = getTestItem(user2, itemRequest2);
        item1.setId(1L);
        item2.setId(2L);

        ItemResponseDto itemResponseDto = itemService.findItemById(item1.getId(), user1.getId());

        Assertions.assertNotNull(itemResponseDto);
        Assertions.assertEquals(itemResponseDto.getId(), item1.getId());
        Assertions.assertEquals(itemResponseDto.getName(), item1.getName());
        Assertions.assertEquals(itemResponseDto.getDescription(), item1.getDescription());
    }

    User getTestUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setName("Test Name");
        entityManager.persist(user);
        return user;
    }

    ItemRequest getTestItemRequest(User user) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription("Description");
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setRequestor(user);
        entityManager.persist(itemRequest);
        return itemRequest;
    }

    Item getTestItem(User user, ItemRequest itemRequest) {
        Item item = new Item();
        item.setName("TestName2");
        item.setDescription("Description2");
        item.setAvailable(Boolean.TRUE);
        item.setOwner(user);
        item.setRequest(itemRequest);
        entityManager.persist(item);
        return item;
    }
}
