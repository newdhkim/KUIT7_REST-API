package com.kuit.baemin.service;

import com.kuit.baemin.domain.Order.Order;
import com.kuit.baemin.domain.Review.Review;
import com.kuit.baemin.domain.Review.ReviewStatus;
import com.kuit.baemin.dto.request.ReviewReq;
import com.kuit.baemin.exception.OrderException;
import com.kuit.baemin.exception.RestaurantException;
import com.kuit.baemin.exception.ReviewException;
import com.kuit.baemin.exception.UserException;
import com.kuit.baemin.exception.errorcode.ErrorStatus;
import com.kuit.baemin.repository.OrderRepository;
import com.kuit.baemin.repository.RestaurantRepository;
import com.kuit.baemin.repository.ReviewRepository;
import com.kuit.baemin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.kuit.baemin.exception.errorcode.ErrorStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;

    /**
     * 리뷰 작성
     */
    @Transactional
    public Long save(ReviewReq req, Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(ORDER_NOT_FOUND));

        // 본인의 주문인지 검증
        if (!order.getUser()
                .getId()
                .equals(req.getUserId())) {
            throw new ReviewException(REVIEW_NOT_OWNER);
        }

        // 주문한 식당과 리뷰 식당이 일치하는지 검증
        if (!order.getRestaurant()
                .getId()
                .equals(req.getRestaurantId())) {
            throw new ReviewException(REVIEW_RESTAURANT_MISMATCH);
        }

        Review review = Review.builder()
                .content(req.getContent())
                .rating(req.getRating())
                .status(ReviewStatus.ACTIVE)
                .user(userRepository.findById(req.getUserId())
                        .orElseThrow(() -> new UserException(USER_NOT_FOUND)))
                .restaurant(restaurantRepository.findById(req.getRestaurantId())
                        .orElseThrow(() -> new RestaurantException(RESTAURANT_NOT_FOUND)))
                .order(order)
                .build();

        Review saved = reviewRepository.save(review);
        return saved.getId();
    }

    /**
     * 리뷰 삭제
     */
    @Transactional
    public void delete(Long reviewId, Long orderId, Long userId) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewException(REVIEW_NOT_FOUND));

        // 해당 리뷰가 본인이 쓴 것인지 검증
        if (!review.getUser()
                .getId()
                .equals(userId)) {
            throw new ReviewException(REVIEW_NOT_OWNER);
        }

        // 본인의 주문인지 검증
        if (!review.getOrder()
                .getId()
                .equals(orderId)) {
            throw new ReviewException(REVIEW_ORDER_MISMATCH);
        }

        review.updateStatus(ReviewStatus.INACTIVE);
    }
}
