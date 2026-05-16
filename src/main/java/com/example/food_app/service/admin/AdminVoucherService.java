package com.example.food_app.service.admin;

import com.example.food_app.dto.request.admin.VoucherRequest;
import com.example.food_app.dto.response.admin.VoucherResponse;
import com.example.food_app.entity.Voucher;
import com.example.food_app.entity.enums.VoucherType;
import com.example.food_app.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminVoucherService {
    private final VoucherRepository voucherRepository;

    // CREATE
    public VoucherResponse create(VoucherRequest request) {

        if (voucherRepository.existsByCode(request.getCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code đã tồn tại");
        }

        Voucher v = mapToEntity(new Voucher(), request);

        return mapToResponse(voucherRepository.save(v));
    }

    // UPDATE
    public VoucherResponse update(BigInteger id, VoucherRequest request) {

        Voucher v = voucherRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy voucher"));

        mapToEntity(v, request);

        return mapToResponse(voucherRepository.save(v));
    }

    // DELETE
    public void delete(BigInteger id) {
        voucherRepository.deleteById(id);
    }

    // GET ALL
    public List<VoucherResponse> getAll() {
        return voucherRepository.findAll()
                .stream().map(this::mapToResponse).toList();
    }

    // MAPPER
    private Voucher mapToEntity(Voucher v, VoucherRequest r) {

        v.setCode(r.getCode());
        v.setTitle(r.getTitle());
        v.setDescription(r.getDescription());

        v.setDiscountType(r.getDiscountType());
        v.setDiscountValue(r.getDiscountValue());

        v.setMaxDiscount(r.getMaxDiscount());
        v.setMinOrderAmount(r.getMinOrderAmount());

        v.setStartDate(r.getStartDate());
        v.setEndDate(r.getEndDate());

        v.setUsageLimit(r.getUsageLimit());
        v.setIsActive(r.getIsActive());
        v.setVoucherType(VoucherType.PUBLIC);

        return v;
    }

    private VoucherResponse mapToResponse(Voucher v) {
        return VoucherResponse.builder()
                .voucherId(v.getVoucherId())
                .code(v.getCode())
                .title(v.getTitle())
                .description(v.getDescription())
                .discountType(v.getDiscountType())
                .discountValue(v.getDiscountValue())
                .maxDiscount(v.getMaxDiscount())
                .minOrderAmount(v.getMinOrderAmount())
                .startDate(v.getStartDate())
                .endDate(v.getEndDate())
                .usageLimit(v.getUsageLimit())
                .isActive(v.getIsActive())
                .build();
    }
}