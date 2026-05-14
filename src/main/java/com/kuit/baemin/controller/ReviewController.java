    package com.kuit.baemin.controller;

    import com.kuit.baemin.common.dto.ApiResponse;
    import com.kuit.baemin.domain.Review.Review;
    import com.kuit.baemin.dto.request.ReviewReq;
    import com.kuit.baemin.service.ReviewService;
    import io.swagger.v3.oas.annotations.Operation;
    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor;
    import org.springframework.web.bind.annotation.*;

    @RestController
    @RequestMapping("/orders/{orderId}/reviews")
    @RequiredArgsConstructor
    public class ReviewController {

        private final ReviewService reviewService;

        /**
         * POST /orders/{id}/reviews - 리뷰 작성
         */
        @PostMapping
        @Operation(summary = "리뷰 작성")
        public ApiResponse<Long> createReview(@Valid @RequestBody ReviewReq req,
                                              @PathVariable Long orderId) {
            return ApiResponse.onSuccess(reviewService.save(req, orderId));
        }

        /**
         * DELETE /orders/{id}/reviews/{reviewId} - 리뷰 삭제
         */
        @DeleteMapping("/{reviewId}")
        @Operation(summary = "리뷰 삭제")
        public ApiResponse<Void> deleteReview(@PathVariable Long reviewId,
                                              @PathVariable Long orderId,
                                              @RequestParam Long userId) {

            reviewService.delete(reviewId, orderId, userId);
            return ApiResponse.onSuccess(null);
        }
    }
