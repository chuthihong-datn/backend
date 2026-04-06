package com.example.food_app.service.admin;

import com.example.food_app.dto.request.admin.WardRequest;
import com.example.food_app.dto.response.admin.WardResponse;
import com.example.food_app.entity.Ward;
import com.example.food_app.repository.WardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminWardService {

    private final WardRepository wardRepository;

    public List<WardResponse> getAll() {
        return wardRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<WardResponse> search(String keyword) {
        return wardRepository.findByNameContainingIgnoreCase(keyword)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public WardResponse update(BigInteger id, WardRequest request) {

        Ward ward = wardRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy địa chỉ"
                ));

        if (request.getShippingFee() != null) {
            if (request.getShippingFee() < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phí ship không hợp lệ");
            }
            ward.setShippingFee(request.getShippingFee());
        }

        if (request.getIsDelivery() != null) {
            ward.setIsDelivery(request.getIsDelivery());
        }

        wardRepository.save(ward);

        return mapToResponse(ward);
    }

    @Transactional
    public void delete(BigInteger id) {

        Ward ward = wardRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy địa chỉ"
                ));

        ward.setIsDelivery(false);
        wardRepository.save(ward);
    }

    private WardResponse mapToResponse(Ward ward) {
        return WardResponse.builder()
                .wardId(ward.getWardId())
                .wardCode(ward.getWardCode())
                .name(ward.getName())
                .shippingFee(ward.getShippingFee())
                .isDelivery(ward.getIsDelivery())
                .createdAt(ward.getCreatedAt())
                .updatedAt(ward.getUpdatedAt())
                .build();
    }
}