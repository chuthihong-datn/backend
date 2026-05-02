package com.example.food_app.service.user;

import com.example.food_app.dto.response.admin.ReviewResponse;
import com.example.food_app.dto.response.user.MenuResponse;
import com.example.food_app.dto.response.user.Voucher1Response;
import com.example.food_app.dto.response.admin.FlashSaleResponse;
import com.example.food_app.dto.response.user.WardResponse;
import com.example.food_app.service.admin.AdminFlashSaleService;
import com.example.food_app.service.admin.AdminReviewService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final MenuService menuService;
    private final VoucherService voucherService;
    private final AdminFlashSaleService flashSaleService;
    private final AdminReviewService reviewService;
    private final WardService wardService;

    // chat main
    public String chat(String userMessage) {
        try {
            Map<String, Boolean> intent = detectIntent(userMessage);

            String context = buildContext(userMessage, intent);

            String prompt = """
                Bạn là trợ lý tư vấn nhà hàng FoodyDelivery.

                Dữ liệu hệ thống:
                """ + context + """

                Hãy:
                - Trả lời NGẮN GỌN
                - Gợi ý cụ thể món ăn nếu có
                - Ưu tiên món bán chạy, rating cao, đang sale
                - Không bịa dữ liệu

                Câu hỏi: """ + userMessage;

            return callGemini(prompt);

        } catch (Exception e) {
            e.printStackTrace();
            return "Xin lỗi, AI đang bận 😢";
        }
    }

    // intent
    private Map<String, Boolean> detectIntent(String message) {
        String msg = message.toLowerCase();

        boolean isMenu = msg.contains("món")
                || msg.contains("ăn")
                || msg.contains("menu")
                || msg.contains("đồ ăn");

        boolean isVoucher = msg.contains("voucher")
                || msg.contains("mã giảm")
                || msg.contains("mã")
                || msg.contains("giảm giá");

        boolean isFlash = msg.contains("sale")
                || msg.contains("flash")
                || msg.contains("khuyến mãi")
                || msg.contains("giảm sốc")
                || msg.contains("mấy giờ")
                || msg.contains("khi nào")
                || msg.contains("thời gian sale");

        boolean isReview = msg.contains("đánh giá")
                || msg.contains("review")
                || msg.contains("ngon")
                || msg.contains("tốt");

        boolean isDelivery = msg.contains("ship")
                || msg.contains("giao hàng")
                || msg.contains("địa chỉ")
                || msg.contains("khu vực")
                || msg.contains("phí ship")
                || msg.contains("vận chuyển");

        boolean askShippingFee = msg.contains("phí ship") || msg.contains("bao nhiêu tiền ship");

        boolean askDeliveryArea = msg.contains("khu vực")
                || msg.contains("giao được không")
                || msg.contains("có giao");

        return Map.of(
                "menu", isMenu,
                "voucher", isVoucher,
                "flash", isFlash,
                "review", isReview,
                "delivery", isDelivery,
                "delivery_fee", askShippingFee,
                "delivery_area", askDeliveryArea
        );
    }

    // build context
    private String buildContext(String message, Map<String, Boolean> intent) {
        StringBuilder context = new StringBuilder();

        if (intent.get("menu")) {
            context.append(buildMenuContext()).append("\n\n");
        }

        if (intent.get("voucher")) {
            context.append(buildVoucherContext()).append("\n\n");
        }

        if (intent.get("flash")) {
            context.append(buildFlashSaleContext()).append("\n\n");
            context.append(buildFlashTimeContext()).append("\n\n");
        }

        if (intent.get("review")) {
            context.append(buildReviewContext()).append("\n\n");
        }

        if (intent.get("delivery")) {
            context.append(buildDeliveryContext(message)).append("\n\n");
        }

        return context.toString();
    }

    // menu
    private String buildMenuContext() {
        List<MenuResponse> menus = menuService.getListMenu();

        return menus.stream()
                .sorted((a, b) -> Long.compare(
                        b.getTotalSold() != null ? b.getTotalSold() : 0,
                        a.getTotalSold() != null ? a.getTotalSold() : 0))
                .limit(15)
                .map(m -> "🍔 " + m.getName() +
                        " | Giá: " + m.getMinPrice() +
                        " | ⭐ " + m.getRating() +
                        (m.getTotalSold() != null ? " | Đã bán: " + m.getTotalSold() : "") +
                        (m.isFlashSale() ? " | 🔥 SALE " + m.getDiscountPercent() + "%" : "") +
                        (m.isOutOfStock() ? " | Hết hàng" : "")
                )
                .reduce("MENU:\n", (a, b) -> a + b + "\n");
    }

    // voucher
    private String buildVoucherContext() {
        List<Voucher1Response> vouchers = voucherService.getAvailableVouchers();

        return vouchers.stream()
                .limit(10)
                .map(v -> "🎟️ " + v.getCode() +
                        " | Giảm: " + v.getDiscountValue() +
                        " | Đơn tối thiểu: " + v.getMinOrderAmount() +
                        " | HSD: " + v.getEndDate())
                .reduce("VOUCHER:\n", (a, b) -> a + b + "\n");
    }

    // flash sale
    private String buildFlashSaleContext() {
        List<FlashSaleResponse> list = flashSaleService.getAll();

        return list.stream()
                .filter(f -> Boolean.TRUE.equals(f.getIsActive()))
                .limit(10)
                .map(f -> "🔥 " + f.getTitle() +
                        " | Giảm: " + f.getDiscountValue() +
                        " | Món: " + String.join(", ", f.getMenuNames()))
                .reduce("FLASH SALE:\n", (a, b) -> a + b + "\n");
    }

    private String buildFlashTimeContext() {
        List<FlashSaleResponse> list = flashSaleService.getAll();

        return list.stream()
                .filter(f -> Boolean.TRUE.equals(f.getIsActive()))
                .limit(10)
                .map(f -> "⏰ " + f.getTitle()
                        + " | Từ: " + f.getStartTime()
                        + " → " + f.getEndTime())
                .reduce("THỜI GIAN FLASH SALE:\n", (a, b) -> a + b + "\n");
    }

    // review
    private String buildReviewContext() {
        List<MenuResponse> menus = menuService.getListMenu();

        StringBuilder context = new StringBuilder("REVIEW:\n");

        menus.stream()
                .sorted((a, b) -> Float.compare(b.getRating(), a.getRating()))
                .limit(5)
                .forEach(menu -> {
                    List<ReviewResponse> reviews =
                            reviewService.getReviews(menu.getId(), 4.0f);

                    context.append("🍔 ").append(menu.getName())
                            .append(" ⭐ ").append(menu.getRating()).append("\n");

                    reviews.stream().limit(2).forEach(r ->
                            context.append(" - ").append(r.getComment()).append("\n")
                    );
                });

        return context.toString();
    }

    private String buildDeliveryContext(String message) {
        List<WardResponse> wards = wardService.getAllWardDelivery();

        String msg = message.toLowerCase();

        Optional<WardResponse> matched = wards.stream()
                .filter(w -> msg.contains(w.getName().toLowerCase()))
                .findFirst();

        if (matched.isPresent()) {
            WardResponse w = matched.get();
            return "🚚 GIAO HÀNG:\n"
                    + "- " + w.getName()
                    + (w.isDelivery() ? " ✔️ Có giao" : " ❌ Không giao")
                    + " | Phí ship: " + w.getShippingFee() + "đ";
        }

        // fallback: list chung
        return buildDeliveryContext();
    }

    private String buildDeliveryContext() {
        List<WardResponse> wards = wardService.getAllWardDelivery();

        if (wards == null || wards.isEmpty()) {
            return "GIAO HÀNG:\nHiện chưa có khu vực giao hàng.";
        }

        StringBuilder context = new StringBuilder("🚚 GIAO HÀNG:\n");

        wards.stream()
                .limit(15)
                .forEach(w -> context.append("- ")
                        .append(w.getName())
                        .append(" | Phí ship: ").append(w.getShippingFee()).append("đ\n")
                );

        return context.toString();
    }

    // gemini
    private String callGemini(String prompt) throws Exception {

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(requestBody, headers);

        String url = geminiApiUrl + "?key=" + geminiApiKey;

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );

        JsonNode json = objectMapper.readTree(response.getBody());

        return json
                .path("candidates")
                .get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text")
                .asText();
    }
}