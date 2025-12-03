package com.yeoun.order.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.yeoun.emp.entity.Emp;
import com.yeoun.masterData.entity.ProdLine;
import com.yeoun.masterData.entity.ProductMst;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "WORK_ORDER")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class WorkOrder {

	// 작업 지시번호 ID
	// WO-YYYYMMDD-0000 (시퀀스)
	@Id @Column(nullable = false, length = 16)
	private String orderId;
	
	// 제품 ID
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PRD_ID", nullable = false)
	private ProductMst product;
    
    // 계획수량
    @Column(nullable = false)
    private Integer planQty;
    
    // Lot번호
//    @Column(nullable = false)
//    private String lotNo;				=================> 컬럼 삭제
    
    // 시작예정일시
    @Column(nullable = false)
    private LocalDateTime startDate;
    
    // 완료예정일시
    @Column(nullable = false)
    private LocalDateTime endDate;
    
    // 수행 라인
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "LINE_ID", nullable = false)
    private ProdLine line;
    
    // 수행 상태
    @Column(nullable = false)
    private String status;
    
    // 작성자 ID
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CREATED_ID", nullable = false)
    private Emp createdEmp;
    
    // 작성일자
    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdDate;
    
    // 수정일자
    @Column
    private LocalDateTime updatedDate;
    
    // 비고(특이사항 및 메모)
    @Column
    private String remark;
	
	
}
