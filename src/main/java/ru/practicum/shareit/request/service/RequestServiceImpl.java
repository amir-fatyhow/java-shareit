package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repositories.ItemStorage;
import ru.practicum.shareit.request.mapper.RequestMapper;
import ru.practicum.shareit.request.model.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.dto.RequestWithResponseDto;
import ru.practicum.shareit.request.model.entity.ItemRequest;
import ru.practicum.shareit.request.repositories.RequestStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.services.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestStorage requestStorage;
    private final ItemStorage itemStorage;
    private final UserService userService;

    @Override
    public ItemRequestDto createRequest(ItemRequestDto itemRequestDto, long userId)
            throws BadRequestException, NotFoundException {
        if (itemRequestDto.getDescription() == null || itemRequestDto.getDescription().isEmpty()) {
            throw new BadRequestException("ItemRequest Description is empty");
        }
        User requester = userService.checkUser(userId);
        ItemRequest itemRequest = RequestMapper.toItemRequest(itemRequestDto, requester);
        requestStorage.save(itemRequest);
        return RequestMapper.toItemRequestDto(itemRequest);
    }

    @Override
    public ItemRequest checkItemRequest(long itemRequestId) throws NotFoundException {
        Optional<ItemRequest> optionalItemRequest = requestStorage.findById(itemRequestId);
        if (optionalItemRequest.isPresent()) {
            return optionalItemRequest.get();
        } else {
            throw new NotFoundException(String.format("ItemRequest by ID: %s  - not found", itemRequestId));
        }
    }

    @Override
    public List<RequestWithResponseDto> getAllResponsesForAllRequests(long userId, Integer from, Integer size)
            throws NotFoundException {
        userService.checkUser(userId);
        Page<ItemRequest> itemRequests = requestStorage.findAllByRequestor_IdOrderByCreatedDesc(
                userId, PageRequest.of(from, size));
        return getRequestWithResponseDto(itemRequests.toList());
    }

    @Override
    public List<RequestWithResponseDto> getAllRequestsOtherUsers(long userId, Integer from, Integer size)
            throws NotFoundException, BadRequestException {
        User user = userService.checkUser(userId);
        Page<ItemRequest> itemRequests = requestStorage.findAllByRequestorNot(user, PageRequest.of(from, size));
        return getRequestWithResponseDto(itemRequests.toList());
    }

    @Override
    public RequestWithResponseDto getRequestById(long userId, long requestId) {
        userService.checkUser(userId);
        ItemRequest itemRequest = checkItemRequest(requestId);
        List<Item> items = itemStorage.findAllByRequest_IdOrderByRequestIdDesc(itemRequest.getId());
        List<ItemForRequestDto> requests = items.stream()
                .map(ItemMapper::toItemForRequestDto)
                .collect(Collectors.toList());
        return RequestMapper.toRequestWithResponseDto(itemRequest, requests);
    }

    public List<RequestWithResponseDto> getRequestWithResponseDto(List<ItemRequest> itemRequests) {
        List<RequestWithResponseDto> requestWithResponseDtos = new ArrayList<>();
        for (ItemRequest itemRequest : itemRequests) {
            List<Item> items = itemStorage.findAllByRequest_IdOrderByRequestIdDesc(itemRequest.getId());
            List<ItemForRequestDto> requests = items.stream()
                    .map(ItemMapper::toItemForRequestDto)
                    .collect(Collectors.toList());
            RequestWithResponseDto request = RequestMapper.toRequestWithResponseDto(itemRequest, requests);
            requestWithResponseDtos.add(request);
        }
        return requestWithResponseDtos;
    }
}
