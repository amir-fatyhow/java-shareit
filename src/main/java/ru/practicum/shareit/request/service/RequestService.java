package ru.practicum.shareit.request.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.request.model.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.dto.RequestWithResponseDto;
import ru.practicum.shareit.request.model.entity.ItemRequest;

import java.util.List;

@Service
public interface RequestService {

    ItemRequest checkItemRequest(long itemRequestId);

    ItemRequestDto createRequest(ItemRequestDto itemRequestDto, long userId);

    List<RequestWithResponseDto> getAllResponsesForAllRequests(long userId, Integer from, Integer size);

    List<RequestWithResponseDto> getAllRequestsOtherUsers(long userId, Integer from, Integer size);

    RequestWithResponseDto getRequestById(long userId, long requestId);
}
