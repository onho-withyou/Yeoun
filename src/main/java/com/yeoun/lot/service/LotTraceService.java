package com.yeoun.lot.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.yeoun.emp.entity.Emp;
import com.yeoun.emp.repository.EmpRepository;
import com.yeoun.lot.dto.LotHistoryDTO;
import com.yeoun.lot.dto.LotMasterDTO;
import com.yeoun.lot.entity.LotHistory;
import com.yeoun.lot.entity.LotMaster;
import com.yeoun.lot.repository.LotHistoryRepository;
import com.yeoun.lot.repository.LotMasterRepository;
import com.yeoun.masterData.entity.ProcessMst;
import com.yeoun.masterData.repository.ProcessMstRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor
public class LotTraceService {
	private final LotMasterRepository lotMasterRepository;
	private final LotHistoryRepository historyRepository;
	private final ProcessMstRepository processMstRepository;
	private final EmpRepository empRepository;
	
	// LOT 생성
	@Transactional
	public String registLotMaster(LotMasterDTO lotMasterDTO, String matId, String line) {
		
		log.info(">>>>>> lotMasterDTO " + lotMasterDTO);
		
		String prdId = null;
		
		if ("RAW".equals(lotMasterDTO.getLotType()) || "SUB".equals(lotMasterDTO.getLotType()) ||
				"PKG".equals(lotMasterDTO.getLotType())) {
			prdId = matId;
		} else {
			prdId = lotMasterDTO.getPrdId();
		}
		
		// Lot번호 생성
		String LotNo = generateLotId(lotMasterDTO.getLotType(), prdId, line, LocalDate.now());
		
		lotMasterDTO.setLotNo(LotNo);
		
		LotMaster lotMaster = lotMasterDTO.toEntity();
		
		lotMasterRepository.save(lotMaster);
				
		return LotNo;
	}
	
	//Lot 이력 생성
	@Transactional
	public void registLotHistory(LotHistoryDTO historyDTO) {
		LotMaster lot = lotMasterRepository.findByLotNo(historyDTO.getLotNo())
				.orElseThrow(() -> new NoSuchElementException("LOT 없음"));
		
		// 엔티티로 변환
		LotHistory lotHistory = historyDTO.toEntity();
		
		lotHistory.setLot(lot);
		
		// 공정 정보 조회 후 엔티티에 주입
		if (historyDTO.getProcessId() != null) {
			ProcessMst processMst = processMstRepository.findByProcessId(historyDTO.getProcessId())
					.orElse(null);	
			lotHistory.setProcess(processMst);
		}
		
		// 직원 조회 후 엔티티에 주입
		if (historyDTO.getWorkedId() != null) {
			Emp emp = empRepository.findByEmpId(historyDTO.getWorkedId())
					.orElseThrow(() -> new NoSuchElementException("직원 없음"));
			lotHistory.setWorker(emp);
		}
		
		historyRepository.save(lotHistory);
	}
	
	// LOT번호 생성
	// 형식: [LOT유형]-[제품코드5자리]-[YYYYMMDD]-[라인]-[시퀀스3자리]
	public String generateLotId(String lotType, String prdId, String line, LocalDate date) {
		String dateStr  = date.format(DateTimeFormatter.BASIC_ISO_DATE); 
		
		// 최근 시퀀스 조회
		String lastSeq = lotMasterRepository.findLastSeq(lotType, prdId, dateStr, line);
		
		// 마지막 시퀀스 값이 없을 경우 1로 설정 값이 있으면 1씩 증가
		int next = (lastSeq == null) ? 1 : Integer.parseInt(lastSeq) + 1;
		// 시퀀스가 3자리 만들기
		String nextSeq = String.format("%03d", next);
		
		return String.format(
			"%s-%s-%s-%s-%s",	
			lotType,
			prdId,
			dateStr,
			line,
			nextSeq
		);
	}
}
