package com.example.food_app.service.user;

import com.example.food_app.dto.request.user.ProfileUpdateRequest;
import com.example.food_app.dto.response.user.OrderByUserResponse;
import com.example.food_app.dto.response.user.ProfileResponse;
import com.example.food_app.entity.Account;
import com.example.food_app.entity.Order;
import com.example.food_app.entity.OrderDetail;
import com.example.food_app.entity.Topping;
import com.example.food_app.repository.AccountRepository;
import com.example.food_app.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AccountRepository accountRepository;
    private final Cloudinary cloudinary;

    public ProfileResponse getProfile(Account account) {

        if (account == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Chưa đăng nhập"
            );
        }

        Account acc = accountRepository.findByEmailAndIsActiveIsTrue(account.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy tài khoản"
                ));

        return mapToResponse(acc);
    }

    public ProfileResponse updateProfile(
            Account account,
            ProfileUpdateRequest request,
            MultipartFile file
    ) {

        if (account == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Chưa đăng nhập"
            );
        }

        Account acc = accountRepository.findById(account.getAccountId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy tài khoản"
                ));

        if (request.getFullName() != null) {
            if (request.getFullName().trim().isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Tên không được để trống"
                );
            }
            acc.setFullName(request.getFullName());
        }

        if (request.getPhone() != null) {
            if (!request.getPhone().matches("^(0[0-9]{9})$")) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Số điện thoại không hợp lệ"
                );
            }
            acc.setPhone(request.getPhone());
        }

        if (file != null && !file.isEmpty()) {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "File phải là ảnh"
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
                        ObjectUtils.asMap(
                                "folder", "avatars"
                        )
                );

                String imageUrl = uploadResult.get("secure_url").toString();

                acc.setAvtUrl(imageUrl);

            } catch (IOException e) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Upload ảnh thất bại"
                );
            }
        }

        try {
            accountRepository.save(acc);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Lỗi khi cập nhật dữ liệu"
            );
        }

        return mapToResponse(acc);
    }

    private ProfileResponse mapToResponse(Account account) {
        return ProfileResponse.builder()
                .accountId(account.getAccountId())
                .fullName(account.getFullName())
                .email(account.getEmail())
                .phone(account.getPhone())
                .avtUrl(account.getAvtUrl())
                .createdAt(account.getCreatedAt())
                .build();
    }

    private final OrderRepository orderRepository;

    public List<OrderByUserResponse> getMyOrders(Account account) {
        List<Order> orders = orderRepository.findByAccountOrderByCreatedAtDesc(account);

        if (orders.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Bạn chưa đặt đơn nào"
            );
        }

        return orders.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public OrderByUserResponse getMyOrderDetail(
            BigInteger orderId,
            Account account
    ) {
        Order order = orderRepository.findByOrderIdAndAccount(orderId, account)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy đơn hàng"
                ));

        return mapToResponse(order);
    }

    private OrderByUserResponse mapToResponse(Order order) {

        List<OrderByUserResponse.OrderItemResponse> items =
                order.getOrderDetails().stream()
                        .map(this::mapItem)
                        .toList();

        return OrderByUserResponse.builder()
                .orderId(order.getOrderId())
                .address(order.getAddressDetail())
                .wardName(order.getWard().getName())
                .totalAmount(order.getTotalAmount())
                .shippingFee(order.getShippingFee())
                .finalAmount(order.getFinalAmount())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }

    private OrderByUserResponse.OrderItemResponse mapItem(OrderDetail detail) {

        String sizeName = detail.getMenuSize() != null
                ? detail.getMenuSize().getSizeName()
                : null;

        List<String> toppings = detail.getToppings()
                .stream()
                .map(Topping::getName)
                .toList();

        return OrderByUserResponse.OrderItemResponse.builder()
                .menuName(detail.getMenu().getName())
                .sizeName(sizeName)
                .toppings(toppings)
                .quantity(detail.getQuantity())
                .itemTotal(detail.getItemTotalPrice())
                .build();
    }
}