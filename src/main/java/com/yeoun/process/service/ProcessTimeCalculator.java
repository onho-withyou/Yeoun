package com.yeoun.process.service;

import static com.yeoun.process.constant.ProcessTimeStandard.*;

import java.time.Duration;
import java.time.LocalDateTime;

import com.yeoun.process.util.EuConverter;
import com.yeoun.process.util.ProductSpecParser;
import com.yeoun.process.entity.WorkOrderProcess;
import com.yeoun.masterData.entity.ProductMst;
import com.yeoun.order.entity.WorkOrder;

/**
 * =====================================================
 * 공정 예상시간 / 지연 판정 계산기
 * -----------------------------------------------------
 * 역할:
 * 1) 작업지시 기준 총 작업량(EU) 계산
 * 2) 공정별 예상 소요시간 계산
 * 3) 실제 경과시간 대비 지연 여부 판단
 * =====================================================
 */
public class ProcessTimeCalculator {
	
	private ProcessTimeCalculator() {}

    // 작업지시 기준 총 EU 계산
	public static double calcTotalEU(WorkOrder wo) {

	    if (wo == null || wo.getPlanQty() == null) return 0;

	    ProductMst p = wo.getProduct();
	    if (p == null) return wo.getPlanQty();

	    String spec = p.getPrdSpec();      // "용량 30ml - 과일향, 꽃향"
	    String itemName = p.getItemName(); // LIQUID / SOLID

	    int size = ProductSpecParser.extractSize(spec);
	    String form = ProductSpecParser.extractForm(spec, itemName);

	    double euPerUnit = EuConverter.toEU(form, size);

	    return wo.getPlanQty() * euPerUnit;
	}


    // 공정별 예상 소요시간 계산
    public static long calcExpectedMinutes(String processId, double totalEU) {

        // 배치 공정
        Integer batchMin = BATCH_MINUTES.get(processId);
        if (batchMin != null) {
            return batchMin;
        }

        // 단위 공정
        Integer secPerEU = UNIT_SECONDS_PER_EU.get(processId);
        if (secPerEU != null) {
            return (long) Math.ceil((totalEU * secPerEU) / 60.0);
        }

        return 0;
    }

    // 공정 지연 여부 판정
    public static boolean isDelayed(WorkOrderProcess wop, double totalEU) {

        if (wop == null || wop.getStartTime() == null) return false;

        long expectedMin =
            calcExpectedMinutes(
                wop.getProcess().getProcessId(),
                totalEU
            );

        long elapsedMin =
            Duration.between(
                wop.getStartTime(),
                LocalDateTime.now()
            ).toMinutes();

        return expectedMin > 0 && elapsedMin > expectedMin;
    }
}
