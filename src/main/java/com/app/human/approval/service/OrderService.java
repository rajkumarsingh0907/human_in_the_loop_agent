package com.app.human.approval.service;

import com.app.human.approval.model.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class OrderService {

    private final Map<String, Order> orders = Map.of(
            "ORD-1001",
            new Order("ORD-1001", 499900, "INR"),

            "ORD-1002",
            new Order("ORD-1002", 129900, "INR")
    );

    public Order get(String orderId) {
        Order order = orders.get(orderId);

        if (order == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Order not found"
            );
        }

        return order;
    }

}
