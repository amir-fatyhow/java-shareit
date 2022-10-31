package ru.practicum.shareit.user.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repositories.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) throws BadRequestException {
        if (userDto.getEmail() == null || userDto.getEmail().isEmpty() || !userDto.getEmail().contains("@")) {
            throw new BadRequestException("BAD EMAIL");
        }
        User user = UserMapper.toUser(userDto);
        user = userStorage.save(user);
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public UserDto updateUser(UserDto userDto, long userId) {
        User user = checkUser(userId);
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        userStorage.save(user);
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public void deleteUser(long userId) {
        User user = checkUser(userId);
        userStorage.delete(user);
    }

    @Override
    public List<UserDto> findAllUsers() {
        return userStorage.findAll().stream()
                .map(user -> new UserDto(user.getId(), user.getName(), user.getEmail()))
                .collect(Collectors.toList());
    }

    @Override
    public UserDto findUserById(long userId) {
        User user = checkUser(userId);
        return UserMapper.toUserDto(user);
    }

    @Override
    public User checkUser(long userId) throws NotFoundException {
        return userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User by ID: %s not found", userId)));
    }
}
