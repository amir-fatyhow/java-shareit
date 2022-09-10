package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.getAllUsers();
    }

    @Override
    public UserDto createUser(User user) {
        return userRepository.createUser(user);
    }

    @Override
    public UserDto getUserById(long userId) {
        return userRepository.getUserById(userId);
    }

    @Override
    public UserDto updateUser(Map<Object,Object> fields, long userId) {
        return userRepository.updateUser(fields,userId);
    }

    @Override
    public void deleteUser(long userId) {
        userRepository.deleteUser(userId);
    }
}
