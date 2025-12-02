package com.yeoun.sales.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.yeoun.sales.dto.OrderListDTO;
import com.yeoun.sales.repository.OrdersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrdersService {

    private final OrdersRepository ordersRepository;

    public List<OrderListDTO> search(
            String status,
            LocalDate startDate,
            LocalDate endDate,
            String keyword
    ) {
        return ordersRepository.searchOrders(status, startDate, endDate, keyword);
    }
}
