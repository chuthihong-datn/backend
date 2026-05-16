package com.example.food_app.service.admin;

import com.example.food_app.dto.request.admin.ToppingRequest;
import com.example.food_app.dto.response.admin.ToppingResponse;
import com.example.food_app.entity.Topping;
import com.example.food_app.repository.ToppingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminToppingService {
    private final ToppingRepository toppingRepository;

    public List<ToppingResponse> getAll() {
        return toppingRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ToppingResponse create(ToppingRequest request) {

        validate(request);

        Topping topping = new Topping();
        topping.setName(request.getName());
        topping.setPrice(request.getPrice());
        topping.setIsActive(true);
        topping.setOutOfStock(false);

        try {
            toppingRepository.save(topping);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Lỗi khi lưu topping"
            );
        }

        return mapToResponse(topping);
    }

    @Transactional
    public ToppingResponse update(BigInteger id, ToppingRequest request) {

        Topping topping = toppingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy topping"
                ));

        if (request.getName() != null) {
            if (request.getName().trim().isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Tên không được để trống"
                );
            }
            topping.setName(request.getName());
        }

        if (request.getPrice() != null) {
            if (request.getPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Giá không hợp lệ"
                );
            }
            topping.setPrice(request.getPrice());
        }

        if (request.getIsActive() != null) {
            topping.setIsActive(request.getIsActive());
        }

        if (request.getOutOfStock() != null) {
            topping.setOutOfStock(request.getOutOfStock());
        }

        try {
            toppingRepository.save(topping);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Lỗi khi cập nhật topping"
            );
        }

        return mapToResponse(topping);
    }

    @Transactional
    public void delete(BigInteger id) {

        Topping topping = toppingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy topping"
                ));

        topping.setIsActive(false);

        toppingRepository.save(topping);
    }

    public List<ToppingResponse> search(String keyword) {

        List<Topping> toppings;

        if (keyword == null || keyword.trim().isEmpty()) {
            toppings = toppingRepository.findAll();
        } else {
            toppings = toppingRepository.findByNameContainingIgnoreCase(keyword);
        }

        return toppings.stream()
                .filter(Topping::getIsActive)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void validate(ToppingRequest request) {

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tên topping không được để trống"
            );
        }

        if (request.getPrice() == null ||
                request.getPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Giá topping không hợp lệ"
            );
        }
    }

    private ToppingResponse mapToResponse(Topping topping) {
        return ToppingResponse.builder()
                .toppingId(topping.getToppingId())
                .name(topping.getName())
                .price(topping.getPrice())
                .isActive(topping.getIsActive())
                .outOfStock(topping.isOutOfStock())
                .build();
    }
}