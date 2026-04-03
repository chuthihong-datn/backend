package com.example.food_app.service.admin;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.food_app.dto.request.admin.MenuRequest;
import com.example.food_app.dto.response.admin.MenuResponse;
import com.example.food_app.entity.Category;
import com.example.food_app.entity.Menu;
import com.example.food_app.entity.MenuSize;
import com.example.food_app.entity.Topping;
import com.example.food_app.repository.CategoryRepository;
import com.example.food_app.repository.MenuRepository;
import com.example.food_app.repository.ToppingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminMenuService {

    private final MenuRepository menuRepository;
    private final CategoryRepository categoryRepository;
    private final ToppingRepository toppingRepository;
    private final Cloudinary cloudinary;

    public List<MenuResponse> getAll() {
        return menuRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public MenuResponse getDetail(BigInteger id) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy menu"
                ));
        return mapToResponse(menu);
    }

    @Transactional
    public MenuResponse create(MenuRequest request, MultipartFile[] files) {

        validateRequest(request);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Category không tồn tại"
                ));

        Menu menu = new Menu();
        menu.setCategory(category);
        menu.setName(request.getName());
        menu.setDescription(request.getDescription());
        menu.setBasePrice(request.getBasePrice());
        menu.setAmount(request.getAmount());
        menu.setIsActive(true);

        if (files != null && files.length > 0) {
            menu.setImages(uploadImages(files));
        }

        if (request.getToppingIds() != null && !request.getToppingIds().isEmpty()) {
            Set<Topping> toppings = new HashSet<>(
                    toppingRepository.findAllById(request.getToppingIds())
            );
            menu.setToppings(toppings);
        }

        if (request.getSizes() != null && !request.getSizes().isEmpty()) {
            List<MenuSize> sizes = request.getSizes().stream()
                    .map(s -> {
                        MenuSize ms = new MenuSize();
                        ms.setMenu(menu);
                        ms.setSizeName(s.getSizeName());
                        ms.setExtraPrice(s.getExtraPrice());
                        return ms;
                    }).toList();

            menu.setSizes(sizes);
        }

        menuRepository.save(menu);

        return mapToResponse(menu);
    }

    @Transactional
    public MenuResponse update(BigInteger id, MenuRequest request, MultipartFile[] files) {

        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy menu"
                ));

        if (request.getName() != null) {
            if (request.getName().trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên không được để trống");
            }
            menu.setName(request.getName());
        }

        if (request.getDescription() != null) {
            menu.setDescription(request.getDescription());
        }

        if (request.getBasePrice() != null) {
            menu.setBasePrice(request.getBasePrice());
        }

        if (request.getAmount() != null) {
            menu.setAmount(request.getAmount());
        }

        if (files != null && files.length > 0) {
            menu.setImages(uploadImages(files));
        }

        if (request.getToppingIds() != null) {
            Set<Topping> toppings = new HashSet<>(
                    toppingRepository.findAllById(request.getToppingIds())
            );
            menu.setToppings(toppings);
        }

        if (request.getSizes() != null) {
            menu.getSizes().clear();

            List<MenuSize> newSizes = request.getSizes().stream()
                    .map(s -> {
                        MenuSize ms = new MenuSize();
                        ms.setMenu(menu);
                        ms.setSizeName(s.getSizeName());
                        ms.setExtraPrice(s.getExtraPrice());
                        return ms;
                    }).toList();

            menu.getSizes().addAll(newSizes);
        }

        menuRepository.save(menu);

        return mapToResponse(menu);
    }

    @Transactional
    public void delete(BigInteger id) {

        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy menu"
                ));

        menu.setIsActive(false);
        menuRepository.save(menu);
    }

    private List<String> uploadImages(MultipartFile[] files) {

        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile file : files) {

            if (file.isEmpty()) continue;

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "File phải là ảnh"
                );
            }

            if (file.getSize() > 2 * 1024 * 1024) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Ảnh tối đa 2MB"
                );
            }

            try {
                Map uploadResult = cloudinary.uploader().upload(
                        file.getBytes(),
                        ObjectUtils.asMap("folder", "menus")
                );

                imageUrls.add(uploadResult.get("secure_url").toString());

            } catch (IOException e) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Upload ảnh thất bại"
                );
            }
        }

        return imageUrls;
    }

    private void validateRequest(MenuRequest request) {

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Tên không được để trống"
            );
        }

        if (request.getBasePrice() == null || request.getBasePrice().doubleValue() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Giá không hợp lệ"
            );
        }

        if (request.getAmount() == null || request.getAmount() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Số lượng không hợp lệ"
            );
        }
    }

    private MenuResponse mapToResponse(Menu menu) {

        return MenuResponse.builder()
                .menuId(menu.getMenuId())
                .name(menu.getName())
                .description(menu.getDescription())
                .basePrice(menu.getBasePrice())
                .amount(menu.getAmount())
                .isActive(menu.getIsActive())
                .images(menu.getImages())
                .categoryName(menu.getCategory().getName())

                .toppings(menu.getToppings()
                        .stream().map(Topping::getName).toList())

                .sizes(menu.getSizes().stream()
                        .map(s -> MenuResponse.SizeResponse.builder()
                                .sizeName(s.getSizeName())
                                .extraPrice(s.getExtraPrice())
                                .build())
                        .toList())
                .outOfStock(menu.isOutOfStock())

                .build();
    }
}