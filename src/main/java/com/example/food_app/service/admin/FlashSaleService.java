package com.example.food_app.service.admin;

import com.example.food_app.dto.request.admin.FlashSaleRequest;
import com.example.food_app.dto.response.admin.FlashSaleResponse;
import com.example.food_app.entity.FlashSale;
import com.example.food_app.entity.Menu;
import com.example.food_app.repository.FlashSaleRepository;
import com.example.food_app.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlashSaleService {

    private final FlashSaleRepository flashSaleRepository;
    private final MenuRepository menuRepository;

    public FlashSaleResponse create(FlashSaleRequest request) {

        FlashSale fs = new FlashSale();

        mapToEntity(fs, request);

        return mapToResponse(flashSaleRepository.save(fs));
    }

    public FlashSaleResponse update(BigInteger id, FlashSaleRequest request) {

        FlashSale fs = flashSaleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy flash sale"));

        mapToEntity(fs, request);

        return mapToResponse(flashSaleRepository.save(fs));
    }

    public void delete(BigInteger id) {
        flashSaleRepository.deleteById(id);
    }

    public List<FlashSaleResponse> getAll() {
        return flashSaleRepository.findAll()
                .stream().map(this::mapToResponse).toList();
    }

    private void mapToEntity(FlashSale fs, FlashSaleRequest r) {

        fs.setTitle(r.getTitle());
        fs.setDescription(r.getDescription());

        fs.setDiscountType(r.getDiscountType());
        fs.setDiscountValue(r.getDiscountValue());

        fs.setMaxDiscount(r.getMaxDiscount());
        fs.setMinOrderAmount(r.getMinOrderAmount());

        fs.setMaxQuantityPerOrder(r.getMaxQuantityPerOrder());
        fs.setMinStock(r.getMinStock());

        fs.setStartTime(r.getStartTime());
        fs.setEndTime(r.getEndTime());

        fs.setIsActive(r.getIsActive());

        if (r.getMenuIds() != null) {
            List<Menu> menus = menuRepository.findAllById(r.getMenuIds());
            fs.setItems(menus);
        }
    }

    private FlashSaleResponse mapToResponse(FlashSale fs) {

        return FlashSaleResponse.builder()
                .flashSaleId(fs.getFlashSaleId())
                .title(fs.getTitle())
                .discountType(fs.getDiscountType())
                .discountValue(fs.getDiscountValue())
                .maxDiscount(fs.getMaxDiscount())
                .startTime(fs.getStartTime())
                .endTime(fs.getEndTime())
                .isActive(fs.getIsActive())
                .menuNames(
                        fs.getItems() != null
                                ? fs.getItems().stream().map(Menu::getName).toList()
                                : List.of()
                )
                .build();
    }
}