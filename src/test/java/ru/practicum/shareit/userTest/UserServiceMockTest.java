package ru.practicum.shareit.userTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceMockTest {
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        userService = new UserServiceImpl(userRepository, objectMapper);
    }

    @Test
    void save() {
        // Assign
        User user = getTestUser();
        UserDto userDto = getTestUserDto();
        user.setId(userDto.getId());
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());

        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        UserDto result = userService.save(userDto);

        // Assert
        assertNotNull(result);
        assertEquals(userDto.getId(), result.getId());
        assertEquals(userDto.getName(), result.getName());
        assertEquals(userDto.getEmail(), result.getEmail());
    }

    /*@Test
    public void createUserErrorTest() {
        // Assign
        User user = getTestUser();
        UserDto userDto = getTestUserDto();
        userDto.setEmail(null);
        user.setId(userDto.getId());
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());

        // Act
        BadRequestException thrown = Assertions.assertThrows(BadRequestException.class, () -> {
            UserDto result = userService.save(userDto);
        });

        // Assert
        assertEquals(thrown.getMessage(), "BAD EMAIL");
    }*/

    @Test
    public void updateUserTest() throws Exception {
        // Assign
        var user = getTestUser();
        Map<Object, Object> fields = new HashMap<>();
        fields.put("name", "updateName");
        fields.put("email", "update@Email");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        // Act
        var result = userService.update(fields, user.getId());

        // Assert
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(fields.get("name"), result.getName());
        assertEquals(fields.get("email"), result.getEmail());
    }

    @Test
    public void deleteUserTest() throws Exception {
        // Assign
        var user = getTestUser();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        // Act
        userService.deleteById(user.getId());

        // Assert
        Mockito.verify(userRepository, Mockito.times(1)).delete(user);
    }

    @Test
    public void findAllUsersTest() throws Exception {
        // Assign
        User user1 = getTestUser();
        User user2 = getTestUser();
        user2.setId(2L);
        List<User> users = List.of(user1, user2);

        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<UserDto> actualUsers = userService.findAll();

        // Assert
        assertNotNull(actualUsers);
        assertEquals(users.size(), actualUsers.size());
    }

    @Test
    public void findUserByIdTest() throws Exception {
        // Assign
        var user = getTestUser();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        // Act
        var result = userService.findUserById(user.getId());

        // Assert
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    public void checkUserTest() throws Exception {
        // Assign
        User user = getTestUser();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        // Act
        User actualUser = userService.checkUser(user.getId());

        // Assert
        Assertions.assertNotNull(actualUser);
        Assertions.assertEquals(user.getId(), actualUser.getId());
        Assertions.assertEquals(user.getEmail(), actualUser.getEmail());
        Assertions.assertEquals(user.getName(), actualUser.getName());
    }

    private User getTestUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("testUser@email.ru");
        user.setName("Test User Name");
        return user;
    }

    private UserDto getTestUserDto() {
        return new UserDto(1L, "Test UserDto Name", "testUserDto@email.ru");
    }
}
