package com.yeoun.order.service;

import java.util.ArrayList;
import java.util.List;

import com.yeoun.order.dto.WorkOrderSearchDTO;
import org.springframework.stereotype.Service;

import com.yeoun.emp.dto.EmpListDTO;
import com.yeoun.masterData.dto.BomMstDTO;
import com.yeoun.masterData.entity.BomMst;
import com.yeoun.masterData.entity.ProdLine;
import com.yeoun.masterData.entity.ProductMst;
import com.yeoun.masterData.repository.BomMstRepository;
import com.yeoun.masterData.repository.ProdLineRepository;
import com.yeoun.masterData.repository.ProductMstRepository;
import com.yeoun.order.dto.WorkOrderListDTO;
import com.yeoun.order.mapper.OrderMapper;
import com.yeoun.production.dto.ProductionPlanListDTO;
import com.yeoun.production.entity.ProductionPlan;
import com.yeoun.production.repository.ProductionPlanRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class OrderService {
	
	private final OrderMapper orderMapper;
	private final BomMstRepository bomMstRepository;
	private final ProdLineRepository prodLineRepository;
	private final ProductMstRepository productMstRepository;
	private final ProductionPlanRepository productionPlanRepository;
	
	// =======================================================
	// 작업지시 목록 조회
	public List<WorkOrderListDTO> loadAllOrders (WorkOrderSearchDTO dto){
		return orderMapper.selectOrderList(dto);
	}
	
	// =======================================================
	// 생산계획 조회
	public List<ProductionPlanListDTO> loadAllPlans () {
		List<ProductionPlanListDTO> list = new ArrayList<>();
		List<ProductionPlanListDTO> plans = productionPlanRepository.findPlanList();
		for (ProductionPlanListDTO plan : plans) {
			if (plan.getStatus().equals("PLANNING")) list.add(plan);
		}
		return list; 
	}
	
	// =======================================================
	// 품목 조회
	public List<ProductMst> loadAllProducts () {
		return productMstRepository.findAll();
	}
	
	// =======================================================
	// 라인 조회
	public List<ProdLine> loadAllLines() {
		return prodLineRepository.findAll();
	}
	
	// =======================================================
	// 작업자 조회
	public List<EmpListDTO> loadAllWorkers(String pos) {
		return orderMapper.selectWorkers(pos);
	}
	
	// =======================================================
	// BOM 조회 (여기서부터 재고까지 보류)
	public List<BomMstDTO> loadBom(String prdId) {
		List<BomMst> bomMst = bomMstRepository.findByPrdId(prdId);
		return bomMst.stream()
				.map(mst -> new BomMstDTO(
						mst.getBomId(),
						mst.getPrdId(),
						mst.getMatId(),
						mst.getMatQty(),
						mst.getMatUnit(),
						mst.getBomSeqNo(),
						mst.getCreatedId(),
						mst.getCreatedDate(),
						mst.getUpdatedId(),
						mst.getUpdatedDate()
					))
				.toList();
	}
	

}









