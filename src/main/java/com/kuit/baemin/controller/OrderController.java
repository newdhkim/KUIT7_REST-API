package com.kuit.baemin.controller;

import com.kuit.baemin.common.dto.ApiResponse;
import com.kuit.baemin.dto.request.OrderReq;
import com.kuit.baemin.dto.response.OrderRes;
import com.kuit.baemin.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * GET /orders/{id} - 주문 상세 조회
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "주문 상세 조회")
    public ApiResponse<OrderRes> getOrderById(@PathVariable Long orderId,
                                              @RequestParam Long userId) {

        return ApiResponse.of(orderService.getOrder(orderId, userId));
    }

    /**
     * POST /orders - 주문 생성 (주문 요청)
     */
    @PostMapping
    @Operation(summary = "주문 요청")
    public ApiResponse<Long> createOrder(@Valid @RequestBody OrderReq orderReq) {

        return ApiResponse.onSuccess(orderService.createOrder(orderReq));
    }
}
