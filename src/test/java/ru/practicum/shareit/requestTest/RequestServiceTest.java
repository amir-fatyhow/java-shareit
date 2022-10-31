package ru.practicum.shareit.requestTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repositories.ItemStorage;
import ru.practicum.shareit.request.model.dto.RequestWithResponseDto;
import ru.practicum.shareit.request.model.entity.ItemRequest;
import ru.practicum.shareit.request.service.RequestService;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
// Очищаем контекст между тестовыми методами
public class RequestServiceTest {

    @Autowired
    private RequestService requestService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ItemStorage itemStorage;

    @Test
    public void getAllResponsesForAllRequestsTest() {
        User owner = getTestUser("1@.ru");
        User booker = getTestUser("2@.ru");
        owner.setId(1L);
        booker.setId(2L);
        ItemRequest itemRequest = getTestItemRequest(booker);
        itemRequest.setId(1L);
        Item item = getTestItem(owner, itemRequest);
        item.setId(1L);

        List<RequestWithResponseDto> result = requestService.getAllResponsesForAllRequests(booker.getId(), 0, 10);

        //TODO Добавить больше Assertions
        Assertions.assertNotNull(result);
        for (RequestWithResponseDto request : result) {
            Assertions.assertNotNull(request);
            Assertions.assertEquals(request.getId(), itemRequest.getId());
            Assertions.assertEquals(request.getItems().size(), 1);
            for (ItemForRequestDto resultItem : request.getItems()) {
                Assertions.assertEquals(resultItem.getId(), item.getId());
                Assertions.assertEquals(resultItem.getName(), item.getName());
            }
        }
    }

    @Test
    public void getAllRequestsOtherUsersTest() {
        User owner = getTestUser("1@.ru");
        User booker = getTestUser("2@.ru");
        owner.setId(1L);
        booker.setId(2L);
        ItemRequest itemRequest1 = getTestItemRequest(booker);
        ItemRequest itemRequest2 = getTestItemRequest(owner);
        itemRequest1.setId(1L);
        itemRequest2.setId(2L);
        Item item1 = getTestItem(owner, itemRequest1);
        Item item2 = getTestItem(booker, itemRequest2);
        item1.setId(1L);
        item2.setId(2L);

        List<RequestWithResponseDto> result = requestService.getAllRequestsOtherUsers(booker.getId(), 0, 10);

        Assertions.assertNotNull(result);
        for (RequestWithResponseDto request : result) {
            Assertions.assertNotNull(request);
            Assertions.assertEquals(request.getId(), itemRequest2.getId());
            for (ItemForRequestDto resultItem : request.getItems()) {
                Assertions.assertEquals(resultItem.getId(), item2.getId());
                Assertions.assertEquals(resultItem.getName(), item2.getName());
            }
        }
    }

    private ItemRequest getTestItemRequest(User user) {
        ItemRequest itemRequest = new ItemRequest();
        // Не сетим id
        itemRequest.setDescription("Test ItemRequest Description");
        itemRequest.setRequestor(user);
        itemRequest.setCreated(LocalDateTime.now());
        entityManager.persist(itemRequest);
        return itemRequest;
    }

    private User getTestUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setName("Test User Name");
        entityManager.persist(user);
        return user;
    }

    private Item getTestItem(User user, ItemRequest itemRequest) {
        Item item = new Item();
        item.setName("Test Item Name 1");
        item.setDescription("Test Item Description 1");
        item.setOwner(user);
        item.setRequest(itemRequest);
        entityManager.persist(item);
        return item;
    }
}
