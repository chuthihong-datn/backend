package com.example.food_app.service.user;

import com.example.food_app.dto.response.user.CategoryResponse;
import com.example.food_app.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAllByIsActiveIsTrue()
                .stream()
                .map(category -> CategoryResponse.builder()
                        .id(category.getCategoryId())
                        .name(category.getName())
                        .iconUrl(category.getIconUrl())
                        .build())
                .toList();
    }
}
