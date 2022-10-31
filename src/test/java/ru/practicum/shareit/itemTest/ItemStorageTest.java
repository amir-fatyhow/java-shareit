package ru.practicum.shareit.itemTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repositories.ItemStorage;
import ru.practicum.shareit.request.model.entity.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest // Тестируем JPA
class ItemStorageTest {

    @Autowired
    public TestEntityManager testEntityManager;

    @Autowired
    private ItemStorage itemStorage;

    @Test
    void testFindAllByNameByDescriptionTest() {
        User user1 = new User();
        user1.setName("user1");
        user1.setEmail("user1@email.ru");

        User user2 = new User();
        user2.setName("user2");
        user2.setEmail("user2@email.ru");

        testEntityManager.persist(user1);
        testEntityManager.persist(user2);

        Item item1 = new Item();
        Item item2 = new Item();
        Item item3 = new Item();

        item1.setName("test name1");
        item2.setName("test name1");
        item3.setName("test name3");

        item1.setDescription("test description1");
        item2.setDescription("test description1");
        item3.setDescription("test description3");

        item1.setOwner(user1);
        item2.setOwner(user1);
        item3.setOwner(user2);

        item1.setAvailable(true);
        item2.setAvailable(true);
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

        Page<Item> items = itemStorage.findAllByNameAndDescriptionLowerCase(
                "name1", "description1", PageRequest.of(0, 100));
        assertThat(items).hasSize(2);
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

        Item item1 = new Item();
        Item item2 = new Item();
        Item item3 = new Item();

        item1.setName("test name1");
        item2.setName("test name1");
        item3.setName("test name3");

        item1.setDescription("test description1");
        item2.setDescription("test description1");
        item3.setDescription("test description3");

        item1.setOwner(user3);
        item2.setOwner(user3);
        item3.setOwner(user4);

        item1.setAvailable(true);
        item2.setAvailable(true);
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

        Page<Item> items = itemStorage.findAllByOwnerIdOrderByIdAsc(3L, PageRequest.of(0, 100));
        assertThat(items).hasSize(2);
    }

    @Test
    void testFindAllByRequest_IdOrderByRequestIdDesc() {
        User user5 = new User();
        user5.setName("user5");
        user5.setEmail("user5@email.ru");

        User user6 = new User();
        user6.setName("user6");
        user6.setEmail("user6@email.ru");

        testEntityManager.persist(user5);
        testEntityManager.persist(user6);

        Item item1 = new Item();
        Item item2 = new Item();
        Item item3 = new Item();

        item1.setName("test name1");
        item2.setName("test name1");
        item3.setName("test name3");

        item1.setDescription("test description1");
        item2.setDescription("test description1");
        item3.setDescription("test description3");

        item1.setOwner(user5);
        item2.setOwner(user5);
        item3.setOwner(user6);

        item1.setAvailable(true);
        item2.setAvailable(true);
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

        List<Item> items = itemStorage.findAllByRequest_IdOrderByRequestIdDesc(1L);
        assertThat(items).hasSize(2);
    }

}
