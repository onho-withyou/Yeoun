package com.yeoun.process.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yeoun.process.dto.WorkOrderProcessDTO;
import com.yeoun.process.dto.WorkOrderProcessDetailDTO;
import com.yeoun.process.dto.WorkOrderProcessStepDTO;
import com.yeoun.process.service.WorkOrderProcessService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Controller
@RequestMapping("/process")
@RequiredArgsConstructor
public class WorkOrderProcessController {
	
	private final WorkOrderProcessService workOrderProcessService;
	
	// 공정 현황 페이지
	@GetMapping("/status")
	public String processStatus() {
		return "/process/process_status";
	}
	
	// 공정 현황 목록 데이터
	@GetMapping("/status/data")
	@ResponseBody
	public List<WorkOrderProcessDTO> getWorkOrdersForGrid() {
		return workOrderProcessService.getWorkOrderListForStatus();
	}
	
	// 공정 현황 상세 모달용 데이터
	@GetMapping("/status/detail/{orderId}")
	@ResponseBody
	public WorkOrderProcessDetailDTO getWorkOrderDetail(@PathVariable("orderId") String orderId) {
		return workOrderProcessService.getWorkOrderProcessDetail(orderId);
	}
	
	// -----------------------------
    // 공정 단계 시작/종료/메모
    // -----------------------------
    @PostMapping("/status/step/start")
    @ResponseBody
    public Map<String, Object> startStep(@RequestBody StepRequest req) {
        try {
            WorkOrderProcessStepDTO updated =
                    workOrderProcessService.startStep(req.getOrderId(), req.getStepSeq());
            return Map.of(
                    "success", true,
                    "message", "공정을 시작 처리했습니다.",
                    "updatedStep", updated
            );
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", e.getMessage()
            );
        }
    }

    @PostMapping("/status/step/finish")
    @ResponseBody
    public Map<String, Object> finishStep(@RequestBody StepRequest req) {
        try {
            WorkOrderProcessStepDTO updated =
                    workOrderProcessService.finishStep(req.getOrderId(), req.getStepSeq());
            return Map.of(
                    "success", true,
                    "message", "공정을 종료 처리했습니다.",
                    "updatedStep", updated
            );
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", e.getMessage()
            );
        }
    }

    @PostMapping("/status/step/memo")
    @ResponseBody
    public Map<String, Object> updateMemo(@RequestBody MemoRequest req) {
        try {
            WorkOrderProcessStepDTO updated =
                    workOrderProcessService.updateStepMemo(req.getOrderId(), req.getStepSeq(), req.getMemo());
            return Map.of(
                    "success", true,
                    "message", "메모를 저장했습니다.",
                    "updatedStep", updated
            );
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", e.getMessage()
            );
        }
    }

    // 요청 JSON 바디용 DTO (내부 static 클래스로 둬도 되고, 별도 파일로 빼도 됨)
    @Getter @Setter
    public static class StepRequest {
        private String orderId;
        private Integer stepSeq;
    }

    @Getter @Setter
    public static class MemoRequest {
        private String orderId;
        private Integer stepSeq;
        private String memo;
    }

}
