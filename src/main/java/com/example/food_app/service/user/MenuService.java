package com.example.food_app.service.user;

import com.example.food_app.dto.response.user.*;
import com.example.food_app.entity.*;
import com.example.food_app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ToppingRepository toppingRepository;
    private final FlashSaleRepository flashSaleRepository;

    // ================= GET ALL MENU =================
    public List<MenuResponse> getListMenu() {

        List<Menu> menus = menuRepository.findAllByIsActiveIsTrue();

        Map<BigInteger, Float> ratingMap = buildRatingMap(menus);
        Map<BigInteger, FlashSale> flashSaleMap = buildFlashSaleMap();
        Map<BigInteger, Long> totalSoldMap = buildTotalSoldMap();

        return menus.stream()
                .map(menu -> mapToMenuResponse(menu, ratingMap, flashSaleMap, totalSoldMap))
                .toList();
    }

    // ================= GET BY CATEGORY =================
    public List<MenuResponse> getMenusByCategory(BigInteger categoryId) {

        if (!categoryRepository.existsByCategoryIdAndIsActiveIsTrue(categoryId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category không tồn tại");
        }

        List<Menu> menus =
                menuRepository.findByCategory_CategoryIdAndIsActiveTrue(categoryId);

        Map<BigInteger, Float> ratingMap = buildRatingMap(menus);
        Map<BigInteger, FlashSale> flashSaleMap = buildFlashSaleMap();
        Map<BigInteger, Long> totalSoldMap = buildTotalSoldMap();

        return menus.stream()
                .map(menu -> mapToMenuResponse(menu, ratingMap, flashSaleMap, totalSoldMap))
                .toList();
    }

    // ================= SEARCH MENU =================
    public List<MenuResponse> searchMenus(String keyword) {

        List<Menu> menus =
                menuRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(keyword);

        Map<BigInteger, Float> ratingMap = buildRatingMap(menus);
        Map<BigInteger, FlashSale> flashSaleMap = buildFlashSaleMap();
        Map<BigInteger, Long> totalSoldMap = buildTotalSoldMap();

        return menus.stream()
                .map(menu -> mapToMenuResponse(menu, ratingMap, flashSaleMap, totalSoldMap))
                .toList();
    }

    // ================= MENU DETAIL =================
    public MenuDetailResponse getMenuDetail(BigInteger menuId) {

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
                .outOfStock(menu.isOutOfStock())
                .build();
    }

    // ================= MAPPING =================
    private MenuResponse mapToMenuResponse(
            Menu menu,
            Map<BigInteger, Float> ratingMap,
            Map<BigInteger, FlashSale> flashSaleMap,
            Map<BigInteger, Long> totalSoldMap
    ) {

        BigDecimal minPrice = calculateMinPrice(menu);
        FlashSale fs = flashSaleMap.get(menu.getMenuId());

        boolean isFlashSale = fs != null;
        BigDecimal discountedPrice = null;
        Integer discountPercent = null;
        LocalDateTime flashSaleEndTime = null;

        if (fs != null) {
            discountedPrice = applyDiscount(minPrice, fs);

            if ("PERCENT".equals(fs.getDiscountType().name())) {
                discountPercent = fs.getDiscountValue().intValue();
            }

            flashSaleEndTime = fs.getEndTime();
        }

        Long totalSold = totalSoldMap.getOrDefault(menu.getMenuId(), 0L);

        return MenuResponse.builder()
                .id(menu.getMenuId())
                .name(menu.getName())
                .images(menu.getImages())
                .minPrice(minPrice)
                .amount(menu.getAmount())
                .rating(ratingMap.getOrDefault(menu.getMenuId(), 0f))
                .outOfStock(menu.isOutOfStock())
                .totalSold(totalSold)
                .isFlashSale(isFlashSale)
                .discountedPrice(discountedPrice)
                .discountPercent(discountPercent)
                .flashSaleEndTime(flashSaleEndTime)
                .build();
    }

    // ================= TOTAL SOLD MAP =================
    private Map<BigInteger, Long> buildTotalSoldMap() {

        List<Object[]> salesData = orderDetailRepository.findTotalSoldByMenu();

        return salesData.stream()
                .collect(Collectors.toMap(
                        obj -> (BigInteger) obj[0],
                        obj -> ((Number) obj[1]).longValue()
                ));
    }

    // ================= FLASH SALE MAP =================
    private Map<BigInteger, FlashSale> buildFlashSaleMap() {

        LocalDateTime now = LocalDateTime.now();

        List<FlashSale> activeSales = flashSaleRepository.findAll().stream()
                .filter(fs -> Boolean.TRUE.equals(fs.getIsActive()))
                .filter(fs -> fs.getStartTime() != null && fs.getEndTime() != null)
                .filter(fs -> now.isAfter(fs.getStartTime()) && now.isBefore(fs.getEndTime()))
                .toList();

        Map<BigInteger, FlashSale> map = new HashMap<>();

        for (FlashSale fs : activeSales) {
            if (fs.getItems() == null) continue;

            for (Menu item : fs.getItems()) {
                map.put(item.getMenuId(), fs);
            }
        }

        return map;
    }

    // ================= RATING MAP =================
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

    // ================= PRICE =================
    private BigDecimal calculateMinPrice(Menu menu) {

        BigDecimal basePrice = menu.getBasePrice();

        if (menu.getSizes() == null || menu.getSizes().isEmpty()) {
            return basePrice;
        }

        BigDecimal minExtra = menu.getSizes().stream()
                .map(MenuSize::getExtraPrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        return basePrice.add(minExtra);
    }

    // ================= DISCOUNT =================
    private BigDecimal applyDiscount(BigDecimal price, FlashSale fs) {

        if ("PERCENT".equals(fs.getDiscountType().name())) {

            BigDecimal discount = price
                    .multiply(fs.getDiscountValue())
                    .divide(BigDecimal.valueOf(100));

            if (fs.getMaxDiscount() != null) {
                discount = discount.min(fs.getMaxDiscount());
            }

            return price.subtract(discount);
        }

        return price.subtract(fs.getDiscountValue());
    }
}