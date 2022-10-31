package ru.practicum.shareit.user.service;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ShareItNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private static final String USER_NOT_FOUND = "Пользователя с указанным Id не существует.";

    @Override
    @Transactional
    public UserDto save(UserDto userDto) {
        /*if (userDto.getEmail() == null || userDto.getEmail().isEmpty() || !userDto.getEmail().contains("@")) {
            throw new BadRequestException("BAD EMAIL");
        }*/
        User user = userRepository.save(UserMapper.toUser(userDto));
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public UserDto update(Map<Object, Object> fields, long userId) throws JsonMappingException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ShareItNotFoundException(USER_NOT_FOUND));
        user = objectMapper.updateValue(user, fields);

        userRepository.save(user);
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public void deleteById(long userId) {
        User user = checkUser(userId);
        userRepository.delete(user);
    }

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(user -> new UserDto(user.getId(), user.getName(), user.getEmail()))
                .collect(Collectors.toList());
    }

    @Override
    public UserDto findUserById(long userId) {
        User user = checkUser(userId);
        return UserMapper.toUserDto(user);
    }

    @Override
    public User checkUser(long userId) throws ShareItNotFoundException {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ShareItNotFoundException(String.format("User by ID: %s not found", userId)));
    }
}
