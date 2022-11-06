package ru.practicum.shareit.request.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.request.model.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.dto.RequestWithResponseDto;

import java.util.List;

@Service
public interface RequestService {
    ItemRequestDto save(ItemRequestDto itemRequestDto, long userId);

    List<RequestWithResponseDto> findAllResponsesForAllRequests(long userId, Integer from, Integer size);

    List<RequestWithResponseDto> findAllByUserId(long userId, Integer from, Integer size);

    RequestWithResponseDto findById(long userId, long requestId);
}
