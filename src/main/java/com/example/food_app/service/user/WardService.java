package com.example.food_app.service.user;

import com.example.food_app.dto.response.user.WardResponse;
import com.example.food_app.entity.Ward;
import com.example.food_app.repository.WardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WardService {

    private final WardRepository wardRepository;

    public List<WardResponse> getAllWardDelivery() {
        List<Ward> wards = wardRepository.findByIsDeliveryTrue();

        if (wards.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Hiện tại không có khu vực nào hỗ trợ giao hàng"
            );
        }

        return wards.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private WardResponse mapToResponse(Ward ward) {
        return WardResponse.builder()
                .wardId(ward.getWardId())
                .name(ward.getName())
                .isDelivery(Boolean.TRUE.equals(ward.getIsDelivery()))
                .shippingFee(ward.getShippingFee())
                .build();
    }
}