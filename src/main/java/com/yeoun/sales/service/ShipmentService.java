package com.yeoun.sales.service;

import com.yeoun.sales.dto.ShipmentListDTO;
import com.yeoun.sales.entity.Orders;
import com.yeoun.sales.entity.Shipment;
import com.yeoun.sales.enums.ShipmentStatus;
import com.yeoun.sales.repository.OrdersRepository;
import com.yeoun.sales.repository.ShipmentRepository;
import com.yeoun.sales.repository.ShipmentQueryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrdersRepository ordersRepository;
    private final ShipmentQueryRepository shipmentQueryRepository;


    /** ================================
     *  출하 목록 조회 
     * ================================ */
    public List<ShipmentListDTO> search(
            String startDate,
            String endDate,
            String keyword,
            String status
    ) {
        return shipmentQueryRepository.search(startDate, endDate, keyword, status);
    }


    /** ================================
     *  출하 예약 생성 (WAITING → RESERVED)
     * ================================ */
    @Transactional
    public String reserveShipment(String orderId, String empId) {

        // 이미 출하생성 되어있는지 확인
        if (shipmentRepository.existsByOrderId(orderId)) {
            throw new IllegalArgumentException("이미 출하 예약된 주문입니다.");
        }

        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("수주를 찾을 수 없습니다."));

        // 출하 ID 생성
        String shipmentId = generateShipmentId();

        Shipment shipment = Shipment.builder()
                .shipmentId(shipmentId)
                .orderId(orderId)
                .shipmentDate(LocalDate.now())
                .shipmentStatus(ShipmentStatus.RESERVED)   // ⭐ enum 활용
                .empId(empId)
                .clientName(order.getClient().getClientName())
                .clientId(order.getClient().getClientId())
                .memo("출하 예약")
                .createdAt(LocalDateTime.now())
                .build();

        shipmentRepository.save(shipment);

        return shipmentId;
    }



    /** ================================
     *  출하상태 변경 (버튼 클릭 시)
     * ================================ */
    @Transactional
    public void updateShipmentStatusToReserved(String shipmentId) {

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("출고지시 없음"));

        // 상태 변경
        shipment.setShipmentStatus(ShipmentStatus.RESERVED);

        log.info("출하 상태 변경 완료 → shipmentId={}", shipmentId);
    }



    /** ================================
     *  출하 ID 생성 로직
     * ================================ */
    private String generateShipmentId() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "SHP" + today + "-";

        String lastId = shipmentRepository.findLastId(prefix);

        int seq = (lastId == null)
                ? 1
                : Integer.parseInt(lastId.substring(lastId.lastIndexOf("-") + 1)) + 1;

        return prefix + String.format("%04d", seq);
    }

}
