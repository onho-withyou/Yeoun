package com.yeoun.sales.repository;

import com.yeoun.sales.entity.Shipment;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShipmentRepository extends JpaRepository<Shipment, String> {

    /** 최근 SHIPMENT_ID 조회 **/
    @Query("""
        SELECT s.shipmentId
        FROM Shipment s
        WHERE s.shipmentId LIKE :prefix%
        ORDER BY s.shipmentId DESC
        LIMIT 1
    """)
    String findLastId(String prefix);

    boolean existsByOrderId(String orderId); // 이미 예약된 수주인지 확인

    // shipmentId로 조회
	Optional<Shipment> findByShipmentId(String shipmentId);
	
	
	//출하 예약 처리
	@Modifying
	@Query("""
	    UPDATE Shipment s
	    SET s.shipmentStatus = 'RESERVED'
	    WHERE s.shipmentId = :shipmentId
	""")
	    void updateStatusToReserved(String shipmentId);

}
