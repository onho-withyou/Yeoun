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

    // =================================================================================
    // 1. 공정현황 메인 목록
    // - 작업지시별 진행률, 현재 공정, 경과시간 등 표시
    // =================================================================================
    public List<WorkOrderProcessDTO> getWorkOrderListForStatus() {

        // 1) 공정현황 대상이 되는 작업지시 조회 (RELEASED 상태만)
        List<String> statuses = List.of("RELEASED");
        List<WorkOrder> workOrders = workOrderRepository.findByStatusIn(statuses);

        // 2) 각 작업지시를 공정현황 DTO로 변환
        return workOrders.stream()
                .map(this::toProcessSummaryDto)
                .collect(Collectors.toList());
    }

    /**
     * 개별 작업지시 -> 공정현황 요약 DTO 변환
     */
    private WorkOrderProcessDTO toProcessSummaryDto(WorkOrder workOrder) {

        // 이 작업지시에 해당하는 공정진행 목록 (순번 순)
        List<WorkOrderProcess> processes =
                workOrderProcessRepository.findByWorkOrderOrderIdOrderByStepSeqAsc(workOrder.getOrderId());

        // 1) 양품수량
        int goodQty = qcResultRepository.findByOrderId(workOrder.getOrderId())
                .map(qc -> {
                    Integer q = qc.getGoodQty();
                    return (q != null) ? q : 0;
                })
                .orElse(0);

        // 2) 진행률: 공정 단계 기준 (DONE 개수 / 전체 단계수 * 100)
        int progressRate = calculateProgressRate(processes);

        // 3) 현재 공정명: IN_PROGRESS > READY(가장 앞 단계) > 모두 DONE/QC_PENDING이면 "완료"
        String currentProcess = resolveCurrentProcess(processes);

        // 4) 경과시간: 첫 START_TIME ~ 현재시간
        String elapsedTime = calculateElapsedTime(processes);

        // 5) 최종 DTO 생성
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
     * - IN_PROGRESS 단계가 있으면 그 공정명
     * - 없으면 READY 중 가장 앞(stepSeq) 단계의 공정명
     * - 모두 DONE 또는 QC_PENDING이면 "완료"
     * - 그 외에는 "대기"
     */
    private String resolveCurrentProcess(List<WorkOrderProcess> processes) {
        if (processes.isEmpty()) {
            return "대기";
        }

        // 1) IN_PROGRESS 공정 우선
        WorkOrderProcess inProgress = processes.stream()
                .filter(p -> "IN_PROGRESS".equals(p.getStatus()))
                .findFirst()
                .orElse(null);

        if (inProgress != null) {
            return inProgress.getProcess().getProcessName();
        }

        // 2) READY 중 가장 stepSeq가 작은 공정
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

        // 3) 공정이 있고, 모두 DONE 또는 QC_PENDING이면 "완료"
        boolean allDoneOrQcPending = processes.stream()
                .allMatch(p -> "DONE".equals(p.getStatus()) || "QC_PENDING".equals(p.getStatus()));

        if (allDoneOrQcPending) {
            return "완료";
        }

        return "대기";
    }

    /**
     * 경과시간 계산 (첫 START_TIME ~ 현재시간)
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

    // =================================================================================
    // 2. 공정현황 상세 모달
    // - 특정 작업지시의 라우트 기준 단계별 상세 및 버튼 활성화 조건
    // =================================================================================
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
                        p -> p.getRouteStep().getRouteStepId(),
                        p -> p,
                        (p1, p2) -> p1 // 혹시 중복 키가 있으면 첫 번째 것 사용
                ));

        // 5) 상단 요약 DTO
        WorkOrderProcessDTO headerDto = buildHeaderDto(workOrder);

        // 6) QC 결과 확인 (포장 공정 시작 조건에 사용)
        boolean isQcPassed = qcResultRepository.existsByOrderIdAndOverallResult(orderId, "PASS");

        // 7) 공정 단계 DTO 리스트 (RouteStep + WorkOrderProcess 병합 + 버튼 플래그 설정)
        List<WorkOrderProcessStepDTO> stepDTOs =
                buildStepDtos(steps, processMap, isQcPassed);

        // 8) 상세 DTO 리턴
        return new WorkOrderProcessDetailDTO(headerDto, stepDTOs);
    }

    /**
     * 상세 상단 요약 DTO 생성
     */
    private WorkOrderProcessDTO buildHeaderDto(WorkOrder workOrder) {
        WorkOrderProcessDTO dto = new WorkOrderProcessDTO();
        dto.setOrderId(workOrder.getOrderId());
        dto.setPrdId(workOrder.getProduct().getPrdId());
        dto.setPrdName(workOrder.getProduct().getPrdName());
        dto.setPlanQty(workOrder.getPlanQty());
        dto.setStatus(workOrder.getStatus());
        return dto;
    }

    /**
     * 공정 단계 DTO 리스트 생성
     * - RouteStep + WorkOrderProcess 병합
     * - canStart / canFinish 플래그 계산 포함
     */
    private List<WorkOrderProcessStepDTO> buildStepDtos(
            List<RouteStep> steps,
            Map<String, WorkOrderProcess> processMap,
            boolean isQcPassed
    ) {

        // 1) 라우트 단계 기준으로 DTO 생성 (상태/수량/메모 매핑)
        List<WorkOrderProcessStepDTO> stepDTOs = steps.stream()
                .map(step -> {
                    WorkOrderProcessStepDTO dto = new WorkOrderProcessStepDTO();
                    dto.setStepSeq(step.getStepSeq());
                    dto.setProcessId(step.getProcess().getProcessId());
                    dto.setProcessName(step.getProcess().getProcessName());

                    // 이 라우트단계에 해당하는 공정진행 데이터가 있는지 확인
                    WorkOrderProcess proc = processMap.get(step.getRouteStepId());

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
                .collect(Collectors.toList());

        // 2) 버튼 표시 플래그 계산 (시작/종료 가능 여부)
        for (int i = 0; i < stepDTOs.size(); i++) {
            WorkOrderProcessStepDTO dto = stepDTOs.get(i);
            WorkOrderProcessStepDTO prevDto = (i > 0) ? stepDTOs.get(i - 1) : null;

            // 이전 단계 완료 여부 (첫 단계는 true)
            boolean prevDone = (i == 0) || "DONE".equals(prevDto.getStatus());

            // 이전 단계가 QC_PENDING이면 다음 공정 시작 불가
            boolean isPrevStatusBlocking =
                    (prevDto != null) && "QC_PENDING".equals(prevDto.getStatus());

            // 포장 공정(PRC-PACK)의 특수 시작조건
            if ("PRC-PACK".equals(dto.getProcessId())) {
                // 포장 공정은 READY 상태 + QC 합격(PASS)일 때만 시작 가능
                dto.setCanStart("READY".equals(dto.getStatus()) && isQcPassed);
            } else {
                // 일반 공정은 이전 단계 DONE + 이전 단계가 QC_PENDING 아님 + 본인 READY
                dto.setCanStart("READY".equals(dto.getStatus()) && prevDone && !isPrevStatusBlocking);
            }

            // IN_PROGRESS 상태만 종료 가능
            dto.setCanFinish("IN_PROGRESS".equals(dto.getStatus()));
        }

        return stepDTOs;
    }

    // =================================================================================
    // 3. 공정 단계 시작/종료
    // =================================================================================

    /**
     * 공정 단계 시작 처리
     */
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
        
        //  공정 시작 시, WORK_ORDER.ACT_START_DATE 설정 (최초 시작 시)
        WorkOrder workOrder = proc.getWorkOrder();
        if (stepSeq == 1 && workOrder.getActStartDate() == null) {
            workOrder.setActStartDate(LocalDateTime.now());
        }

        WorkOrderProcess saved = workOrderProcessRepository.save(proc);
        return toStepDTO(saved);
    }

    /**
     * 공정 단계 종료 처리
     */
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
            // TODO: QC 검사 등록(QC_RESULT) 데이터 생성/요청 로직 추가
        } else {
            proc.setStatus("DONE");
        }
        
        proc.setEndTime(LocalDateTime.now());

        // 마지막 단계면 WORK_ORDER 완료 처리
        WorkOrder workOrder = proc.getWorkOrder();
        
        // 이 작업지시에 대해 나보다 stepSeq가 큰 공정이 하나도 없으면 -> 마지막 단계
        boolean hasLaterStep =
                workOrderProcessRepository.existsByWorkOrderOrderIdAndStepSeqGreaterThan(orderId, stepSeq);
        
        if (!hasLaterStep) {
            // 마지막 공정까지 끝난 시점이므로 작업지시 완료 처리
            workOrder.setStatus("COMPLETED");          
            workOrder.setActEndDate(LocalDateTime.now());
        }

        WorkOrderProcess saved = workOrderProcessRepository.save(proc);
        return toStepDTO(saved);
    }

    // =================================================================================
    // 4. 공정 메모
    // =================================================================================
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
     * 엔티티 -> 단계 DTO 변환 공통 메서드
     * - startStep / finishStep / 메모 수정 후 응답용
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

        // 여기서는 단일 단계 기준의 canStart/canFinish만 세팅
        dto.setCanStart("READY".equals(proc.getStatus()));
        dto.setCanFinish("IN_PROGRESS".equals(proc.getStatus()));

        return dto;
    }

}