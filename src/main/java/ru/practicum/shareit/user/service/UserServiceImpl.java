package ru.practicum.shareit.user.service;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserRowMapper;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;

    private final ObjectMapper objectMapper;

    @Override
    public UserDto save(UserDto userDto) {
        User user = userRepository.save(UserRowMapper.toUser(userDto));
        return UserRowMapper.toUserDto(user);
    }

    @Override
    public UserDto update(Map<Object,Object> fields, long userId) throws JsonMappingException {
        if (!userRepository.existsById(userId)) {
            throw new NullPointerException("Пользователь с указанным id не существует.");
        }

        Optional<User> targetUser = Optional.of(userRepository.findById(userId).orElseThrow(NullPointerException::new));
        User updateUser = objectMapper.updateValue(targetUser.get(), fields);

        userRepository.save(updateUser);
        return UserRowMapper.toUserDto(updateUser);
    }

    @Override
    public UserDto findById(long userId) {
        Optional<User> user = Optional.of(userRepository.findById(userId).orElseThrow(NullPointerException::new));
        return UserRowMapper.toUserDto(user.get());
    }

    @Override
    public List<UserDto> findAll() {
        return UserRowMapper.toUserDto(userRepository.findAll());
    }

    @Override
    public void deleteById(long userId) {
        userRepository.deleteById(userId);
    }
}
