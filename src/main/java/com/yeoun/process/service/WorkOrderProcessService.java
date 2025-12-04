package com.yeoun.process.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yeoun.masterData.entity.RouteHeader;
import com.yeoun.masterData.entity.RouteStep;
import com.yeoun.masterData.repository.RouteHeaderRepository;
import com.yeoun.masterData.repository.RouteStepRepository;
import com.yeoun.order.entity.WorkOrder;
import com.yeoun.order.repository.WorkOrderRepository;
import com.yeoun.process.dto.WorkOrderProcessDTO;
import com.yeoun.process.dto.WorkOrderProcessDetailDTO;
import com.yeoun.process.dto.WorkOrderProcessStepDTO;
import com.yeoun.process.entity.WorkOrderProcess;
import com.yeoun.process.repository.WorkOrderProcessRepository;
import com.yeoun.qc.repository.QcResultRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkOrderProcessService {
	
	private final WorkOrderRepository workOrderRepository;
	private final WorkOrderProcessRepository workOrderProcessRepository;
	private final RouteHeaderRepository routeHeaderRepository;
	private final RouteStepRepository routeStepRepository;
	private final QcResultRepository qcResultRepository;
	
	// ------------------------------------------------------------------------------------
	// 공정 현황 목록 (작업지시번호기준)
	public List<WorkOrderProcessDTO> getWorkOrderListForStatus() {
		
		// 1. 작업지시 목록 조회
		List<String> statuses = List.of("RELEASED");
		
		List<WorkOrder> workOrders = workOrderRepository.findByStatusIn(statuses);
		
		// 각 작업지시별로 공정진행 조회 후 DTO 변환
		return workOrders.stream()
                .map(w -> {

                    // 이 작업지시에 해당하는 공정진행 목록 (순번 순)
                    List<WorkOrderProcess> processes =
                            workOrderProcessRepository.findByWorkOrderOrderIdOrderByStepSeqAsc(w.getOrderId());

                    // ---- 1) 양품수량: 추후 QC에서 가져올 예정 ----
                    int goodQty = 0;

                    // ---- 2) 진행률: 공정 단계 기준 (DONE 개수 / 전체 단계수 * 100) ----
                    int progressRate = 0;
                    int totalSteps = processes.size();
                    if (totalSteps > 0) {
                        long doneCount = processes.stream()
                        		.filter(p -> "DONE".equals(p.getStatus()) || "QC_PENDING".equals(p.getStatus()))
                                .count();
                        progressRate = (int) Math.round(doneCount * 100.0 / totalSteps);
                    }

                    // ---- 3) 현재공정: IN_PROGRESS > READY(가장 앞) > 모두 DONE이면 "완료" ----
                    String currentProcess = "대기";

                    // IN_PROGRESS 공정 우선
                    WorkOrderProcess inProgress = processes.stream()
                            .filter(p -> "IN_PROGRESS".equals(p.getStatus()))
                            .findFirst()
                            .orElse(null);

                    if (inProgress != null) {
                        currentProcess = inProgress.getProcess().getProcessName();
                    } else {
                        // READY 중 가장 stepSeq가 작은 공정
                        WorkOrderProcess nextReady = processes.stream()
                                .filter(p -> "READY".equals(p.getStatus()))
                                .sorted((a, b) -> Integer.compare(
                                        a.getStepSeq() == null ? 0 : a.getStepSeq(),
                                        b.getStepSeq() == null ? 0 : b.getStepSeq()
                                ))
                                .findFirst()
                                .orElse(null);

                        if (nextReady != null) {
                            currentProcess = nextReady.getProcess().getProcessName();
                        } else if (totalSteps > 0 && processes.stream().allMatch(p -> "DONE".equals(p.getStatus()) || "QC_PENDING".equals(p.getStatus()))) {
                        	// 공정이 있고, 모두 DONE 또는 QC_PENDING이면
                            currentProcess = "완료";
                        }
                    }

                    // ---- 4) 경과시간: 첫 START_TIME ~ 현재시간 ----
                    String elapsedTime = "-";
                    LocalDateTime firstStart = processes.stream()
                            .map(WorkOrderProcess::getStartTime)
                            .filter(Objects::nonNull)
                            .min(LocalDateTime::compareTo)
                            .orElse(null);

                    if (firstStart != null) {
                        Duration d = Duration.between(firstStart, LocalDateTime.now());
                        long hours = d.toHours();
                        long minutes = d.toMinutesPart();
                        elapsedTime = hours + "시간 " + minutes + "분";
                    }

                    // ---- 5) 최종 DTO 생성 ----
                    WorkOrderProcessDTO dto = new WorkOrderProcessDTO();
                    dto.setOrderId(w.getOrderId());
                    dto.setPrdId(w.getProduct().getPrdId());
                    dto.setPrdName(w.getProduct().getPrdName());
                    dto.setPlanQty(w.getPlanQty());
                    dto.setStatus(w.getStatus());

                    dto.setGoodQty(goodQty);
                    dto.setProgressRate(progressRate);
                    dto.setCurrentProcess(currentProcess);
                    dto.setElapsedTime(elapsedTime);

                    return dto;
                })
                .toList();
    }

	// 공정 현황 상세
    @Transactional(readOnly = true)
    public WorkOrderProcessDetailDTO getWorkOrderProcessDetail(String orderId) {

        // 1) 작업지시 기본정보
        WorkOrder workOrder = workOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 작업지시: " + orderId));

        // 2) 제품별 라우트 찾기
        RouteHeader routeHeader = routeHeaderRepository
                .findFirstByProductAndUseYn(workOrder.getProduct(), "Y")
                .orElseThrow(() -> new IllegalArgumentException("해당 제품의 사용중인 라우트가 없습니다."));

        // 3) 라우트 단계 목록 조회 (마스터 기준 단계 정의)
        List<RouteStep> steps = routeStepRepository
                .findByRouteHeaderOrderByStepSeqAsc(routeHeader);

        // 4) 이 작업지시의 공정진행 데이터 (WORK_ORDER_PROCESS)
        List<WorkOrderProcess> processes =
                workOrderProcessRepository.findByWorkOrderOrderIdOrderByStepSeqAsc(orderId);

        // RouteStep 기준으로 WorkOrderProcess를 빠르게 찾기 위한 Map 구성
        Map<String, WorkOrderProcess> processMap = processes.stream()
                .filter(p -> p.getRouteStep() != null && p.getRouteStep().getRouteStepId() != null)
                .collect(Collectors.toMap(
                        p -> p.getRouteStep().getRouteStepId(), // String 키
                        p -> p,
                        (p1, p2) -> p1 // 혹시 중복 키가 있으면 첫 번째 것 사용
                ));

        // 5) 상단 요약 DTO
        WorkOrderProcessDTO wopDTO = new WorkOrderProcessDTO();
        wopDTO.setOrderId(workOrder.getOrderId());
        wopDTO.setPrdId(workOrder.getProduct().getPrdId());
        wopDTO.setPrdName(workOrder.getProduct().getPrdName());
        wopDTO.setPlanQty(workOrder.getPlanQty());
        wopDTO.setStatus(workOrder.getStatus());
        
        // QC 결과 확인
        boolean isQcPassed = qcResultRepository.existsByOrderIdAndOverallResult(orderId, "PASS");

        // 6) 공정 단계 DTO 리스트 (RouteStep + WorkOrderProcess 병합)
        List<WorkOrderProcessStepDTO> stepDTOs = steps.stream()
                .map(s -> {
                    WorkOrderProcessStepDTO dto = new WorkOrderProcessStepDTO();
                    dto.setStepSeq(s.getStepSeq());
                    dto.setProcessId(s.getProcess().getProcessId());
                    dto.setProcessName(s.getProcess().getProcessName());

                    // 이 라우트단계에 해당하는 공정진행 데이터가 있는지 확인
                    WorkOrderProcess proc = processMap.get(s.getRouteStepId());

                    if (proc != null) {
                        dto.setStatus(proc.getStatus());
                        dto.setStartTime(proc.getStartTime());
                        dto.setEndTime(proc.getEndTime());
                        dto.setGoodQty(proc.getGoodQty());
                        dto.setDefectQty(proc.getDefectQty());
                        dto.setMemo(proc.getMemo());
                    } else {
                        // 아직 공정진행이 생성되지 않은 단계라면 기본 READY 상태로
                        dto.setStatus("READY");
                        dto.setStartTime(null);
                        dto.setEndTime(null);
                        dto.setGoodQty(null);
                        dto.setDefectQty(null);
                        dto.setMemo(null);
                    }

                    return dto;
                })
                .toList();

        // 7) 버튼 표시 플래그 계산 (시작/종료 가능 여부)
        for (int i = 0; i < stepDTOs.size(); i++) {
            WorkOrderProcessStepDTO dto = stepDTOs.get(i);
            WorkOrderProcessStepDTO prevDto = (i > 0) ? stepDTOs.get(i - 1) : null;

            // 1. 이전 단계 완료 확인 (DONE일 때만 TRUE. QC_PENDING은 DONE으로 간주하지 않음)
            boolean prevDone = (i == 0) || "DONE".equals(prevDto.getStatus());
            
            // 2. 이전 단계의 상태가 QC_PENDING인지 확인 (이 상태는 다음 공정 시작을 블로킹함)
            boolean isPrevStatusBlocking = (prevDto != null) && 
                                           ("QC_PENDING".equals(prevDto.getStatus()));

            // ----------------------------------------------------------------------
            // [수정] 포장 공정 시작 조건 강화 (포장 공정 ID를 'PRC-PACK'로 가정)
            // ----------------------------------------------------------------------
            if ("PRC-PACK".equals(dto.getProcessId())) { 
                
                // 포장 공정은 READY 상태이고 AND QC가 합격('PASS')했을 때만 시작 가능
                // 이전 단계 완료 여부는 isQcPassed를 통해 간접적으로 검증됨.
                dto.setCanStart("READY".equals(dto.getStatus()) && isQcPassed);
                
            } else { 
            
                // 일반 공정은 이전 단계가 DONE이어야 하고, QC_PENDING 상태가 아니어야 시작 가능
                dto.setCanStart("READY".equals(dto.getStatus()) && prevDone && !isPrevStatusBlocking);
            }

            // IN_PROGRESS 상태인 단계만 종료 가능 (변동 없음)
            dto.setCanFinish("IN_PROGRESS".equals(dto.getStatus()));
        }

        // 8) 상세 DTO 리턴
        return new WorkOrderProcessDetailDTO(wopDTO, stepDTOs);
    }
    
    // 공정 단계 시작 처리
    @Transactional
    public WorkOrderProcessStepDTO startStep(String orderId, Integer stepSeq) {

        WorkOrderProcess proc = workOrderProcessRepository
                .findByWorkOrderOrderIdAndStepSeq(orderId, stepSeq)
                .orElseThrow(() -> new IllegalArgumentException("해당 공정 단계가 존재하지 않습니다."));

        // READY 상태만 시작 가능
        if (!"READY".equals(proc.getStatus())) {
            throw new IllegalStateException("대기 상태(READY)인 공정만 시작할 수 있습니다.");
        }

        proc.setStatus("IN_PROGRESS");
        proc.setStartTime(LocalDateTime.now());

        WorkOrderProcess saved = workOrderProcessRepository.save(proc);
        return toStepDTO(saved);
    }

    // 공정 단계 종료 처리
    @Transactional
    public WorkOrderProcessStepDTO finishStep(String orderId, Integer stepSeq) {

    	WorkOrderProcess proc = workOrderProcessRepository
                .findByWorkOrderOrderIdAndStepSeq(orderId, stepSeq)
                .orElseThrow(() -> new IllegalArgumentException("해당 공정 단계가 존재하지 않습니다."));

        // 진행중인 것만 종료 가능
        if (!"IN_PROGRESS".equals(proc.getStatus())) {
            throw new IllegalStateException("진행중(IN_PROGRESS) 상태인 공정만 종료할 수 있습니다.");
        }
        
        // QC 검사가 필요한 특정 공정 후 상태를 'QC_PENDING'으로 변경
        String processId = proc.getProcess().getProcessId();
        
       
        if ("PRC-CAP".equals(processId)) {
        	proc.setStatus("QC_PENDING");
        	
        	// QC 검사 등록 페이지로 보낼 QC 테이블에 데이터를 생성/요청하는 로직 추가
        	
        } else {
        	proc.setStatus("DONE");
        }

        proc.setEndTime(LocalDateTime.now());

        WorkOrderProcess saved = workOrderProcessRepository.save(proc);
        return toStepDTO(saved);
    }

    // 공정 메모
    @Transactional
    public WorkOrderProcessStepDTO updateStepMemo(String orderId, Integer stepSeq, String memo) {

        WorkOrderProcess proc = workOrderProcessRepository
                .findByWorkOrderOrderIdAndStepSeq(orderId, stepSeq)
                .orElseThrow(() -> new IllegalArgumentException("해당 공정 단계가 존재하지 않습니다."));

        proc.setMemo(memo);

        WorkOrderProcess saved = workOrderProcessRepository.save(proc);
        return toStepDTO(saved);
    }

    // 엔티티 -> 단계 DTO 변환 공통 메서드
    private WorkOrderProcessStepDTO toStepDTO(WorkOrderProcess proc) {
        WorkOrderProcessStepDTO dto = new WorkOrderProcessStepDTO();
        dto.setOrderId(proc.getWorkOrder().getOrderId());
        dto.setStepSeq(proc.getStepSeq());
        dto.setProcessId(proc.getProcess().getProcessId());
        dto.setProcessName(proc.getProcess().getProcessName());
        dto.setStatus(proc.getStatus());
        dto.setStartTime(proc.getStartTime());
        dto.setEndTime(proc.getEndTime());
        dto.setGoodQty(proc.getGoodQty());
        dto.setDefectQty(proc.getDefectQty());
        dto.setMemo(proc.getMemo());

        dto.setCanStart("READY".equals(proc.getStatus()));
        dto.setCanFinish("IN_PROGRESS".equals(proc.getStatus()));

        return dto;
    }


} // WorkOrderProcessService 끝