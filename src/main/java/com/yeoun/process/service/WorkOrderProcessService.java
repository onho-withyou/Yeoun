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
import com.yeoun.qc.entity.QcResult;
import com.yeoun.qc.repository.QcResultRepository;

import lombok.RequiredArgsConstructor;

/**
 * 작업지시 기준 공정 진행 현황 Service
 */
@Service
@RequiredArgsConstructor
public class WorkOrderProcessService {

    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderProcessRepository workOrderProcessRepository;
    private final RouteHeaderRepository routeHeaderRepository;
    private final RouteStepRepository routeStepRepository;
    private final QcResultRepository qcResultRepository;

    // =========================================================================
    // 1. 공정현황 메인 목록
    //    - 작업지시별 진행률, 현재공정, 경과시간, 양품수량 표시
    // =========================================================================
    @Transactional(readOnly = true)
    public List<WorkOrderProcessDTO> getWorkOrderListForStatus() {

        // 1) 공정현황 대상이 되는 작업지시 조회 (예: RELEASED 상태만)
        List<String> statuses = List.of("RELEASED");
        List<WorkOrder> workOrders = workOrderRepository.findByStatusIn(statuses);

        if (workOrders.isEmpty()) {
            return List.of();
        }

        // 작업지시번호 리스트 추출
        List<String> orderIds = workOrders.stream()
                .map(WorkOrder::getOrderId)
                .toList();

        // 2) 모든 공정 데이터를 한 번에 조회 (orderId + stepSeq 순)
        List<WorkOrderProcess> allProcesses =
                workOrderProcessRepository.findByWorkOrderOrderIdInOrderByWorkOrderOrderIdAscStepSeqAsc(orderIds);

        // orderId -> 공정 리스트 맵핑
        Map<String, List<WorkOrderProcess>> processMap = allProcesses.stream()
                .collect(Collectors.groupingBy(p -> p.getWorkOrder().getOrderId()));

        // 3) 모든 QC 결과를 한 번에 조회
        List<QcResult> allQcResults = qcResultRepository.findByOrderIdIn(orderIds);

        Map<String, QcResult> qcMap = allQcResults.stream()
                .collect(Collectors.toMap(
                        QcResult::getOrderId,
                        qc -> qc,
                        (q1, q2) -> q1 // 중복 시 첫 번째 사용
                ));

        // 4) 각 작업지시를 공정현황 DTO로 변환
        return workOrders.stream()
                .map(w -> {
                    List<WorkOrderProcess> processes =
                            processMap.getOrDefault(w.getOrderId(), List.of());
                    QcResult qcResult = qcMap.get(w.getOrderId());
                    return toProcessSummaryDto(w, processes, qcResult);
                })
                .collect(Collectors.toList());
    }

    /**
     * 공정현황 목록용 요약 DTO 생성
     */
    private WorkOrderProcessDTO toProcessSummaryDto(
            WorkOrder workOrder,
            List<WorkOrderProcess> processes,
            QcResult qcResult
    ) {

        // 양품수량: 작업지시당 QC_RESULT 1건 기준
        int goodQty = 0;
        if (qcResult != null && qcResult.getGoodQty() != null) { // 필드명 맞게 수정
            goodQty = qcResult.getGoodQty();
        }

        int progressRate = calculateProgressRate(processes);
        String currentProcess = resolveCurrentProcess(processes);
        String elapsedTime = calculateElapsedTime(processes);

        WorkOrderProcessDTO dto = new WorkOrderProcessDTO();
        dto.setOrderId(workOrder.getOrderId());
        dto.setPrdId(workOrder.getProduct().getPrdId());
        dto.setPrdName(workOrder.getProduct().getPrdName());
        dto.setPlanQty(workOrder.getPlanQty());
        dto.setStatus(workOrder.getStatus());

        dto.setGoodQty(goodQty);
        dto.setProgressRate(progressRate);
        dto.setCurrentProcess(currentProcess);
        dto.setElapsedTime(elapsedTime);

        return dto;
    }

    /**
     * 진행률 계산 (DONE + QC_PENDING 단계 수 / 전체 단계 수 * 100)
     */
    private int calculateProgressRate(List<WorkOrderProcess> processes) {
        int totalSteps = processes.size();
        if (totalSteps == 0) {
            return 0;
        }

        long doneCount = processes.stream()
                .filter(p -> "DONE".equals(p.getStatus()) || "QC_PENDING".equals(p.getStatus()))
                .count();

        return (int) Math.round(doneCount * 100.0 / totalSteps);
    }

    /**
     * 현재 공정명 계산
     * - IN_PROGRESS 우선
     * - 없으면 READY 중 가장 앞(stepSeq) 단계
     * - 모두 DONE 또는 QC_PENDING이면 "완료"
     * - 그 외에는 "대기"
     */
    private String resolveCurrentProcess(List<WorkOrderProcess> processes) {
        if (processes.isEmpty()) {
            return "대기";
        }

        // IN_PROGRESS 공정 우선
        WorkOrderProcess inProgress = processes.stream()
                .filter(p -> "IN_PROGRESS".equals(p.getStatus()))
                .findFirst()
                .orElse(null);

        if (inProgress != null) {
            return inProgress.getProcess().getProcessName();
        }

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
            return nextReady.getProcess().getProcessName();
        }

        // 공정이 있고, 모두 DONE 또는 QC_PENDING이면 "완료"
        boolean allDoneOrQcPending = processes.stream()
                .allMatch(p -> "DONE".equals(p.getStatus()) || "QC_PENDING".equals(p.getStatus()));

        if (allDoneOrQcPending) {
            return "완료";
        }

        return "대기";
    }

    /**
     * 경과시간 계산 (첫 START_TIME ~ 현재)
     */
    private String calculateElapsedTime(List<WorkOrderProcess> processes) {
        LocalDateTime firstStart = processes.stream()
                .map(WorkOrderProcess::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        if (firstStart == null) {
            return "-";
        }

        Duration d = Duration.between(firstStart, LocalDateTime.now());
        long hours = d.toHours();
        long minutes = d.toMinutesPart();

        return hours + "시간 " + minutes + "분";
    }

    // =========================================================================
    // 2. 공정현황 상세 모달
    // =========================================================================
    @Transactional(readOnly = true)
    public WorkOrderProcessDetailDTO getWorkOrderProcessDetail(String orderId) {

        // 1) 작업지시 기본정보
        WorkOrder workOrder = workOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 작업지시: " + orderId));

        // 2) 제품별 라우트 찾기
        String routeId = workOrder.getRouteId();
        
        RouteHeader routeHeader = routeHeaderRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 작업지시의 라우트가 없습니다. ROUTE_ID=" + routeId));

        // 3) 라우트 단계 목록 (마스터 기준)
        List<RouteStep> steps = routeStepRepository
                .findByRouteHeaderOrderByStepSeqAsc(routeHeader);

        // 4) 이 작업지시의 공정진행 데이터
        List<WorkOrderProcess> processes =
                workOrderProcessRepository.findByWorkOrderOrderIdOrderByStepSeqAsc(orderId);

        // RouteStep 기준으로 WorkOrderProcess 매핑
        Map<String, WorkOrderProcess> processMap = processes.stream()
                .filter(p -> p.getRouteStep() != null && p.getRouteStep().getRouteStepId() != null)
                .collect(Collectors.toMap(
                        p -> p.getRouteStep().getRouteStepId(),
                        p -> p,
                        (p1, p2) -> p1
                ));

        // 5) 상단 요약 DTO
        WorkOrderProcessDTO headerDto = new WorkOrderProcessDTO();
        headerDto.setOrderId(workOrder.getOrderId());
        headerDto.setPrdId(workOrder.getProduct().getPrdId());
        headerDto.setPrdName(workOrder.getProduct().getPrdName());
        headerDto.setPlanQty(workOrder.getPlanQty());
        headerDto.setStatus(workOrder.getStatus());

        // QC 결과 PASS 여부 (포장 공정 시작 조건)
        boolean isQcPassed = qcResultRepository.existsByOrderIdAndOverallResult(orderId, "PASS");

        // 6) 공정 단계 DTO 리스트 + 버튼 플래그 세팅
        List<WorkOrderProcessStepDTO> stepDTOs = buildStepDtos(steps, processMap, isQcPassed);

        return new WorkOrderProcessDetailDTO(headerDto, stepDTOs);
    }

    private List<WorkOrderProcessStepDTO> buildStepDtos(
            List<RouteStep> steps,
            Map<String, WorkOrderProcess> processMap,
            boolean isQcPassed
    ) {

        List<WorkOrderProcessStepDTO> stepDTOs = steps.stream()
                .map(step -> {
                    WorkOrderProcessStepDTO dto = new WorkOrderProcessStepDTO();
                    dto.setStepSeq(step.getStepSeq());
                    dto.setProcessId(step.getProcess().getProcessId());
                    dto.setProcessName(step.getProcess().getProcessName());

                    WorkOrderProcess proc = processMap.get(step.getRouteStepId());

                    if (proc != null) {
                        dto.setStatus(proc.getStatus());
                        dto.setStartTime(proc.getStartTime());
                        dto.setEndTime(proc.getEndTime());
                        dto.setGoodQty(proc.getGoodQty());
                        dto.setDefectQty(proc.getDefectQty());
                        dto.setMemo(proc.getMemo());
                    } else {
                        dto.setStatus("READY");
                        dto.setStartTime(null);
                        dto.setEndTime(null);
                        dto.setGoodQty(null);
                        dto.setDefectQty(null);
                        dto.setMemo(null);
                    }

                    return dto;
                })
                .collect(Collectors.toList());

        // 버튼 활성화 플래그 계산
        for (int i = 0; i < stepDTOs.size(); i++) {
            WorkOrderProcessStepDTO dto = stepDTOs.get(i);
            WorkOrderProcessStepDTO prevDto = (i > 0) ? stepDTOs.get(i - 1) : null;

            boolean prevDone = (i == 0) || "DONE".equals(prevDto.getStatus());
            boolean isPrevBlocking = (prevDto != null) && "QC_PENDING".equals(prevDto.getStatus());

            // 포장 공정은 QC PASS 필요
            if ("PRC-PACK".equals(dto.getProcessId())) {
                dto.setCanStart("READY".equals(dto.getStatus()) && isQcPassed);
            } else {
                dto.setCanStart("READY".equals(dto.getStatus()) && prevDone && !isPrevBlocking);
            }

            dto.setCanFinish("IN_PROGRESS".equals(dto.getStatus()));
        }

        return stepDTOs;
    }

    // =========================================================================
    // 3. 공정 단계 시작 / 종료
    // =========================================================================

    /**
     * 공정 단계 시작 처리
     * - READY → IN_PROGRESS
     * - 최초 시작 시 WORK_ORDER.ACT_START_DATE 설정
     */
    @Transactional
    public WorkOrderProcessStepDTO startStep(String orderId, Integer stepSeq) {

        WorkOrderProcess proc = workOrderProcessRepository
                .findByWorkOrderOrderIdAndStepSeq(orderId, stepSeq)
                .orElseThrow(() -> new IllegalArgumentException("해당 공정 단계가 존재하지 않습니다."));

        if (!"READY".equals(proc.getStatus())) {
            throw new IllegalStateException("대기 상태(READY)인 공정만 시작할 수 있습니다.");
        }

        proc.setStatus("IN_PROGRESS");
        proc.setStartTime(LocalDateTime.now());

        // 최초 시작일자 기록 (이미 값 있으면 유지)
        WorkOrder workOrder = proc.getWorkOrder();
        if (workOrder.getActStartDate() == null) {
            workOrder.setActStartDate(LocalDateTime.now());
        }

        WorkOrderProcess saved = workOrderProcessRepository.save(proc);
        return toStepDTO(saved);
    }

    /**
     * 공정 단계 종료 처리
     * - IN_PROGRESS → DONE or QC_PENDING
     * - 마지막 단계 완료 시 WORK_ORDER 상태 COMPLETED + ACT_END_DATE 설정
     */
    @Transactional
    public WorkOrderProcessStepDTO finishStep(String orderId, Integer stepSeq) {

        WorkOrderProcess proc = workOrderProcessRepository
                .findByWorkOrderOrderIdAndStepSeq(orderId, stepSeq)
                .orElseThrow(() -> new IllegalArgumentException("해당 공정 단계가 존재하지 않습니다."));

        if (!"IN_PROGRESS".equals(proc.getStatus())) {
            throw new IllegalStateException("진행중(IN_PROGRESS) 상태인 공정만 종료할 수 있습니다.");
        }

        String processId = proc.getProcess().getProcessId();

        if ("PRC-CAP".equals(processId)) {
            proc.setStatus("QC_PENDING");
            // TODO: QC_RESULT 생성/연결 로직 추가
        } else {
            proc.setStatus("DONE");
        }

        proc.setEndTime(LocalDateTime.now());

        // 마지막 단계인지 확인
        WorkOrder workOrder = proc.getWorkOrder();
        boolean hasLaterStep =
                workOrderProcessRepository.existsByWorkOrderOrderIdAndStepSeqGreaterThan(orderId, stepSeq);

        if (!hasLaterStep) {
            // 마지막 공정까지 완료 → 작업지시 완료 처리
            workOrder.setStatus("COMPLETED");              // 상태코드는 네 프로젝트 값에 맞게
            workOrder.setActEndDate(LocalDateTime.now());
        }

        WorkOrderProcess saved = workOrderProcessRepository.save(proc);
        return toStepDTO(saved);
    }

    // =========================================================================
    // 4. 공정 메모
    // =========================================================================
    @Transactional
    public WorkOrderProcessStepDTO updateStepMemo(String orderId, Integer stepSeq, String memo) {

        WorkOrderProcess proc = workOrderProcessRepository
                .findByWorkOrderOrderIdAndStepSeq(orderId, stepSeq)
                .orElseThrow(() -> new IllegalArgumentException("해당 공정 단계가 존재하지 않습니다."));

        proc.setMemo(memo);

        WorkOrderProcess saved = workOrderProcessRepository.save(proc);
        return toStepDTO(saved);
    }

    /**
     * 엔티티 → 단계 DTO 변환 (start/finish/메모 응답용)
     */
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
}
