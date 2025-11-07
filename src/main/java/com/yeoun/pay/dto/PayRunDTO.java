// src/main/java/com/yeoun/pay/dto/PayRunDTO.java
package com.yeoun.pay.dto;

import com.yeoun.pay.enums.CalcType;
import com.yeoun.pay.enums.RunStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayRunDTO {

    @NotNull(message = "계산유형(CALC_TYPE)은 필수입니다.")
    private CalcType calcType;

    @NotNull(message = "상태(STATUS)는 필수입니다.")
    private RunStatus status;

    @NotNull(message = "요청자(REQ_USER)는 필수입니다.")
    @Positive(message = "요청자(REQ_USER)는 양수여야 합니다.")
    private Long reqUser;

    private LocalDateTime startedDate;
    private LocalDateTime endedDate;
}
