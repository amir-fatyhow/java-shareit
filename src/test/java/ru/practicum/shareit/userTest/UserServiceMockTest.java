package ru.practicum.shareit.userTest;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ShareItNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

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
        User user = user();
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

    @Test
    void updateTest() throws JsonMappingException {
        // Assign
        User user = user();
        Map<Object, Object> fields = new HashMap<>();
        fields.put("name", "updateName");
        fields.put("email", "update@Email");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        // Act
        UserDto result = userService.update(fields, user.getId());

        // Assert
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(fields.get("name"), result.getName());
        assertEquals(fields.get("email"), result.getEmail());
    }

    @Test
    void updateUserNotFoundTest() {
        // Assign
        User user = user();
        Map<Object, Object> fields = new HashMap<>();

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act
        ShareItNotFoundException exception = assertThrows(ShareItNotFoundException.class, () -> {
            userService.update(fields, user.getId());
        });

        // Assert
        assertEquals("Пользователя с указанным Id не существует.", exception.getMessage());
    }

    @Test
    void deleteTest() {
        // Assign
        User user = user();

        // Act
        userService.deleteById(user.getId());

        // Assert
        verify(userRepository, times(1)).deleteById(user.getId());
    }

    @Test
    void findAllTest() {
        // Assign
        User user1 = user();
        User user2 = user();
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
    void findByIdTest() {
        // Assign
        User user = user();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        // Act
        UserDto result = userService.findById(user.getId());

        // Assert
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    void findByIdUserNotFoundTest() {
        // Assign
        User user = user();

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act
        ShareItNotFoundException exception = assertThrows(ShareItNotFoundException.class, () ->
            userService.findById(user.getId()));

        // Assert
        assertEquals("Пользователя с указанным Id не существует.", exception.getMessage());
    }

    private User user() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@email.ru");
        user.setName("Name");
        return user;
    }

    private UserDto getTestUserDto() {
        return new UserDto(1L, "Name", "userDto@email.ru");
    }
}
