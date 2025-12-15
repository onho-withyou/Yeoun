package com.yeoun.process.service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.yeoun.equipment.entity.ProdLine;
import com.yeoun.equipment.repository.ProdLineRepository;
import com.yeoun.order.repository.WorkOrderRepository;
import com.yeoun.process.dto.ImmediateActionRowDTO;
import com.yeoun.process.dto.LineStayRowDTO;
import com.yeoun.process.dto.ProductionDashboardKpiDTO;
import com.yeoun.process.dto.ProductionTrendResponseDTO;
import com.yeoun.process.dto.StayCellDTO;
import com.yeoun.process.dto.TrendRowDTO;
import com.yeoun.process.entity.WorkOrderProcess;
import com.yeoun.process.mapper.ProductionTrendMapper;
import com.yeoun.process.repository.WorkOrderProcessRepository;
import com.yeoun.qc.repository.QcResultRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductionDashboardService {
	
	private final QcResultRepository qcResultRepository;
	private final WorkOrderRepository workOrderRepository;
    private final WorkOrderProcessRepository workOrderProcessRepository;
    private final ProdLineRepository prodLineRepository;
    private final ProductionTrendMapper productionTrendMapper;

    // 상단 KPI 카드
    public ProductionDashboardKpiDTO getKpis() {

        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        LocalDate tomorrow = today.plusDays(1);
        
        // 큰 숫자
        // 오늘 작업지시
        long todayOrders = workOrderRepository.countTodayOrders(start, end);
        // 진행 중 작업지시 수
        long inProgressOrders = workOrderRepository.countByStatus("IN_PROGRESS");
        // 지연 작업지시 수 (예정완료시간 초과)
        long delayedOrders = workOrderRepository.countByStatusAndPlanEndDateBefore("IN_PROGRESS", now);
        // QC 대기 단계 수
        long qcPendingSteps = workOrderProcessRepository.countQcPendingOnly("PRC-QC");
        // QC 불합격 수
        long qcFailSteps = 
        		qcResultRepository.countByOverallResultAndInspectionDateGreaterThanEqualAndInspectionDateLessThan(
        				"FAIL", today, tomorrow
        	    );
        
        // 작은 숫자
        // 오늘 완료된 작업지시
        long completedToday = workOrderRepository.countByStatusAndActEndDateBetween("COMPLETED", start, end);
        // 오늘 기준 대기(진행중)
        long waitingToday = workOrderRepository.countByStatus("IN_PROGRESS");
        return ProductionDashboardKpiDTO.builder()
                .todayOrders(todayOrders)
                .inProgressOrders(inProgressOrders)
                .delayedOrders(delayedOrders)
                .qcPendingOrders(qcPendingSteps) 
                .qcFailSteps(qcFailSteps) 
                .completedDelta(completedToday)
                .waitingDelta(waitingToday)
                .build();
    }
    
    // =====================================================================================
    // 라인 6단계명 고정
    private static final Map<Integer, String> STEP_NAME_MAP = Map.of(
        1, "블렌딩",
        2, "여과",
        3, "충진",
        4, "캡·펌프",
        5, "품질검사",
        6, "포장"
    );

    // 진행 흐름으로 카운트할 상태들
    private static final Set<String> ACTIVE_STATUSES = Set.of("IN_PROGRESS", "QC_PENDING");

    // 조회는 READY까지 포함(표시/시작대기 계산용)
    private static final List<String> HEATMAP_STATUSES = List.of("IN_PROGRESS", "QC_PENDING", "READY", "DONE");

    public List<LineStayRowDTO> getLineStayHeatmap() {

        LocalDateTime now = LocalDateTime.now();

        // 1) 라인 고정(진행중 없어도 표에 무조건 나옴)
        List<ProdLine> lines = prodLineRepository.findAllByOrderByLineIdAsc();

        // 2) 히트맵 구성에 필요한 WOP 조회
        List<String> orderStatuses = List.of("IN_PROGRESS"); 
        List<WorkOrderProcess> wops =
            workOrderProcessRepository.findForHeatmap(HEATMAP_STATUSES, orderStatuses);

        // 3) (라인ID, stepSeq) 그룹핑
        record Key(String lineId, Integer stepSeq) {}
        Map<Key, List<WorkOrderProcess>> grouped =
            wops.stream().collect(Collectors.groupingBy(
                w -> new Key(
                    w.getWorkOrder().getLine().getLineId(),
                    w.getStepSeq()
                )
            ));

        // 4) 라인 × 6단계 고정 생성
        List<LineStayRowDTO> rows = new ArrayList<>();
        List<StayCellDTO> allCells = new ArrayList<>();

        for (ProdLine line : lines) {
            String lineId = line.getLineId();

            List<StayCellDTO> steps = new ArrayList<>();

            for (int step = 1; step <= 6; step++) {

                List<WorkOrderProcess> list =
                    grouped.getOrDefault(new Key(lineId, step), List.of());

                // 진행중 카운트: IN_PROGRESS/QC_PENDING만
                long activeCnt = list.stream()
                    .filter(w -> ACTIVE_STATUSES.contains(w.getStatus()))
                    .count();

                // READY 카운트(표시용)
                long readyCnt = list.stream()
                    .filter(w -> "READY".equals(w.getStatus()))
                    .count();
                boolean hasReady = readyCnt > 0;

                // "시작대기(startable)" = READY가 있고, 이전 단계에 DONE이 존재하면
                boolean prevDoneExists = false;
                if (step > 1) {
                    List<WorkOrderProcess> prevList =
                        grouped.getOrDefault(new Key(lineId, step - 1), List.of());

                    prevDoneExists = prevList.stream()
                        .anyMatch(w -> "DONE".equals(w.getStatus()));
                }
                long startableCnt = (readyCnt > 0 && prevDoneExists) ? readyCnt : 0;

                // 체류시간: 상태별 기준시간
                long stayMax = list.stream()
                    .mapToLong(w -> {
                        String st = w.getStatus();

                        // 진행중: startTime ~ now
                        if ("IN_PROGRESS".equals(st)) {
                            if (w.getStartTime() == null) return 0;
                            return Duration.between(w.getStartTime(), now).toMinutes();
                        }

                        // QC대기: endTime(공정 종료) ~ now
                        // (너네 시스템에서 QC_PENDING은 startTime 없을 수 있으니 endTime 기준)
                        if ("QC_PENDING".equals(st)) {
                            if (w.getEndTime() == null) return 0;
                            return Duration.between(w.getEndTime(), now).toMinutes();
                        }

                        return 0; // READY/DONE 등은 체류 표시 대상 아님
                    })
                    .max()
                    .orElse(0);

                StayCellDTO cell = StayCellDTO.builder()
                    .lineId(lineId)
                    .lineName(line.getLineName())
                    .stepSeq(step)
                    .stepName(STEP_NAME_MAP.get(step))
                    .stayMin(stayMax)
                    .inProgressCnt(activeCnt)
                    .readyCnt(readyCnt)
                    .hasReady(hasReady)
                    .startableCnt(startableCnt)
                    .level("OK")
                    .build();

                steps.add(cell);
                allCells.add(cell);
            }

            rows.add(LineStayRowDTO.builder()
                .lineId(lineId)
                .lineName(line.getLineName())
                .steps(steps)
                .build());
        }

        // 5) 임계치 없이 TopN 기준 색상 (진행중/대기 흐름이 있는 셀만)
        // - 우선순위: IN_PROGRESS/QC_PENDING 체류 기반
        List<StayCellDTO> ranked =
            allCells.stream()
                .filter(c -> c.getInProgressCnt() > 0) // 진행 흐름이 있는 셀만
                .sorted(Comparator.comparingLong(StayCellDTO::getStayMin).reversed())
                .toList();

        if (!ranked.isEmpty()) ranked.get(0).setLevel("DELAY");
        if (ranked.size() >= 2) ranked.get(1).setLevel("WARN");
        if (ranked.size() >= 3) ranked.get(2).setLevel("WARN");

        return rows;
    }

    // =====================================================================================
    /**
     * 생산 현황 추적 차트 데이터 생성
     */
    public ProductionTrendResponseDTO getProductionTrend(String range) {

        // 1) range 기본값/정규화
        String r = (range == null || range.isBlank()) ? "day" : range.toLowerCase();

        LocalDate today = LocalDate.now();

        // [fromDt, toDt) 범위로 조회 (toDt는 내일 0시)
        LocalDateTime toDt = today.plusDays(1).atStartOfDay();
        LocalDateTime fromDt;

        // 2) 기간 윈도우 결정
        if ("week".equals(r)) {
            // 최근 6주: (오늘 기준) 5주 전 월요일 0시부터
            fromDt = today.minusWeeks(5).with(DayOfWeek.MONDAY).atStartOfDay();
        } else if ("month".equals(r)) {
            // 최근 6개월: 5개월 전 1일 0시부터
            fromDt = today.minusMonths(5).withDayOfMonth(1).atStartOfDay();
        } else {
            // 기본 day: 최근 7일
            r = "day";
            fromDt = today.minusDays(6).atStartOfDay();
        }

        // 3) Mapper에서 집계 조회 
        List<TrendRowDTO> plannedRows = selectPlanned(r, fromDt, toDt);
        List<TrendRowDTO> completedRows = selectCompleted(r, fromDt, toDt);

        // 4) Map<label, cnt> 변환 (라벨 기준으로 빠르게 찾기 위해)
        Map<String, Long> plannedMap = toMap(plannedRows);
        Map<String, Long> completedMap = toMap(completedRows);

        // 5) 라벨 축 생성 (데이터가 없는 구간도 축에 포함 -> 차트 안 깨짐)
        List<String> labels = buildLabels(r, fromDt.toLocalDate(), today);
        
        labels = trimLabels(r, labels);

        // 6) 라벨 순서대로 값 매핑 (없으면 0으로 채움)
        List<Long> planned = labels.stream()
                .map(l -> plannedMap.getOrDefault(l, 0L))
                .toList();

        List<Long> completed = labels.stream()
                .map(l -> completedMap.getOrDefault(l, 0L))
                .toList();

        // 7) 응답 DTO 구성
        return ProductionTrendResponseDTO.builder()
                .range(r)
                .labels(labels)
                .planned(planned)
                .completed(completed)
                .build();
    }
    
    /**
     * 주/월 차트는 최근 구간만 보여주기
     * - week: 최근 4구간
     * - month: 최근 6구간
     */
    private List<String> trimLabels(String r, List<String> labels) {
        if (labels == null) return List.of();

        int keep;
        if ("week".equals(r)) keep = 4;
        else if ("month".equals(r)) keep = 6;
        else return labels; // day는 그대로

        if (labels.size() <= keep) return labels;
        return labels.subList(labels.size() - keep, labels.size());
    }

    // Mapper 호출 분기
    private List<TrendRowDTO> selectPlanned(String r, LocalDateTime fromDt, LocalDateTime toDt) {
        return switch (r) {
            case "week" -> productionTrendMapper.plannedByWeek(fromDt, toDt);
            case "month" -> productionTrendMapper.plannedByMonth(fromDt, toDt);
            default -> productionTrendMapper.plannedByDay(fromDt, toDt);
        };
    }

    private List<TrendRowDTO> selectCompleted(String r, LocalDateTime fromDt, LocalDateTime toDt) {
        return switch (r) {
            case "week" -> productionTrendMapper.completedByWeek(fromDt, toDt);
            case "month" -> productionTrendMapper.completedByMonth(fromDt, toDt);
            default -> productionTrendMapper.completedByDay(fromDt, toDt);
        };
    }

    // =========================
    // Map 변환
    // =========================
    private Map<String, Long> toMap(List<TrendRowDTO> rows) {
        if (rows == null) return Map.of();
        return rows.stream().collect(Collectors.toMap(
                TrendRowDTO::getLabel,
                r -> Optional.ofNullable(r.getCnt()).orElse(0L),
                (a, b) -> a
        ));
    }

    // =========================
    // 라벨 축 생성 (0 채우기용)
    // =========================
    private List<String> buildLabels(String r, LocalDate from, LocalDate toInclusive) {
        List<String> labels = new ArrayList<>();

        if ("month".equals(r)) {
            LocalDate start = from.withDayOfMonth(1);
            LocalDate end = toInclusive.withDayOfMonth(1);

            for (LocalDate d = start; !d.isAfter(end); d = d.plusMonths(1)) {
                labels.add(String.format("%04d-%02d", d.getYear(), d.getMonthValue()));
            }
            return labels;
        }

        if ("week".equals(r)) {

            // fromDt 기준으로 주 단위(월요일)로 돌되,
            // 라벨은 "12월 1주차" 처럼 월 기준 주차로 생성
            LocalDate start = from.with(DayOfWeek.MONDAY);
            LocalDate end = toInclusive.with(DayOfWeek.MONDAY);

            for (LocalDate d = start; !d.isAfter(end); d = d.plusWeeks(1)) {
                int month = d.getMonthValue();

                // 월 기준 주차: (해당 월 1일이 속한 주의 월요일) 기준으로 몇 주차인지 계산
                LocalDate firstDay = d.withDayOfMonth(1);
                LocalDate firstMon = firstDay.with(DayOfWeek.MONDAY);
                if (firstMon.isAfter(firstDay)) firstMon = firstMon.minusWeeks(1);

                long weekNo = java.time.temporal.ChronoUnit.WEEKS.between(firstMon, d) + 1;

                labels.add(month + "월 " + weekNo + "주차");
            }
            return labels;
        }


        // day
        for (LocalDate d = from; !d.isAfter(toInclusive); d = d.plusDays(1)) {
            labels.add(d.toString()); // YYYY-MM-DD
        }
        return labels;
    }

    // ===================================================
    // 4. 즉시 조치 리스트
    public List<ImmediateActionRowDTO> getImmediateActions(int limit) {
        // limit 방어 (0/음수 방지)
        int safeLimit = (limit <= 0) ? 10 : limit;
        return productionTrendMapper.selectImmediateActions(safeLimit);
    }


}
