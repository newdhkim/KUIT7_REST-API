package com.kuit.baemin.service;

import com.kuit.baemin.domain.Address.Address;
import com.kuit.baemin.domain.Menu.Menu;
import com.kuit.baemin.domain.Order.Order;
import com.kuit.baemin.domain.Order.OrderStatus;
import com.kuit.baemin.domain.OrderMenu.OrderMenu;
import com.kuit.baemin.domain.OrderMenu.OrderMenuStatus;
import com.kuit.baemin.dto.request.OrderMenuReq;
import com.kuit.baemin.dto.request.OrderReq;
import com.kuit.baemin.dto.response.OrderRes;
import com.kuit.baemin.exception.*;
import com.kuit.baemin.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.kuit.baemin.exception.errorcode.ErrorStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMenuRepository orderMenuRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuRepository menuRepository;
    private final AddressRepository addressRepository;

    /**
     * 주문 상세 조회 (주문한 모든 메뉴 목록 조회)
     */
    public OrderRes getOrder(Long orderId, Long userId) {

        // 사용자의 주문인지 검증
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(ORDER_NOT_FOUND));
        if (!order.getUser()
                .getId()
                .equals(userId)) {
            throw new OrderException(ORDER_NOT_OWNER);
        }
        return OrderRes.from(order, orderMenuRepository.findByOrderId(orderId));
    }

    /**
     * 주문 생성 (주문 요청)
     */
    @Transactional
    public Long createOrder(OrderReq req) {

        // 사용자의 주소인지 검증
        Address address = addressRepository.findById(req.getAddressId())
                .orElseThrow(() -> new AddressException(ADDRESS_NOT_FOUND));
        if (!address.getUser()
                .getId()
                .equals(req.getUserId())) {
            throw new AddressException(ADDRESS_NOT_OWNER);
        }

        Order order = Order.builder()
                .status(OrderStatus.PENDING)
                .user(userRepository.findById(req.getUserId())
                        .orElseThrow(() -> new UserException(USER_NOT_FOUND)))
                .restaurant((restaurantRepository.findById(req.getRestaurantId()))
                        .orElseThrow(() -> new RestaurantException(RESTAURANT_NOT_FOUND)))
                .address(address)
                .build();

        Order saved =  orderRepository.save(order);

        // 주문 메뉴 저장
        for (OrderMenuReq menuReq : req.getMenus()) {
            Menu menu = menuRepository.findById(menuReq.getMenuId())
                    .orElseThrow(() -> new MenuException(MENU_NOT_FOUND));

            // 메뉴가 해당 식당의 메뉴인지 검증
            if (!menu.getRestaurant()
                    .getId()
                    .equals(req.getRestaurantId())) {
                throw new MenuException(MENU_RESTAURANT_MISMATCH);
            }

            OrderMenu orderMenu = OrderMenu.builder()
                    .order(saved)
                    .menu(menu)
                    .price(menu.getPrice())
                    .quantity(menuReq.getQuantity())
                    .status(OrderMenuStatus.ACTIVE)
                    .build();

            orderMenuRepository.save(orderMenu);
        }

        return saved.getId();
    }
}
