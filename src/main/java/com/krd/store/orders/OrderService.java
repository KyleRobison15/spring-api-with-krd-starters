package com.krd.store.orders;

import com.krd.store.auth.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final AuthService authService;
    private final OrderMapper orderMapper;

    public List<OrderDto> getAllOrders() {
        var user = authService.getCurrentUser();
        var orders = orderRepository.getOrdersByCustomer(user);
        return orders.stream()
                .map(orderMapper::toDto)
                .toList();
    }

    public OrderDto getOrderById(Long id) {
        var user = authService.getCurrentUser();
        var order = orderRepository.getOrderWithItems(id).orElseThrow(OrderNotFoundException::new);

        if(!order.isPlacedBy(user)) {
            throw new AccessDeniedException("You do not have permission to access this order.");
        }

        return orderMapper.toDto(order);
    }

}
