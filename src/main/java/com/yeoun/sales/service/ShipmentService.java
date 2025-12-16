package com.yeoun.sales.service;

import com.yeoun.outbound.service.OutboundService;
import com.yeoun.sales.dto.ShipmentListDTO;
import com.yeoun.sales.entity.OrderItem;
import com.yeoun.sales.entity.Orders;
import com.yeoun.sales.entity.Shipment;
import com.yeoun.sales.entity.ShipmentItem;
import com.yeoun.sales.enums.ShipmentStatus;
import com.yeoun.sales.repository.OrderItemRepository;
import com.yeoun.sales.repository.OrdersRepository;
import com.yeoun.sales.repository.ShipmentItemRepository;
import com.yeoun.sales.repository.ShipmentRepository;
import com.yeoun.sales.repository.ShipmentQueryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentItemRepository shipmentItemRepository;
    private final OrdersRepository ordersRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShipmentQueryRepository shipmentQueryRepository;
    private final OutboundService outboundService;
    
    private static final SecureRandom RANDOM = new SecureRandom();


    // ================================
    // 출하 목록 조회
    // ================================
    public List<ShipmentListDTO> search(
            String startDate,
            String endDate,
            String keyword,
            List<String> statusList
    ) {
        return shipmentQueryRepository.search(startDate, endDate, keyword, statusList);
    }

 // ================================
 // 출하 예약 생성 (WAITING → RESERVED)
 // ================================
 @Transactional
 public String reserveShipment(String orderId, String empId) {

     // ✅ 0) 실제로 "예약 중 / 출고 중 / 완료" 상태만 차단
     boolean alreadyReserved =
             shipmentRepository.existsByOrderIdAndShipmentStatusIn(
                     orderId,
                     List.of(
                             ShipmentStatus.RESERVED,
                             ShipmentStatus.PENDING,
                             ShipmentStatus.SHIPPED
                     )
             );

     if (alreadyReserved) {
         throw new IllegalArgumentException("이미 출하 예약된 주문입니다.");
     }

     // 1) 주문 정보 조회
     Orders order = ordersRepository.findById(orderId)
             .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다."));

     // 2) SHIPMENT_ID 생성
     String shipmentId = generateShipmentId();

     // 3) Shipment 저장
     Shipment shipment = Shipment.builder()
             .shipmentId(shipmentId)
             .orderId(orderId)
             .clientId(order.getClient().getClientId())
             .clientName(order.getClient().getClientName())
             .shipmentDate(LocalDate.now())
             .shipmentStatus(ShipmentStatus.RESERVED)
             .empId(empId)
             .memo("출하 예약 생성")
             .createdAt(LocalDateTime.now())
             .build();

     shipmentRepository.save(shipment);

     // 4) ShipmentItem 자동 생성
     List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

     for (OrderItem oi : orderItems) {
         ShipmentItem item = ShipmentItem.builder()
                 .shipmentId(shipmentId)
                 .prdId(oi.getPrdId())
                 .lotQty(oi.getOrderQty())
                 .build();

         shipmentItemRepository.save(item);
     }

     log.info("출하 예약 완료 → shipmentId={}, orderId={}", shipmentId, orderId);

     return shipmentId;
 }



    // ================================
    // 출하 상태 변경
    // ================================
    @Transactional
    public void updateShipmentStatusToReserved(String shipmentId) {

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("출하 데이터 없음"));

        shipment.setShipmentStatus(ShipmentStatus.RESERVED);

        log.info("출하 상태 변경 완료 → shipmentId={}", shipmentId);
    }


    // ================================
    // 출하 ID 생성 로직
    // SHP + yyyyMMdd + - + 4자리 Sequence
    // ================================
    private String generateShipmentId() {

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "SHP" + today + "-";

        String lastId = shipmentRepository.findLastId(prefix);

        int seq = (lastId == null)
                ? 1
                : Integer.parseInt(lastId.substring(lastId.lastIndexOf("-") + 1)) + 1;

        return prefix + String.format("%04d", seq);
    }
    
 // ================================
    // 출하 예약 취소
    // (RESERVED / PENDING → WAITING)
    // ================================
    @Transactional
    public void cancelShipment(String orderId) {

        // 1️⃣ 출하지시서 조회
        Shipment shipment = shipmentRepository
            .findByOrderIdAndShipmentStatusIn(
                orderId,
                List.of(
                    ShipmentStatus.RESERVED,
                    ShipmentStatus.PENDING
                )
            )
            .orElseThrow(() -> new IllegalStateException("취소 가능한 출하 예약이 없습니다."));

        // 2️⃣ 완제품 출고 취소 (재고 예정수량 복구 + OUTBOUND → CANCELED)
        outboundService.canceledProductOutbound(shipment.getShipmentId());

        // 3️⃣ 출하지시 상태 → WAITING (재예약 가능)
        shipment.changeStatus(ShipmentStatus.WAITING);
    }
    
 // ================================
 // 출하 확정 + 운송장번호(TRACKING_NUMBER) 생성
 // ================================
 @Transactional
 public void confirmShipment(String shipmentId, String empId) {

     Shipment shipment = shipmentRepository.findById(shipmentId)
         .orElseThrow(() -> new IllegalArgumentException("출하 데이터 없음"));

     // 이미 출하 완료면 차단
     if (shipment.getShipmentStatus() == ShipmentStatus.SHIPPED) {
         throw new IllegalStateException("이미 출하 완료된 건입니다.");
     }

     // ⭐ TRACKING_NUMBER 없을 때만 생성
     if (shipment.getTrackingNumber() == null) {
         shipment.setTrackingNumber(generateTrackingNumber());
     }

     shipment.setShipmentStatus(ShipmentStatus.SHIPPED);
     shipment.setShipmentDate(LocalDate.now());
     shipment.setEmpId(empId);
 }
 
//================================
//TRACKING_NUMBER 생성
//TRK + yyyyMMdd + - + 4자리
//================================
 private String generateTrackingNumber() {

	    String today = LocalDate.now()
	            .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

	    int randomNumber = RANDOM.nextInt(900000) + 100000; // 100000 ~ 999999

	    return "TRK" + today + "-" + randomNumber;
	}


}
