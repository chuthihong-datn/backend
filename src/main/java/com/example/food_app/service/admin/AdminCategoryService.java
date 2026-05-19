package com.example.food_app.service.admin;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.food_app.dto.request.admin.CategoryRequest;
import com.example.food_app.dto.response.admin.CategoryResponse;
import com.example.food_app.entity.Category;
import com.example.food_app.repository.CategoryRepository;
import com.example.food_app.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCategoryService {
    private final CategoryRepository categoryRepository;
    private final MenuRepository menuRepository;
    private final Cloudinary cloudinary;

    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse create(CategoryRequest request, MultipartFile file) {

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tên danh mục không được để trống"
            );
        }

        Category category = new Category();
        String name = normalizeName(request.getName());
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tên danh mục đã tồn tại"
            );
        }
        category.setName(name);
        category.setDescription(request.getDescription());
        category.setIsActive(true);

        if (file != null && !file.isEmpty()) {
            category.setIconUrl(uploadImage(file));
        }

        try {
            categoryRepository.save(category);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Lỗi khi lưu danh mục"
            );
        }

        return mapToResponse(category);
    }

    @Transactional
    public CategoryResponse update(
            Long id,
            CategoryRequest request,
            MultipartFile file
    ) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy danh mục"
                ));

        if (request.getName() != null) {

            String newName = normalizeName(request.getName());

            if (newName.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Tên không được để trống"
                );
            }

            if (categoryRepository.existsByNameIgnoreCaseAndCategoryIdNot(newName, id)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Tên danh mục đã tồn tại"
                );
            }

            category.setName(newName);
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
            menuRepository.updateIsActiveByCategoryId(id, request.getIsActive());
        }

        if (file != null && !file.isEmpty()) {
            category.setIconUrl(uploadImage(file));
        }

        try {
            categoryRepository.save(category);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Lỗi khi cập nhật danh mục"
            );
        }

        return mapToResponse(category);
    }

    @Transactional
    public void delete(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy danh mục"
                ));

        category.setIsActive(false);
        categoryRepository.save(category);

        menuRepository.updateIsActiveByCategoryId(id, false);
    }

    private String uploadImage(MultipartFile file) {

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File phải là ảnh"
            );
        }

        if (file.getSize() > 2 * 1024 * 1024) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ảnh tối đa 2MB"
            );
        }

        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", "categories")
            );

            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Upload ảnh thất bại"
            );
        }
    }

    public List<CategoryResponse> search(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return getAll();
        }

        return categoryRepository.findByNameContainingIgnoreCase(keyword)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .description(category.getDescription())
                .iconUrl(category.getIconUrl())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    private String normalizeName(String name) {
        return name.trim().replaceAll("\\s+", " ");
    }
}