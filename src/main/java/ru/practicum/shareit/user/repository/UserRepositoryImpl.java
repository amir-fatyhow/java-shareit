package ru.practicum.shareit.user.repository;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.ReflectionUtils;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserRowMapper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final List<User> users = new ArrayList<>();

    private static long id = 0;

    @Override
    public UserDto createUser(User user) {
        emailValidation(user.getEmail());
        user.setId(++id);
        users.add(user);
        return UserRowMapper.toItemDto(user);
    }

    @Override
    public UserDto getUserById(long userId) {
        return UserRowMapper.toItemDto(users.stream().filter(user -> user.getId() == userId)
                .findAny().orElseThrow(() -> new IllegalArgumentException("Неверно указан Id пользователя.")));
    }

    @Override
    public List<UserDto> getAllUsers() {
        return users.stream().map(UserRowMapper::toItemDto).collect(Collectors.toList());
    }

    @Override
    public UserDto updateUser(Map<Object,Object> fields, long userId) {
        User targetUser = users.stream().filter(user -> user.getId() == userId).findAny()
                .orElseThrow(() -> new IllegalArgumentException("Неверно указан Id пользователя."));

        fields.forEach((key, value) -> {
            Field field = ReflectionUtils.findField(User.class, (String) key);
            assert field != null;
            field.setAccessible(true);

            emailValidation((String) value);
            ReflectionUtils.setField(field, targetUser, value);
        });
        return UserRowMapper.toItemDto(targetUser);
    }

    @Override
    public void deleteUser(long userId) {
        users.removeIf(user -> user.getId() == userId);
    }

    private void emailValidation(String email) {
        if (!users.isEmpty() && users.stream().anyMatch(isUserEmailExist ->
                isUserEmailExist.getEmail().equals(email))) {
            throw new IllegalArgumentException("Пользователь с указанным email уже существует.");
        }
    }

}
