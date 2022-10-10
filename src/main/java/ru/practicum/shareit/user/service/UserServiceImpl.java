package ru.practicum.shareit.user.service;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ShareItNotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserRowMapper;
import ru.practicum.shareit.user.repositories.UserStorage;

import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;
    private final ObjectMapper objectMapper;

    @Override
    public UserDto save(UserDto userDto) {
        User user = userStorage.save(UserRowMapper.toUser(userDto));
        return UserRowMapper.toUserDto(user);
    }

    @Override
    public UserDto update(Map<Object,Object> fields, long userId) throws JsonMappingException {
        if (!userStorage.existsById(userId)) {
            throw new ShareItNotFoundException("Пользователь с указанным id не существует.");
        }

        User targetUser = userStorage.findById(userId).orElseThrow(() -> new ShareItNotFoundException(""));
        User updateUser = objectMapper.updateValue(targetUser, fields);

        userStorage.save(updateUser);
        return UserRowMapper.toUserDto(updateUser);
    }

    @Override
    public UserDto findById(long userId) {
        User user = userStorage.findById(userId).orElseThrow(() -> new ShareItNotFoundException(""));
        return UserRowMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> findAll() {
        return UserRowMapper.toUserDto(userStorage.findAll());
    }

    @Override
    public void deleteById(long userId) {
        userStorage.deleteById(userId);
    }
}
