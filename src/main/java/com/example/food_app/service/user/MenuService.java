package com.example.food_app.service.user;

import com.example.food_app.dto.response.user.*;
import com.example.food_app.entity.Menu;
import com.example.food_app.entity.MenuSize;
import com.example.food_app.entity.Review;
import com.example.food_app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {
    private final MenuRepository menuRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ToppingRepository toppingRepository;

    //danh sách tất cả menu
    public List<MenuResponse> getListMenu() {

        List<Menu> menus = menuRepository.findAllByIsActiveIsTrue();

        Map<BigInteger, Float> ratingMap = buildRatingMap(menus);

        return menus.stream()
                .map(menu -> MenuResponse.builder()
                        .id(menu.getMenuId())
                        .name(menu.getName())
                        .images(menu.getImages())
                        .minPrice(calculateMinPrice(menu))
                        .amount(menu.getAmount())
                        .rating(ratingMap.getOrDefault(menu.getMenuId(), 0f))
                        .outOfStock(menu.isOutOfStock())
                        .build())
                .toList();
    }

    //tính giá tối thiểu hiển thị
    private BigDecimal calculateMinPrice(Menu menu) {

        BigDecimal basePrice = menu.getBasePrice();

        if (menu.getSizes() == null || menu.getSizes().isEmpty()) {
            return basePrice;
        }

        BigDecimal minExtraPrice = menu.getSizes()
                .stream()
                .map(MenuSize::getExtraPrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        return basePrice.add(minExtraPrice);
    }

    //lấy danh sách menu theo id category
    public List<MenuResponse> getMenusByCategory(BigInteger categoryId){

        if(!categoryRepository.existsByCategoryIdAndIsActiveIsTrue(categoryId)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category không tồn tại");
        }

        List<Menu> menus =
                menuRepository.findByCategory_CategoryIdAndIsActiveTrue(categoryId);

        Map<BigInteger, Float> ratingMap = buildRatingMap(menus);

        return menus.stream()
                .map(menu -> MenuResponse.builder()
                        .id(menu.getMenuId())
                        .name(menu.getName())
                        .images(menu.getImages())
                        .minPrice(calculateMinPrice(menu))
                        .amount(menu.getAmount())
                        .rating(ratingMap.getOrDefault(menu.getMenuId(), 0f))
                        .outOfStock(menu.isOutOfStock())
                        .build())
                .toList();
    }

    //lấy thông tin chi tiết menu
    public MenuDetailResponse getMenuDetail(BigInteger menuId){

        Menu menu = menuRepository.findByMenuIdAndIsActiveTrue(menuId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Menu không tồn tại"
                ));

        List<MenuSizeResponse> sizeResponses =
                menu.getSizes() == null ? List.of() :
                        menu.getSizes().stream()
                                .map(s -> MenuSizeResponse.builder()
                                        .id(s.getMenuSizeId())
                                        .name(s.getSizeName())
                                        .extraPrice(s.getExtraPrice())
                                        .build())
                                .toList();

        List<ToppingResponse> toppingResponses =
                toppingRepository.findAvailableToppingsByMenuId(menu.getMenuId())
                        .stream()
                        .map(t -> ToppingResponse.builder()
                                .id(t.getToppingId())
                                .name(t.getName())
                                .price(t.getPrice())
                                .build())
                        .toList();

        List<Review> reviews = reviewRepository.findByMenu_MenuId(menuId);
        List<ReviewResponse> reviewResponses = reviews.stream()
                .map(r -> ReviewResponse.builder()
                        .userName(r.getAccount().getFullName())
                        .rating(r.getRating())
                        .comment(r.getComment())
                        .createdAt(r.getCreatedAt())
                        .build())
                .toList();

        float avgRating = reviewResponses.isEmpty()
                ? 0f
                : (float) reviewResponses.stream()
                .mapToDouble(ReviewResponse::getRating)
                .average()
                .orElse(0);

        return MenuDetailResponse.builder()
                .id(menu.getMenuId())
                .name(menu.getName())
                .description(menu.getDescription())
                .images(menu.getImages())
                .amount(menu.getAmount())
                .minPrice(calculateMinPrice(menu))
                .sizes(sizeResponses)
                .toppings(toppingResponses)
                .rating(avgRating)
                .reviewCount(reviewResponses.size())
                .reviews(reviewResponses)
                .build();
    }

    private Map<BigInteger, Float> buildRatingMap(List<Menu> menus) {

        List<BigInteger> ids = menus.stream()
                .map(Menu::getMenuId)
                .toList();

        List<Review> reviews = reviewRepository.findByMenu_MenuIdIn(ids);

        return reviews.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getMenu().getMenuId(),
                        Collectors.collectingAndThen(
                                Collectors.averagingDouble(Review::getRating),
                                avg -> avg.floatValue()
                        )
                ));
    }

    //tìm kiếm
    public List<MenuResponse> searchMenus(String keyword){

        List<Menu> menus = menuRepository
                .findByNameContainingIgnoreCaseAndIsActiveTrue(keyword);

        Map<BigInteger, Float> ratingMap = buildRatingMap(menus);

        return menus.stream()
                .map(menu -> MenuResponse.builder()
                        .id(menu.getMenuId())
                        .name(menu.getName())
                        .images(menu.getImages())
                        .minPrice(calculateMinPrice(menu))
                        .rating(ratingMap.getOrDefault(menu.getMenuId(), 0f))
                        .outOfStock(menu.isOutOfStock())
                        .build())
                .toList();
    }

    //danh sách 10 menu hot nhất
    public List<MenuHotResponse> getTopSellingMenus() {
        List<Object[]> salesData = orderDetailRepository.findTotalSoldByMenu();

        Map<BigInteger, Long> totalSoldMap = salesData.stream()
                .collect(Collectors.toMap(
                        obj -> (BigInteger) obj[0], // menuId
                        obj -> ((Number) obj[1]).longValue() // totalSold
                ));

        List<Menu> menus = menuRepository.findAllByMenuIdInAndIsActiveTrue(
                new ArrayList<>(totalSoldMap.keySet())
        );

        Map<BigInteger, Float> ratingMap = buildRatingMap(menus);

        return menus.stream()
                .map(menu -> MenuHotResponse.builder()
                        .id(menu.getMenuId())
                        .name(menu.getName())
                        .images(menu.getImages())
                        .minPrice(calculateMinPrice(menu))
                        .amount(menu.getAmount())
                        .rating(ratingMap.getOrDefault(menu.getMenuId(), 0f))
                        .totalSold(totalSoldMap.getOrDefault(menu.getMenuId(), 0L))
                        .outOfStock(menu.isOutOfStock())
                        .build()
                )

                .sorted(Comparator.comparing(MenuHotResponse::getTotalSold).reversed())
                .limit(10)
                .toList();
    }

}
