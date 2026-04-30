package com.example.food_app.service.user;

import com.example.food_app.dto.request.user.CreateReviewRequest;
import com.example.food_app.dto.request.user.ReviewItemRequest;
import com.example.food_app.entity.Account;
import com.example.food_app.entity.Order;
import com.example.food_app.entity.OrderDetail;
import com.example.food_app.entity.Review;
import com.example.food_app.entity.enums.OrderStatus;
import com.example.food_app.repository.OrderDetailRepository;
import com.example.food_app.repository.OrderRepository;
import com.example.food_app.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    @Transactional
    public void createReview(CreateReviewRequest request, Account account) {

        // 1. Lấy order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // 2. Check quyền
        if (!order.getAccount().getAccountId().equals(account.getAccountId())) {
            throw new RuntimeException("Không có quyền");
        }

        // 3. Check trạng thái
        if (order.getOrderStatus() != OrderStatus.COMPLETED) {
            throw new RuntimeException("Chỉ được đánh giá đơn đã hoàn thành");
        }

        // 4. Loop từng item
        for (ReviewItemRequest item : request.getReviews()) {

            OrderDetail detail = orderDetailRepository.findById(item.getOrderDetailId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy item"));

            // check thuộc order
            if (!detail.getOrder().getOrderId().equals(order.getOrderId())) {
                throw new RuntimeException("Item không thuộc đơn hàng");
            }

            // check đã review chưa
            if (reviewRepository.existsByAccountAndOrderDetail(account, detail)) {
                throw new RuntimeException("Bạn đã đánh giá món này rồi");
            }

            // validate rating
            if (item.getRating() < 1 || item.getRating() > 5) {
                throw new RuntimeException("Rating phải từ 1-5");
            }

            // tạo review
            Review review = new Review();
            review.setOrder(order);
            review.setOrderDetail(detail);
            review.setMenu(detail.getMenu());
            review.setAccount(account);
            review.setRating(item.getRating());
            review.setComment(item.getComment());

            reviewRepository.save(review);
        }
    }
}