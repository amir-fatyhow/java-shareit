package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ShareItNotFoundException;
import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.mapper.RequestMapper;
import ru.practicum.shareit.request.model.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.dto.RequestWithResponseDto;
import ru.practicum.shareit.request.model.entity.ItemRequest;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    private static final String USER_NOT_FOUND = "Пользователя с указанным Id не существует.";
    private static final String REQUEST_NOT_FOUND = "Запрос с указанным Id не существует.";

    @Override
    public ItemRequestDto save(ItemRequestDto itemRequestDto, long userId) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new ShareItNotFoundException(USER_NOT_FOUND));

        ItemRequest itemRequest = RequestMapper.toItemRequest(itemRequestDto, requester);
        itemRequest = requestRepository.save(itemRequest);
        return RequestMapper.toItemRequestDto(itemRequest);
    }

    @Override
    public List<RequestWithResponseDto> findAllResponsesForAllRequests(long userId, Integer from, Integer size) {
        if (!userRepository.existsById(userId)) {
            throw new ShareItNotFoundException(USER_NOT_FOUND);
        }
        Page<ItemRequest> itemRequests = requestRepository.findAllByRequestorIdOrderByCreatedDesc(
                userId, PageRequest.of(from, size));
        return getRequestWithResponseDto(itemRequests.toList());
    }

    @Override
    public List<RequestWithResponseDto> findAllByUserId(long userId, Integer from, Integer size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ShareItNotFoundException(USER_NOT_FOUND));

        Page<ItemRequest> itemRequests = requestRepository.findAllByRequestorNot(user, PageRequest.of(from, size));
        return getRequestWithResponseDto(itemRequests.toList());
    }

    @Override
    public RequestWithResponseDto findById(long userId, long requestId) {
        if (!userRepository.existsById(userId)) {
            throw new ShareItNotFoundException(USER_NOT_FOUND);
        }

        ItemRequest itemRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new ShareItNotFoundException(REQUEST_NOT_FOUND));

        List<Item> items = itemRepository.findAllByRequestIdOrderByRequestIdDesc(itemRequest.getId());
        List<ItemForRequestDto> requests = items.stream()
                .map(ItemMapper::toItemForRequestDto)
                .collect(Collectors.toList());
        return RequestMapper.toRequestWithResponseDto(itemRequest, requests);
    }

    public List<RequestWithResponseDto> getRequestWithResponseDto(List<ItemRequest> itemRequests) {
        List<RequestWithResponseDto> requestWithResponseDtos = new ArrayList<>();
        for (ItemRequest itemRequest : itemRequests) {
            List<Item> items = itemRepository.findAllByRequestIdOrderByRequestIdDesc(itemRequest.getId());
            List<ItemForRequestDto> requests = items.stream()
                    .map(ItemMapper::toItemForRequestDto)
                    .collect(Collectors.toList());
            RequestWithResponseDto request = RequestMapper.toRequestWithResponseDto(itemRequest, requests);
            requestWithResponseDtos.add(request);
        }
        return requestWithResponseDtos;
    }
}
