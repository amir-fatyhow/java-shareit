package ru.practicum.shareit.itemTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.entity.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    public TestEntityManager testEntityManager;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void testFindAllByNameByDescriptionTest() {
        User user1 = user();
        User user2 = user();
        user2.setEmail("user2@email.ru");

        testEntityManager.persist(user1);
        testEntityManager.persist(user2);

        Item item1 = item();
        Item item2 = item();
        Item item3 = item();

        item1.setOwner(user1);
        item2.setOwner(user1);
        item3.setOwner(user2);

        item3.setAvailable(false);

        ItemRequest itemRequest1 = new ItemRequest();
        ItemRequest itemRequest2 = new ItemRequest();

        itemRequest1.setRequestor(user2);
        itemRequest2.setRequestor(user1);

        testEntityManager.persist(itemRequest1);
        testEntityManager.persist(itemRequest2);

        item1.setRequest(itemRequest1);
        item2.setRequest(itemRequest1);
        item3.setRequest(itemRequest2);

        testEntityManager.persist(item1);
        testEntityManager.persist(item2);
        testEntityManager.persist(item3);

        Page<Item> items = itemRepository.findAllByNameAndDescriptionLowerCase(
                "name", "description", PageRequest.of(0, 100));
        assertThat(items).hasSize(3);
    }

    @Test
    void testFindAllByOwnerIdOrderById() {
        User user3 = new User();
        user3.setName("user3");
        user3.setEmail("user3@email.ru");

        User user4 = new User();
        user4.setName("user4");
        user4.setEmail("user4@email.ru");

        testEntityManager.persist(user3);
        testEntityManager.persist(user4);

        Item item1 = item();
        Item item2 = item();
        Item item3 = item();

        item1.setOwner(user3);
        item2.setOwner(user3);
        item3.setOwner(user4);

        item3.setAvailable(false);

        ItemRequest itemRequest1 = new ItemRequest();
        ItemRequest itemRequest2 = new ItemRequest();

        itemRequest1.setRequestor(user4);
        itemRequest2.setRequestor(user3);

        testEntityManager.persist(itemRequest1);
        testEntityManager.persist(itemRequest2);

        item1.setRequest(itemRequest1);
        item2.setRequest(itemRequest1);
        item3.setRequest(itemRequest2);

        testEntityManager.persist(item1);
        testEntityManager.persist(item2);
        testEntityManager.persist(item3);

        Page<Item> items = itemRepository.findAllByOwnerIdOrderByIdAsc(3L, PageRequest.of(0, 100));
        assertThat(items).hasSize(2);
    }

    @Test
    void testFindAllByRequest_IdOrderByRequestIdDesc() {
        User user5 = user();
        User user6 = user();
        user6.setEmail("user6@email.ru");

        testEntityManager.persist(user5);
        testEntityManager.persist(user6);

        Item item1 = item();
        Item item2 = item();
        Item item3 = item();

        item1.setOwner(user5);
        item2.setOwner(user5);
        item3.setOwner(user6);

        item3.setAvailable(false);

        ItemRequest itemRequest1 = new ItemRequest();
        ItemRequest itemRequest2 = new ItemRequest();

        itemRequest1.setRequestor(user6);
        itemRequest2.setRequestor(user5);

        testEntityManager.persist(itemRequest1);
        testEntityManager.persist(itemRequest2);

        item1.setRequest(itemRequest1);
        item2.setRequest(itemRequest1);
        item3.setRequest(itemRequest2);

        testEntityManager.persist(item1);
        testEntityManager.persist(item2);
        testEntityManager.persist(item3);

        List<Item> items = itemRepository.findAllByRequestIdOrderByRequestIdDesc(1L);
        assertThat(items).hasSize(2);
    }

    private Item item() {
        Item item = new Item();
        item.setName("test name");
        item.setDescription("test description");
        item.setAvailable(true);
        return item;
    }

    private User user() {
        User user = new User();
        user.setName("user");
        user.setEmail("user@email.ru");
        return user;
    }

}
